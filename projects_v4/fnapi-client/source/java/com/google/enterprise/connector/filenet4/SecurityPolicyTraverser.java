// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.filenet4.filewrap.ISecurityPolicy;
import com.google.enterprise.connector.filenet4.filewrap.ISecurityTemplate;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.VersionStatusId;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks for updated security policies so that it will send updated TMPL ACLs
 * to the GSA for documents inheriting permissions from these policies.
 */
class SecurityPolicyTraverser implements Traverser, DocumentList {
  private static final Logger LOGGER =
      Logger.getLogger(SecurityPolicyTraverser.class.getName());

  private static final String SEC_POLICY_QUERY_WITH_UUID =
      "SELECT {0}* FROM '" + GuidConstants.Class_SecurityPolicy
      + "' WHERE ((" + PropertyNames.DATE_LAST_MODIFIED + " = {1}) AND ("
      + PropertyNames.ID + " > {2})) OR ("
      + PropertyNames.DATE_LAST_MODIFIED + " > {1}) ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private static final String SEC_POLICY_QUERY_WITHOUT_UUID =
      "SELECT {0}* FROM '" + GuidConstants.Class_SecurityPolicy
      + "' WHERE " + PropertyNames.DATE_LAST_MODIFIED + " > {1} ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private static final String DOCS_BY_SEC_POLICY_QUERY =
      "SELECT " + PropertyNames.ID + ", " + PropertyNames.NAME + ", "
      + PropertyNames.SECURITY_FOLDER + " FROM '" + GuidConstants.Class_Document
      + "' WHERE " + PropertyNames.SECURITY_POLICY + " = Object({0})";

  private final IObjectFactory objectFactory;
  private final IObjectStore os;
  private final FileConnector connector;

  private int batchHint = 500;
  private String lastTimestamp;
  private Date lastModified;
  private IId secPolicyId;
  private Checkpoint checkpoint;
  private LinkedList<AclDocument> acls;

  public SecurityPolicyTraverser(IObjectFactory objectFactory, IObjectStore os,
      FileConnector connector) {
    this.objectFactory = objectFactory;
    this.os = os;
    this.connector = connector;
    this.lastModified = new Date();
  }

  @Override
  public void setTraversalContext(TraversalContext traversalContext) {
  }

  @Override
  public void setBatchHint(int batchHint) {
    this.batchHint = batchHint;
  }

  @Override
  public DocumentList getDocumentList(Checkpoint checkpoint)
      throws RepositoryException {
    LOGGER.fine("Searching for documents by updated security policy");
    os.refreshSUserContext();
    this.checkpoint = checkpoint;

    lastTimestamp =
        getCheckpointValue(JsonField.LAST_SECURITY_POLICY_TIME, checkpoint);
    LOGGER.log(Level.FINEST,
        "Last updated security policy in the checkpoint: {0}", lastTimestamp);
    if (lastTimestamp == null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(lastModified);
      lastTimestamp = Value.calendarToIso8601(cal);
    }
    acls = getDocuments(checkpoint);
    if (acls.isEmpty()) {
      LOGGER.fine("No updated security policy is found");
      return null;
    } else {
      LOGGER.fine("Found " + acls.size()
          + " documents affected by security policy updates");
      return this;
    }
  }

  private String getCheckpointValue(JsonField jsonField,
      Checkpoint checkpoint) {
    try {
      return checkpoint.getString(jsonField);
    } catch (RepositoryException re) {
      // Ignore exception when JsonField is not retrievable or when the
      // checkpoint is empty.
      return null;
    }
  }

