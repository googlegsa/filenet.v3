// Copyright 2009 Google Inc. All Rights Reserved.
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

import static com.google.common.collect.Sets.union;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.adaptor.Acl;
import com.google.enterprise.adaptor.DocId;
import com.google.enterprise.adaptor.DocIdPusher;
import com.google.enterprise.adaptor.GroupPrincipal;
import com.google.enterprise.adaptor.IOHelper;
import com.google.enterprise.adaptor.Response;
import com.google.enterprise.adaptor.UserPrincipal;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.SearchWrapper;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Containable;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for: 1. Construction of FileNet SQL queries for adding and
 * deleting index of documents to GSA. 2. Execution of the SQL query constructed
 * in step 1. 3. Retrieve the results of step 2 and wrap it in DocumentList
 */
class DocumentTraverser {
  private static final Logger logger =
      Logger.getLogger(DocumentTraverser.class.getName());

  private final IConnection connection;
  private final IObjectFactory objectFactory;
  private final IObjectStore objectStore;
  private final FileConnector connector;

  private int batchHint = 1000;

  public DocumentTraverser(IConnection connection,
      IObjectFactory objectFactory, IObjectStore objectStore,
      FileConnector fileConnector) {
    this.connection = connection;
    this.objectFactory = objectFactory;
    this.objectStore = objectStore;
    this.connector = fileConnector;
  }

  /**
   * To set BatchHint for traversal.
   */
  public void setBatchHint(int batchHint) {
    this.batchHint = batchHint;
  }

  // class Checkpoint {
  //   private static String dateToString(Date timestamp) {
  //     Calendar cal = Calendar.getInstance();
  //     cal.setTime(nextCheckpointDate);
  //     return FileUtil.getQueryTimeString(Value.calendarToIso8601(cal));
  //   }

  //   private final String type;
  //   private final String timestamp;
  //   private final String guid;

  //   public Checkpoint fromDocId(DocId docId) {
  //     String[] parts = docId.getUniqueId().split("/", 0);
  //     if (!parts[0].equals("pseudo")) {
  //       return null;
  //     }
  //     return new Checkpoint(parts[
  //   }

  //   public Checkpoint(Checkpoint checkpoint, Date timestamp, Id guid) {
  //     type = checkpoint.type;
  //     this.timestamp = dateToString(timestamp); // TODO: No, not really.
  //     this.guid = guid.toString();
  //   }

  //   private Checkpoint(String type, String timestamp, String guid) {
  //     this.type = type;
  //     this.timestamp = timestamp;
  //     this.guid = guid;
  //   }

  //   public String getTimestamp() {
  //     return timestamp;
  //   }

  //   public String getGuid() {
  //     return guid;
  //   }

  //   public DocId getDocId() { return new DocId(toString()); }

  //   public String toString() {
  //     return String.format("pseudo/type=%1$s;timestamp=%2$s;guid=%3$s",
  //         type, timestamp, guid);
  //   }
  // }

  // TODO: void getDocIds(Checkpoint, DocIdPusher)
  public void getDocIds(String checkpointStr, DocIdPusher pusher)
      throws IOException {
    try {
      Checkpoint checkpoint = new Checkpoint(checkpointStr);

      connection.refreshSUserContext();
      logger.log(Level.FINE, "Target ObjectStore is: {0}", objectStore);

      SearchWrapper search = objectFactory.getSearch(objectStore);

      String query = buildQueryString(checkpoint);
      logger.log(Level.FINE, "Query for added or updated documents: {0}",
          query);
      IndependentObjectSet objectSet = search.fetchObjects(query, batchHint,
          SearchWrapper.dereferenceObjects, SearchWrapper.ALL_ROWS);
      logger.fine(objectSet.isEmpty()
          ? "Found no documents to add or update"
          : "Found documents to add or update");

      ArrayList<DocId> docIds = new ArrayList<>();
      Date timestamp = null;
      Id guid = null; // TODO: Id.ZERO_ID?
      Iterator<?> objects = objectSet.iterator();
      while (objects.hasNext()) {
        // Avoid clash with SPI Document class.
        Containable object = (Containable) objects.next();
        timestamp = object.get_DateLastModified();
        guid = object.get_Id(); // TODO: Use the VersionSeries ID.
        docIds.add(new DocId("guid/" + guid));
      }
      if (timestamp != null) {
        checkpoint.setTimeAndUuid(
            JsonField.LAST_MODIFIED_TIME, timestamp,
            JsonField.UUID, guid);
        docIds.add(new DocId("pseudo/" + checkpoint));
        pusher.pushDocIds(docIds);
      }
    } catch (EngineRuntimeException | RepositoryException
        | InterruptedException e) {
      throw new IOException(e);
    }
  }

