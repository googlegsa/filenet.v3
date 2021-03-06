// Copyright 2007-2010 Google Inc.  All Rights Reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.util.Id;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete Document class with all the functionalities of Document
 */
public class FileDocument implements Document {
  private static final Logger logger =
      Logger.getLogger(FileDocument.class.getName());

  private static Boolean hasMarkings;

  private final Id docId;
  private final IObjectFactory objectFactory;
  private final IObjectStore objectStore;
  private final FileConnector connector;
  private final TraversalContext traversalContext;

  private IDocument document = null;
  private String vsDocId;
  private boolean pushAcls;
  private Permissions.Acl permissions;

  public FileDocument(Id docId, IObjectFactory objectFactory,
      IObjectStore objectStore, FileConnector connector,
      TraversalContext traversalContext) {
    this.docId = docId;
    this.objectFactory = objectFactory;
    this.objectStore = objectStore;
    this.connector = connector;
    this.traversalContext = traversalContext;
    this.pushAcls = connector.pushAcls();
  }

  @VisibleForTesting
  boolean checkForMarkings() throws RepositoryException {
    if (!(pushAcls && connector.checkMarking())) {
      return false;
    }
    synchronized (FileDocument.class) {
      if (hasMarkings == null) {
        // If the additional WHERE clause starts with SELECT,
        // then the config may not be using the Document class.
        String whereClause = connector.getAdditionalWhereClause();
        if (whereClause != null &&
            whereClause.trim().toUpperCase().startsWith("SELECT")) {
          hasMarkings = Boolean.TRUE;
        } else {
          hasMarkings = hasMarkings();
        }
      }
      return hasMarkings;
    }
  }

  // TODO(bmj): This code was lifted from FileAuthorizationHandler.
  private boolean hasMarkings() throws RepositoryException {
    // Check for the marking sets applied over the document class.
    try {
      PropertyDefinitionList propertyDefinitions =
          objectFactory.getPropertyDefinitions(objectStore,
              GuidConstants.Class_Document, null);
      Iterator<?> iter = propertyDefinitions.iterator();
      while (iter.hasNext()) {
        Object propertyDefinition = iter.next();
        // Only string properties can have a marking set, and
        // get_MarkingSet is defined directly on PropertyDefinitionString.
        if (propertyDefinition instanceof PropertyDefinitionString) {
          MarkingSet markingSet =
              ((PropertyDefinitionString) propertyDefinition).get_MarkingSet();
          if (markingSet != null) {
            logger.info("Document class has a property with a marking set");
            return true;
          }
        }
      }
      logger.info("Document class has no properties with a marking set");
      return false;
    } catch (Exception ecp) {
      logger.log(Level.SEVERE, "Failure checking for a marking set", ecp);
      // This was the existing behavior when an exception was thrown, to
      // use checkMarkings, and if we're here then checkMarkings is true.
      return true;
    }
  }

  private void fetch() throws RepositoryException {
    if (document != null) {
      return;
    }
    document = (IDocument) objectStore.fetchObject(ClassNames.DOCUMENT, docId,
        FileUtil.getDocumentPropertyFilter(connector.getIncludedMeta()));
    logger.log(Level.FINE, "Fetch document for DocId {0}", docId);
    vsDocId = document.getVersionSeries().get_Id().toString();
    logger.log(Level.FINE, "VersionSeriesID for document is: {0}", vsDocId);
    if (checkForMarkings()) {
      if (!document.get_ActiveMarkings().isEmpty()) {
        logger.log(Level.FINE, "Document {0} has an active marking set - "
            + "ignoring ACL.", vsDocId);
        pushAcls = false;
      }
    }
    if (pushAcls) {
      permissions = new Permissions(document.get_Permissions(),
          document.get_Owner()).getAcl();
    } else {
      permissions = null;
    }
  }

  private boolean hasSupportedMimeType() throws RepositoryException {
    String value = Value.getSingleValueString(this, PropertyNames.MIME_TYPE);
    if (value == null) {
      logger.log(Level.FINEST,
          "Send content to the GSA since the {0} value is null [DocId: {1}]",
          new Object[] {PropertyNames.MIME_TYPE, docId});
      return true;
    } else {
      int supportLevel = traversalContext.mimeTypeSupportLevel(value);
      if (supportLevel < 0) {
        throw new SkippedDocumentException("Excluded "
            + PropertyNames.MIME_TYPE + " [" + value
            + "]: Skip document [DocId: " + docId.toString() + "]");
      } else if (supportLevel == 0) {
        logger.log(Level.FINER, "Unsupported {0} [{1}]: "
            + "Send document metadata [DocId: {2}]",
            new Object[] {PropertyNames.MIME_TYPE, value, docId});
        return false;
      } else {
        return true;
      }
    }
  }