  private LinkedList<AclDocument> getDocuments(Checkpoint checkpoint)
      throws RepositoryException {
    ISearch searcher = objectFactory.getSearch(os);
    LinkedList<AclDocument> docs = new LinkedList<AclDocument>();
    IObjectSet secPolicySet =
        searcher.execute(buildSecurityPolicyQuery(checkpoint), 100, 0,
            objectFactory.getFactory(ClassNames.SECURITY_POLICY));
    IBaseObjectFactory docFactory =
        objectFactory.getFactory(ClassNames.DOCUMENT);
    Iterator<? extends IBaseObject> secPolicyIter = secPolicySet.getIterator();
    int docCount = 0;
    while (secPolicyIter.hasNext() && (docCount < batchHint)) {
      ISecurityPolicy secPolicy = (ISecurityPolicy) secPolicyIter.next();
      lastModified = secPolicy.getModifyDate();
      secPolicyId = secPolicy.get_Id();
      LOGGER.log(Level.FINEST,
          "Processing security templates for security policy: {0} {1}",
          new Object[] {secPolicy.get_Name(), secPolicy.get_Id()});
      for (ISecurityTemplate secTemplate : secPolicy.getSecurityTemplates()) {
        if (VersionStatusId.RELEASED.toString().equals(
                secTemplate.get_ApplyStateID().toString())) {
          Permissions permissions = secTemplate.get_TemplatePermissions();
          if (hasPermissions(permissions)) {
            IObjectSet docSet = searcher.execute(
                buildDocumentSearchQuery(secPolicy), 100, 1, docFactory);
            Iterator<? extends IBaseObject> docIter = docSet.getIterator();
            while (docIter.hasNext()) {
              String parentId = null;
              IDocument doc = (IDocument) docIter.next();
              if (doc.get_SecurityFolder() != null) {
                parentId = doc.get_Id() + AclDocument.SEC_FOLDER_POSTFIX;
              }
              String tmplDocId =
                  doc.get_Id() + AclDocument.SEC_POLICY_POSTFIX;
              AclDocument aclDoc = new AclDocument(tmplDocId, parentId,
                  AclInheritanceType.CHILD_OVERRIDES,
                  connector.getGoogleGlobalNamespace(),
                  permissions.getAllowUsers(), permissions.getDenyUsers(),
                  permissions.getAllowGroups(), permissions.getDenyGroups());
              aclDoc.setCheckpointLastModified(lastModified);
              aclDoc.setCheckpointLastUuid(secPolicyId);
              docs.add(aclDoc);
              LOGGER.log(Level.FINEST,
                  "Update Parent ACL {0} for Security Policy {1}",
                  new Object[] {tmplDocId, secPolicyId});
              docCount++;
            }
          }
          // There is only one RELEASED template in each security policy
          break;
        }
      }
    }
    return docs;
  }

  private String buildSecurityPolicyQuery(Checkpoint checkpoint)
      throws RepositoryException {
    String timeStr = FileUtil.getQueryTimeString(lastTimestamp);
    String uuid =
        getCheckpointValue(JsonField.UUID_SECURITY_POLICY, checkpoint);

    String topStr = "";
    if (batchHint > 0) {
      topStr = "TOP " + String.valueOf(batchHint) + " ";
    }
    if (Strings.isNullOrEmpty(uuid)) {
      return MessageFormat.format(SEC_POLICY_QUERY_WITHOUT_UUID,
          new Object[] {topStr, timeStr});
    } else {
      return MessageFormat.format(SEC_POLICY_QUERY_WITH_UUID,
          new Object[] {topStr, timeStr, uuid});
    }
  }

  private String buildDocumentSearchQuery(ISecurityPolicy secPolicy)
      throws RepositoryException {
    return MessageFormat.format(DOCS_BY_SEC_POLICY_QUERY, secPolicy.get_Id());
  }

  private boolean hasPermissions(Permissions permissions) {
    return permissions.getAllowUsers().size() > 0
            || permissions.getDenyUsers().size() > 0
            || permissions.getAllowGroups().size() > 0
            || permissions.getDenyGroups().size() > 0;
  }

  @Override
  public Document nextDocument() throws RepositoryException {
    if (acls == null || acls.isEmpty()) {
      return null;
    } else {
      AclDocument aclDoc = acls.poll();
      lastModified = aclDoc.getCheckpointLastModified();
      secPolicyId = aclDoc.getCheckpointLastUuid();
      LOGGER.log(Level.FINEST,
          "Next Security Policy ACL document [UUID: {0}, Last Modified: {1}]",
          new Object[] {secPolicyId, lastModified});
      return aclDoc;
    }
  }

  @Override
  public String checkpoint() throws RepositoryException {
    if (checkpoint == null) {
      return null;
    }
    if (lastModified != null && secPolicyId != null) {
      checkpoint.setTimeAndUuid(JsonField.LAST_SECURITY_POLICY_TIME,
              lastModified, JsonField.UUID_SECURITY_POLICY, secPolicyId);
    }
    return checkpoint.toString();
  }
}