  // TODO: void getDocContent(Id, Request, Response)
  public void getDocContent(String idStr, Response response)
      throws IOException {
    Id id = new Id(idStr);
    try {
      logger.log(Level.FINEST, "Add document [ID: {0}]", id);
      LocalDocument doc =
          new LocalDocument(id, new DocId("guid/" + id), response);
    } catch (EngineRuntimeException | RepositoryException e) {
      throw new IOException(e);
    }
  }

  /**
   * To construct FileNet query to fetch documents from FileNet repository
   * considering additional where clause specified as connector
   * configuration and the previously remembered checkpoint to indicate where
   * to resume acquiring documents from the FileNet repository to send feed.
   */
  private String buildQueryString(Checkpoint checkpoint)
      throws RepositoryException {
    StringBuilder query = new StringBuilder("SELECT TOP ");
    query.append(batchHint);
    query.append(" ");
    query.append(PropertyNames.ID);
    query.append(",");
    query.append(PropertyNames.DATE_LAST_MODIFIED);
    query.append(",");
    query.append(PropertyNames.RELEASED_VERSION);
    query.append(" FROM ");
    query.append(GuidConstants.Class_Document);
    query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

    String additionalWhereClause = connector.getAdditionalWhereClause();
    if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
      if ((additionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
        query = new StringBuilder(additionalWhereClause);
        query.replace(0, 6, "SELECT TOP " + batchHint + " ");
        logger.log(Level.FINE, "Using Custom Query[{0}]",
            additionalWhereClause);
      } else {
        query.append(additionalWhereClause);
      }
    }
    if (!checkpoint.isNull(JsonField.LAST_MODIFIED_TIME)) {
      query.append(getCheckpointClause(checkpoint, JsonField.LAST_MODIFIED_TIME,
              JsonField.UUID));
    }
    query.append(" ORDER BY ");
    query.append(PropertyNames.DATE_LAST_MODIFIED);
    query.append(",");
    query.append(PropertyNames.ID);
    return query.toString();
  }

  /**
   * Returns a query string for the checkpoint values.
   *
   * @param checkpoint the checkpoint
   * @param dateField the checkpoint date field
   * @param uuidField the checkpoint ID field
   * @return a query string
   * @throws RepositoryException if the checkpoint is uninitialized
   */
  @VisibleForTesting
  String getCheckpointClause(Checkpoint checkPoint, JsonField dateField,
      JsonField uuidField) throws RepositoryException {
    String c = FileUtil.getQueryTimeString(checkPoint.getString(dateField));
    String uuid = checkPoint.getString(uuidField);
    if (uuid.equals("")) {
      uuid = Id.ZERO_ID.toString();
    }
    logger.log(Level.FINE, "MakeCheckpointQueryString date: {0}", c);
    logger.log(Level.FINE, "MakeCheckpointQueryString ID: {0}", uuid);
    String whereClause = " AND (("
        + PropertyNames.DATE_LAST_MODIFIED + "={0} AND (''{1}''<"
        + PropertyNames.ID + ")) OR (" + PropertyNames.DATE_LAST_MODIFIED
        + ">{0}))";
    return MessageFormat.format(whereClause, new Object[] { c, uuid });
  }

  private class LocalDocument {
    private final Id guid;
    private final DocId docId;

    private final IDocument document;
    private final Permissions.Acl permissions;