  private boolean hasAllowableSize() throws RepositoryException {
    String value = Value.getSingleValueString(this, PropertyNames.CONTENT_SIZE);
    if (value == null) {
      logger.log(Level.FINEST,
          "Send content to the GSA since the {0} value is null [DocId: {1}]",
          new Object[] {PropertyNames.CONTENT_SIZE, docId});
      return true;
    } else {
      double contentSize = Double.parseDouble(value);
      if (contentSize > 0) {
        if (contentSize <= traversalContext.maxDocumentSize()) {
          logger.log(Level.FINEST, "{0} : {1}",
              new Object[] {PropertyNames.CONTENT_SIZE, contentSize});
          return true;
        } else {
          logger.log(Level.FINER,
              "{0} [{1}] exceeds the allowable size [DocId: {2}]",
              new Object[] {PropertyNames.CONTENT_SIZE, contentSize, docId});
          return false;
        }
      } else {
        logger.log(Level.FINEST, "{0} is empty [DocId: {1}]",
            new Object[] {PropertyNames.CONTENT_SIZE, docId});
        return false;
      }
    }
  }

  @Override
  public Property findProperty(String name) throws RepositoryException {
    LinkedList<Value> list = new LinkedList<Value>();

    fetch();
    // TODO (tdnguyen): refactor this method or conditions below to alleviate
    // performance concern with using Value.getStringValueString().
    // if (!name.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX)) {
    //    document.getProperty(name, list);
    //    return new SimpleProperty(this);
    // } else if (SpiConstants.PROPNAME_CONTENT.equals(name) {
    //    ... All google: properties...
    // } else {
    //    return null;
    // }
    if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      if (traversalContext != null && hasSupportedMimeType()
          && hasAllowableSize()) {
        list.add(Value.getBinaryValue(document.getContent()));
        return new SimpleProperty(list);
      } else {
        return null;
      }
    } else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getStringValue(connector.getWorkplaceDisplayUrl()
              + vsDocId));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getBooleanValue(connector.isPublic()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      this.document.getPropertyDateValue("DateLastModified", list);
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
      document.getPropertyStringValue("MimeType", list);
      logger.log(Level.FINEST, "Getting property: " + name);
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getStringValue(vsDocId));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
      list.add(Value.getStringValue(SpiConstants.ActionType.ADD.toString()));
      logger.fine("Getting Property " + name + " : "
          + SpiConstants.ActionType.ADD.toString());
      return new SimpleProperty(list);
    }
    if (pushAcls) {
      if (SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
        addPrincipals(list, name,
            permissions.getAllowUsers(PermissionSource.SOURCE_DEFAULT));
        addPrincipals(list, name,
            permissions.getAllowUsers(PermissionSource.SOURCE_DIRECT));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACLDENYUSERS.equals(name)) {
        addPrincipals(list, name,
            permissions.getDenyUsers(PermissionSource.SOURCE_DEFAULT));
        addPrincipals(list, name,
            permissions.getDenyUsers(PermissionSource.SOURCE_DIRECT));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACLGROUPS.equals(name)) {
        addPrincipals(list, name,
            permissions.getAllowGroups(PermissionSource.SOURCE_DEFAULT));
        addPrincipals(list, name,
            permissions.getAllowGroups(PermissionSource.SOURCE_DIRECT));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACLDENYGROUPS.equals(name)) {
        addPrincipals(list, name,
            permissions.getDenyGroups(PermissionSource.SOURCE_DEFAULT));
        addPrincipals(list, name,
            permissions.getDenyGroups(PermissionSource.SOURCE_DIRECT));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACLINHERITFROM_DOCID.equals(name)) {
        String parentId = getParentId();
        if (parentId == null) {
          return null;
        } else {
          logger.log(Level.FINE, "{0}: {1}", new Object[] {
              SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, parentId});
          list.add(Value.getStringValue(parentId));
          return new SimpleProperty(list);
        }
      }
    }
    if (name.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX)) {
      return null;
    } else {
      document.getProperty(name, list);
      return new SimpleProperty(list);
    }
  }

  private String getParentId() throws RepositoryException {
    if (hasPermissions(PermissionSource.SOURCE_TEMPLATE)) {
      return docId + AclDocument.SEC_POLICY_POSTFIX;
    } else if (hasPermissions(PermissionSource.SOURCE_PARENT)) {
      return docId + AclDocument.SEC_FOLDER_POSTFIX;
    } else {
      return null;
    }
  }

  private boolean hasPermissions(PermissionSource permSrc) {
    return !(permissions.getAllowUsers(permSrc).isEmpty()
            && permissions.getDenyUsers(permSrc).isEmpty()
            && permissions.getAllowGroups(permSrc).isEmpty()
            && permissions.getDenyGroups(permSrc).isEmpty());
  }

  /*
   * Helper method to add allow/deny users and groups to property using default
   * principal type, namespace and case sensitivity.
   */
  private void addPrincipals(List<Value> list, String propName,
      Set<String> names) {
    FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
        connector.getGoogleGlobalNamespace(), names,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    logger.log(Level.FINEST, "Getting {0}: {1}",
        new Object[] { propName, names});
  }

  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    fetch();
    Set<String> properties = new HashSet<String>();

    if (pushAcls) {
      properties.add(SpiConstants.PROPNAME_ACLUSERS);
      properties.add(SpiConstants.PROPNAME_ACLDENYUSERS);
      properties.add(SpiConstants.PROPNAME_ACLGROUPS);
      properties.add(SpiConstants.PROPNAME_ACLDENYGROUPS);
    }

    Set<String> documentProperties = document.getPropertyNames();
    for (String property : documentProperties) {
      if (property != null) {
        if (connector.getIncludedMeta().size() != 0) {
          // includeMeta - excludeMeta
          if ((!connector.getExcludedMeta().contains(property)
              && connector.getIncludedMeta().contains(property))) {
            properties.add(property);
          }
        } else {
          // superSet - excludeMeta
          if ((!connector.getExcludedMeta().contains(property))) {
            properties.add(property);
          }
        }
      }
    }
    // TODO(jlacey): Add logging for property names in Connector Manager.
    logger.log(Level.FINEST, "Property names: {0}", properties);

    return properties;
  }

  public void processInheritedPermissions(LinkedList<Document> acls)
      throws RepositoryException {
    fetch();
    if (!pushAcls) {
      return;
    }
    logger.log(Level.FINEST, "Process inherited permissions for document: {0}",
        docId);

    // Send add request for adding ACLs inherited from parent folders.
    String secParentId = null;
    Document folderAclDoc = createAclDocument(PermissionSource.SOURCE_PARENT,
        AclDocument.SEC_FOLDER_POSTFIX, null);
    if (folderAclDoc != null) {
      logger.log(Level.FINEST, "Create ACL document for folder {0}{1}",
          new Object[] {docId, AclDocument.SEC_FOLDER_POSTFIX});
      acls.add(folderAclDoc);
      secParentId = docId + AclDocument.SEC_FOLDER_POSTFIX;
    }

    // Send add request for adding ACLs inherited from security template.
    Document secAclDoc = createAclDocument(PermissionSource.SOURCE_TEMPLATE,
        AclDocument.SEC_POLICY_POSTFIX, secParentId);
    if (secAclDoc != null) {
      logger.log(Level.FINEST,
          "Create ACL document for security template {0}{1}",
          new Object[] {docId, AclDocument.SEC_POLICY_POSTFIX});
      acls.add(secAclDoc);
    }
  }

  private Document createAclDocument(PermissionSource permSrc, String postfix,
      String parentId) throws RepositoryException {
    Set<String> allowUsers = permissions.getAllowUsers(permSrc);
    Set<String> denyUsers = permissions.getDenyUsers(permSrc);
    Set<String> allowGroups = permissions.getAllowGroups(permSrc);
    Set<String> denyGroups = permissions.getDenyGroups(permSrc);
    if (allowUsers.isEmpty() && denyUsers.isEmpty()
            && allowGroups.isEmpty() && denyGroups.isEmpty()) {
      logger.log(Level.FINEST,
          "Document {0} does not have inherited permissions", docId);
      return null;
    } else {
      return new AclDocument(docId + postfix, parentId,
          AclInheritanceType.CHILD_OVERRIDES,
          connector.getGoogleGlobalNamespace(), allowUsers, denyUsers,
          allowGroups, denyGroups);
    }
  }
}
