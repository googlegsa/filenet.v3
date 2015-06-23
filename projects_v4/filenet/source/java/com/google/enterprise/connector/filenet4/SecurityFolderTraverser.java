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
import com.google.enterprise.connector.filenet4.api.SearchWrapper;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Folder;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class SecurityFolderTraverser implements Traverser {
  private static final Logger LOGGER =
      Logger.getLogger(SecurityFolderTraverser.class.getName());

  private static final String FOLDER_QUERY_WITH_UUID =
      "SELECT TOP {0,number,#} * FROM '" + GuidConstants.Class_Folder
      + "' WHERE ((" + PropertyNames.DATE_LAST_MODIFIED + " = {1}) AND ("
      + PropertyNames.ID + " > {2})) OR ("
      + PropertyNames.DATE_LAST_MODIFIED + " > {1}) ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private static final String FOLDER_QUERY_WITHOUT_UUID =
      "SELECT TOP {0,number,#} * FROM '" + GuidConstants.Class_Folder
      + "' WHERE " + PropertyNames.DATE_LAST_MODIFIED + " > {1} ORDER BY "
      + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  private final IConnection connection;
  private final IObjectFactory objectFactory;
  private final IObjectStore os;
  private final FileConnector connector;

  private int batchHint = 1000;

  public SecurityFolderTraverser(IConnection connection,
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
  public void setBatchHint(int batchHint) throws RepositoryException {
    this.batchHint = batchHint;
  }

  @Override
  public DocumentList getDocumentList(Checkpoint checkpoint)
      throws RepositoryException {
    LOGGER.fine("Searching for documents in updated folders");
    connection.refreshSUserContext();
    try {
      LinkedList<AclDocument> acls = searchDocs(checkpoint);
      if (acls.size() > 0) {
        return new SecurityFolderDocumentList(acls, checkpoint);
      } else {
        return null;
      }
    } catch (EngineRuntimeException e) {
      throw new RepositoryException(e);
    }
  }

  private String getCheckpointValue(Checkpoint checkpoint,
      JsonField field) {
    try {
      return checkpoint.getString(field);
    } catch (RepositoryException e) {
      LOGGER.log(Level.FINEST,
          "Failed to lookup " + field + " field in checkpoint");
      return null;
    }
  }

  private LinkedList<AclDocument> searchDocs(Checkpoint checkpoint)
      throws RepositoryException {
    LinkedList<AclDocument> aclDocs = new LinkedList<AclDocument>();
    SearchWrapper searcher = objectFactory.getSearch(os);
    IndependentObjectSet folderSet = searcher.fetchObjects(
        getQuery(checkpoint), batchHint, SearchWrapper.noFilter,
        SearchWrapper.ALL_ROWS);
    Iterator<?> folderIter = folderSet.iterator();
    while (folderIter.hasNext()) {
      Folder folder = (Folder) folderIter.next();
      addAllDescendantDocumentAcls(aclDocs, folder,
          folder.get_DateLastModified(), folder.get_Id());
    }
    return aclDocs;
  }

  /**
   * Recursively adds an AclDocument for each descendant Document of the
   * Folder to the list.
   *
   * Changing the ACL on a folder changes the ACL on all its descendant
   * subfolders, yet the last modified time of those subfolders does not
   * get updated, so they don't show up in the query of modified folders.
   * Therefore the SecurityFolderTraverser must recursively update the
   * ACLs for all descendant subfolders. However, the connector does not
   * send folder ACLs to the GSA. It sends a copy of them with the docs
   * that inherit from those security folders. Therefore, we resend ACLs
   * for all documents who directly or indirectly inherit from the original
   * folder whose ACL changed.
   */
  private void addAllDescendantDocumentAcls(LinkedList<AclDocument> aclDocs,
      Folder folder, Date checkpointDate, Id checkpointId)
      throws RepositoryException {
    // First update the ACLs for all documents in this folder.
    DocumentSet docSet = folder.get_ContainedDocuments();
    int count = 0;
    Iterator<?> docIter = docSet.iterator();
    while (docIter.hasNext()) {
      // Document collides with the SPI class of the same name.
      com.filenet.api.core.Document doc =
          (com.filenet.api.core.Document) docIter.next();
      count++;
      Permissions permissions = new Permissions(doc.get_Permissions());
      AclDocument aclDoc = new AclDocument(
          doc.get_Id().toString() + AclDocument.SEC_FOLDER_POSTFIX, null,
          AclInheritanceType.CHILD_OVERRIDES,
          connector.getGoogleGlobalNamespace(),
          permissions.getAllowUsers(PermissionSource.SOURCE_PARENT),
          permissions.getDenyUsers(PermissionSource.SOURCE_PARENT),
          permissions.getAllowGroups(PermissionSource.SOURCE_PARENT),
          permissions.getDenyGroups(PermissionSource.SOURCE_PARENT));
      aclDoc.setCheckpointLastModified(checkpointDate);
      aclDoc.setCheckpointLastUuid(checkpointId);
      aclDocs.add(aclDoc);
    }
    LOGGER.log(Level.FINEST, "Found {0} documents under {1} folder [{2}]",
        new Object[] {count, folder.get_FolderName(), folder.get_Id()});

    // Now do the same for documents in all descendant folders.
    FolderSet folderSet = folder.get_SubFolders();
    Iterator<?> folderIter = folderSet.iterator();
    while (folderIter.hasNext()) {
      addAllDescendantDocumentAcls(aclDocs, (Folder) folderIter.next(),
          checkpointDate, checkpointId);
    }
  }

  private String getQuery(Checkpoint checkpoint) {
    String checkpointTime = getLastModified(checkpoint);
    String timeStr = FileUtil.getQueryTimeString(checkpointTime);
    String checkpointUuid =
        getCheckpointValue(checkpoint, JsonField.UUID_FOLDER);

    if (Strings.isNullOrEmpty(checkpointUuid)) {
      return MessageFormat.format(FOLDER_QUERY_WITHOUT_UUID,
          new Object[] {batchHint, timeStr});
    } else {
      return MessageFormat.format(FOLDER_QUERY_WITH_UUID,
          new Object[] {batchHint, timeStr, checkpointUuid});
    }
  }

  private String getLastModified(Checkpoint checkpoint) {
    String lastModified =
        getCheckpointValue(checkpoint, JsonField.LAST_FOLDER_TIME);
    if (lastModified == null) {
      lastModified = 
          getCheckpointValue(checkpoint, JsonField.LAST_MODIFIED_TIME);
      if (lastModified == null) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        lastModified = Value.calendarToIso8601(cal);
        LOGGER.log(Level.FINEST, "Checkpoint does not contain folder or "
            + "document last modified time, use current time [{0}] to search"
            + " for folder updates", lastModified);
      } else {
        LOGGER.log(Level.FINEST, "Checkpoint does not contain folder last "
            + "modified time, use document last modified time [{0}] to search"
            + " for folder updates", lastModified);
      }
    }
    return lastModified;
  }

  private static class SecurityFolderDocumentList implements DocumentList {
    private final LinkedList<AclDocument> acls;
    private final Checkpoint checkpoint;

    private Date folderLastModified;
    private Id folderLastUuid;

    public SecurityFolderDocumentList(LinkedList<AclDocument> acls, 
        Checkpoint checkpoint) {
      this.acls = acls;
      this.checkpoint = checkpoint;
    }

    @Override
    public Document nextDocument() throws RepositoryException {
      AclDocument aclDoc = acls.poll();
      if (aclDoc != null) {
        folderLastUuid = aclDoc.getCheckpointLastUuid();
        folderLastModified = aclDoc.getCheckpointLastModified();
      }      
      return aclDoc;
    }

    @Override
    public String checkpoint() throws RepositoryException {
      if (folderLastModified != null && folderLastUuid != null) {
        checkpoint.setTimeAndUuid(
            JsonField.LAST_FOLDER_TIME, folderLastModified,
            JsonField.UUID_FOLDER, folderLastUuid);
      }
      return checkpoint.toString();
    }
  }
}