    public LocalDocument(Id guid, DocId docId, Response response)
        throws IOException, RepositoryException {
      this.guid = guid;
      this.docId = docId;
      boolean pushAcls = connector.pushAcls();
      document = (IDocument) objectStore.fetchObject(ClassNames.DOCUMENT, guid,
          FileUtil.getDocumentPropertyFilter(connector.getIncludedMeta()));
      logger.log(Level.FINE, "Fetch document for DocId {0}", guid);
      String vsDocId = document.getVersionSeries().get_Id().toString();
      logger.log(Level.FINE, "VersionSeriesID for document is: {0}", vsDocId);
      if (!document.get_ActiveMarkings().isEmpty()) {
        logger.log(Level.FINE, "Document {0} has an active marking set - "
            + "ignoring ACL.", vsDocId);
        pushAcls = false;
      }
      if (pushAcls) {
        permissions = new Permissions(document.get_Permissions(),
            document.get_Owner()).getAcl();
        response.setAcl(getAcl());

        TreeMap<String, Acl> acls = new TreeMap<>();
        processInheritedPermissions(acls);
        for (Map.Entry<String, Acl> entry : acls.entrySet()) {
          logger.finest("Processing ACL document");
          response.putNamedResource(entry.getKey(), entry.getValue());
        }
      } else {
        permissions = null;
      }

      response.setContentType(document.get_MimeType());
      // TODO(jlacey): Use ValidatedUri. Use a percent encoder, or URI's
      // multi-argument constructors?
      response.setDisplayUrl(
          URI.create(connector.getWorkplaceDisplayUrl() +
              vsDocId.replace("{", "%7B").replace("}", "%7D")));
      response.setLastModified(document.get_DateLastModified());
      response.setSecure(!connector.isPublic());

      logger.log(Level.FINEST, "Getting content");
      if (hasAllowableSize()) {
        IOHelper.copyStream(document.getContent(), response.getOutputStream());
      }
    }

    private boolean hasAllowableSize() {
      Double value = document.get_ContentSize();
      if (value == null) {
        logger.log(Level.FINEST,
            "Send content to the GSA since the {0} value is null [DocId: {1}]",
            new Object[] {PropertyNames.CONTENT_SIZE, guid});
        return true;
      } else {
        double contentSize = value.doubleValue();
        if (contentSize > 0) {
          if (contentSize <= 2L * 1024 * 1024 * 1024) {
            logger.log(Level.FINEST, "{0} : {1}",
                new Object[] {PropertyNames.CONTENT_SIZE, contentSize});
            return true;
          } else {
            logger.log(Level.FINER,
                "{0} [{1}] exceeds the allowable size [DocId: {2}]",
                new Object[] {PropertyNames.CONTENT_SIZE, contentSize, guid});
            return false;
          }
        } else {
          logger.log(Level.FINEST, "{0} is empty [DocId: {1}]",
              new Object[] {PropertyNames.CONTENT_SIZE, guid});
          return false;
        }
      }
    }

    private Acl getAcl() {
      return createAcl(getParentFragment(), Acl.InheritanceType.LEAF_NODE,
          connector.getGoogleGlobalNamespace(),
          union(
              permissions.getAllowUsers(PermissionSource.SOURCE_DEFAULT),
              permissions.getAllowUsers(PermissionSource.SOURCE_DIRECT)),
          union(
              permissions.getDenyUsers(PermissionSource.SOURCE_DEFAULT),
              permissions.getDenyUsers(PermissionSource.SOURCE_DIRECT)),
          union(
              permissions.getAllowGroups(PermissionSource.SOURCE_DEFAULT),
              permissions.getAllowGroups(PermissionSource.SOURCE_DIRECT)),
          union(
              permissions.getDenyGroups(PermissionSource.SOURCE_DEFAULT),
              permissions.getDenyGroups(PermissionSource.SOURCE_DIRECT)));
    }

  public static final String SEC_POLICY_POSTFIX = "TMPL";
  public static final String SEC_FOLDER_POSTFIX = "FLDR";

