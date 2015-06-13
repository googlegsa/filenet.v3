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
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.SecurityTemplateList;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.VersionStatusId;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.security.SecurityPolicy;
import com.filenet.api.security.SecurityTemplate;
import com.filenet.api.util.Id;

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
class SecurityPolicyTraverser implements Traverser {
  private static final Logger LOGGER =
      Logger.getLogger(SecurityPolicyTraverser.class.getName());

  private static final String SEC_POLICY_QUERY_WITH_UUID =
      "SELECT TOP {0} * FROM '" + GuidConstants.Class_SecurityPolicy
      + "' WHERE ((" + PropertyNames.DATE_LAST_MODIFIED + " = {1}) AND ("
      + PropertyNames.ID + " > {2})) OR ("
      + PropertyNames.DATE_LAST_MODIFIED + " > {1}) ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private static final String SEC_POLICY_QUERY_WITHOUT_UUID =
      "SELECT TOP {0} * FROM '" + GuidConstants.Class_SecurityPolicy
      + "' WHERE " + PropertyNames.DATE_LAST_MODIFIED + " > {1} ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private static final String DOCS_BY_SEC_POLICY_QUERY =
      "SELECT " + PropertyNames.ID + ", " + PropertyNames.NAME + ", "
      + PropertyNames.SECURITY_FOLDER + " FROM '" + GuidConstants.Class_Document
      + "' WHERE " + PropertyNames.SECURITY_POLICY + " = Object({0})";

  private final IConnection connection;
  private final IObjectFactory objectFactory;
  private final IObjectStore os;
  private final FileConnector connector;

  private int batchHint = 1000;

  public SecurityPolicyTraverser(IConnection connection,
      IObjectFactory objectFactory, IObjectStore os,
      FileConnector connector) {
    this.connection = connection;
    this.objectFactory = objectFactory;
    this.os = os;
    this.connector = connector;
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
    connection.refreshSUserContext();
    try {
      LinkedList<AclDocument> acls = getDocuments(checkpoint);
      if (acls.isEmpty()) {
        LOGGER.fine("No updated security policy is found");
        return null;
      } else {
        LOGGER.fine("Found " + acls.size()
            + " documents affected by security policy updates");
        return new SecurityPolicyDocumentList(acls, checkpoint);
      }
    } catch (EngineRuntimeException e) {
      throw new RepositoryException("Failed to get security policies", e);
    }
  }

  private String getCheckpointValue(Checkpoint checkpoint,
      JsonField jsonField) {
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
    IndependentObjectSet secPolicySet =
        searcher.execute(buildSecurityPolicyQuery(checkpoint), 100, 0);
    Iterator<?> secPolicyIter = secPolicySet.iterator();
    int docCount = 0;
    while (secPolicyIter.hasNext() && (docCount < batchHint)) {
      SecurityPolicy secPolicy = (SecurityPolicy) secPolicyIter.next();
      Date lastModified = secPolicy.get_DateLastModified();
      Id secPolicyId = secPolicy.get_Id();
      LOGGER.log(Level.FINEST,
          "Processing security templates for security policy: {0} {1}",
          new Object[] {secPolicy.get_Name(), secPolicy.get_Id()});
      SecurityTemplateList secTemplates = secPolicy.get_SecurityTemplates();
      LOGGER.log(Level.FINEST,
          "Found {0} security templates for {1} security policy",
          new Object[] {secTemplates.size(), secPolicy.get_Id()});
      for (Object o : secTemplates) {
        SecurityTemplate secTemplate = (SecurityTemplate) o;
        if (VersionStatusId.RELEASED.toString().equals(
                secTemplate.get_ApplyStateID().toString())) {
          Permissions permissions =
              new Permissions(secTemplate.get_TemplatePermissions());
          if (hasPermissions(permissions)) {
            IndependentObjectSet docSet = searcher.execute(
                buildDocumentSearchQuery(secPolicy), 100, 1);
            Iterator<?> docIter = docSet.iterator();
            while (docIter.hasNext()) {
              // Document collides with the SPI class of the same name.
              com.filenet.api.core.Document doc =
                  (com.filenet.api.core.Document) docIter.next();
              String parentId = null;
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
    String timeStr = FileUtil.getQueryTimeString(getLastModified(checkpoint));
    String uuid =
        getCheckpointValue(checkpoint, JsonField.UUID_SECURITY_POLICY);

    if (Strings.isNullOrEmpty(uuid)) {
      return MessageFormat.format(SEC_POLICY_QUERY_WITHOUT_UUID,
          new Object[] {batchHint, timeStr});
    } else {
      return MessageFormat.format(SEC_POLICY_QUERY_WITH_UUID,
          new Object[] {batchHint, timeStr, uuid});
    }
  }

  private String getLastModified(Checkpoint checkpoint) {
    String lastModified =
        getCheckpointValue(checkpoint, JsonField.LAST_SECURITY_POLICY_TIME);
    LOGGER.log(Level.FINEST,
        "Last updated security policy in the checkpoint: {0}", lastModified);
    if (lastModified == null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      lastModified = Value.calendarToIso8601(cal);
      LOGGER.log(Level.FINEST, "Checkpoint does not contain security policy "
          + "last modified time, use current time [{0}] to search for updates.",
          lastModified);
    }
    return lastModified;
  }

  private String buildDocumentSearchQuery(SecurityPolicy secPolicy)
      throws RepositoryException {
    return MessageFormat.format(DOCS_BY_SEC_POLICY_QUERY, secPolicy.get_Id());
  }

  private boolean hasPermissions(Permissions permissions) {
    return permissions.getAllowUsers().size() > 0
            || permissions.getDenyUsers().size() > 0
            || permissions.getAllowGroups().size() > 0
            || permissions.getDenyGroups().size() > 0;
  }

  private static class SecurityPolicyDocumentList implements DocumentList {
    private final LinkedList<AclDocument> acls;
    private final Checkpoint checkpoint;

    private Date lastModified;
    private Id secPolicyId;

    public SecurityPolicyDocumentList(LinkedList<AclDocument> acls, 
        Checkpoint checkpoint) {
      this.acls = acls;
      this.checkpoint = checkpoint;
    }

    @Override
    public Document nextDocument() throws RepositoryException {
      AclDocument aclDoc = acls.poll();
      if (aclDoc != null) {
        lastModified = aclDoc.getCheckpointLastModified();
        secPolicyId = aclDoc.getCheckpointLastUuid();
        LOGGER.log(Level.FINEST,
            "Next Security Policy ACL document [UUID: {0}, Last Modified: {1}]",
            new Object[] {secPolicyId, lastModified});
      }
      return aclDoc;
    }

    @Override
    public String checkpoint() throws RepositoryException {
      if (lastModified != null && secPolicyId != null) {
        checkpoint.setTimeAndUuid(
            JsonField.LAST_SECURITY_POLICY_TIME, lastModified,
            JsonField.UUID_SECURITY_POLICY, secPolicyId);
      }
      return checkpoint.toString();
    }
  }
}