    private String getParentFragment() {
      if (hasPermissions(PermissionSource.SOURCE_TEMPLATE)) {
        return SEC_POLICY_POSTFIX;
      } else if (hasPermissions(PermissionSource.SOURCE_PARENT)) {
        return SEC_FOLDER_POSTFIX;
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

    private List<UserPrincipal> getUserPrincipals(Set<String> names,
        String namespace) {
      ArrayList<UserPrincipal> list = new ArrayList<>();
      for (String name : names) {
        list.add(new UserPrincipal(FileUtil.convertDn(name), namespace));
      }
      return list;
    }

    private List<GroupPrincipal> getGroupPrincipals(Set<String> names,
        String namespace) {
      ArrayList<GroupPrincipal> list = new ArrayList<>();
      for (String name : names) {
        list.add(new GroupPrincipal(FileUtil.convertDn(name), namespace));
      }
      return list;
    }

    public List<Value> findProperty(String name) throws RepositoryException {
      ArrayList<Value> list = new ArrayList<>();
      document.getProperty(name, list);
      return list;
    }

    public Set<String> getPropertyNames() throws RepositoryException {
      Set<String> properties = new HashSet<String>();

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

    public void processInheritedPermissions(Map<String, Acl> acls) {
      logger.log(Level.FINEST, "Process inherited permissions for document: {0}",
          guid);

      // Send add request for adding ACLs inherited from parent folders.
      String secParentFragment = null;
      Acl folderAcl = createAcl(PermissionSource.SOURCE_PARENT, null);
      if (folderAcl != null) {
        logger.log(Level.FINEST, "Create ACL for folder {0}{1}",
            new Object[] {guid, SEC_FOLDER_POSTFIX});
        acls.put(SEC_FOLDER_POSTFIX, folderAcl);
        secParentFragment = SEC_FOLDER_POSTFIX;
      }

      // Send add request for adding ACLs inherited from security template.
      Acl secAcl = createAcl(PermissionSource.SOURCE_TEMPLATE,
          secParentFragment);
      if (secAcl != null) {
        logger.log(Level.FINEST,
            "Create ACL for security template {0}{1}",
            new Object[] {guid, SEC_POLICY_POSTFIX});
        acls.put(SEC_POLICY_POSTFIX, secAcl);
      }
    }

    private Acl createAcl(PermissionSource permSrc, String parentFragment) {
      Set<String> allowUsers = permissions.getAllowUsers(permSrc);
      Set<String> denyUsers = permissions.getDenyUsers(permSrc);
      Set<String> allowGroups = permissions.getAllowGroups(permSrc);
      Set<String> denyGroups = permissions.getDenyGroups(permSrc);
      if (allowUsers.isEmpty() && denyUsers.isEmpty()
              && allowGroups.isEmpty() && denyGroups.isEmpty()) {
        logger.log(Level.FINEST,
            "Document {0} does not have inherited permissions", guid);
        return null;
      } else {
        return createAcl(parentFragment, Acl.InheritanceType.CHILD_OVERRIDES,
            connector.getGoogleGlobalNamespace(), allowUsers, denyUsers,
            allowGroups, denyGroups);
      }
    }

    private Acl createAcl(String parentFragment,
        Acl.InheritanceType inheritanceType, String namespace,
        Set<String> allowUsers, Set<String> denyUsers,
        Set<String> allowGroups, Set<String> denyGroups) {
      Acl.Builder builder = new Acl.Builder();
      builder.setEverythingCaseInsensitive();
      builder.setInheritanceType(inheritanceType);
      builder.setPermitUsers(getUserPrincipals(allowUsers, namespace));
      builder.setDenyUsers(getUserPrincipals(denyUsers, namespace));
      builder.setPermitGroups(getGroupPrincipals(allowGroups, namespace));
      builder.setDenyGroups(getGroupPrincipals(denyGroups, namespace));
      if (parentFragment != null) {
        builder.setInheritFrom(docId, parentFragment);
      }
      Acl acl = builder.build();
      logger.log(Level.FINER, "ACL for {0}: {1}", new Object[] { guid, acl });
      return acl;
    }
  }
}
