// Copyright 2007-2010 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.TraversalContext;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.util.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDocumentList implements DocumentList {
  private static final Logger logger = 
      Logger.getLogger(FileDocumentList.class.getName());

  private final IObjectStore objectStore;
  private final FileConnector connector;
  private final TraversalContext traversalContext;
  private final Checkpoint checkpoint;

  private final DatabaseType databaseType;
  private final Iterator<SearchObject> objects;
  private final LinkedList<Document> acls;

  private Date fileDocumentDate;
  private Date fileDocumentToDeleteDate;
  private Date fileDocumentToDeleteDocsDate;
  private Id docId;
  private Id docIdToDelete;
  private Id docIdToDeleteDocs;

  public FileDocumentList(IndependentObjectSet objectSet,
      IndependentObjectSet objectSetToDeleteDocs,
      IndependentObjectSet objectSetToDelete,
      IObjectStore objectStore, FileConnector connector,
      TraversalContext traversalContext, Checkpoint checkpoint) {
    this.objectStore = objectStore;
    this.connector = connector;
    this.traversalContext = traversalContext;
    this.checkpoint = checkpoint;

    this.databaseType = getDatabaseType(objectStore);
    this.objects = mergeAndSortObjects(objectSet, objectSetToDelete,
        objectSetToDeleteDocs);
    this.acls = new LinkedList<Document>();
  }

  private DatabaseType getDatabaseType(IObjectStore os) {
    try {
      return os.get_DatabaseType();
    } catch (RepositoryException e) {
      logger.log(Level.WARNING,
          "Unable to retrieve database type from object store", e);
      return null;
    }
  }

  /** Sort the objects by modify date and ID. */
  private Iterator<SearchObject> mergeAndSortObjects(
      IndependentObjectSet objectSet, IndependentObjectSet objectSetToDelete,
      IndependentObjectSet objectSetToDeleteDocs) {
    List<SearchObject> objectList = new ArrayList<>();

    // Adding documents, deletion events and custom deletion to the object list
    addToList(objectList, objectSet, SearchObject.Type.ADD);
    addToList(objectList, objectSetToDelete, SearchObject.Type.DELETION_EVENT);
    addToList(objectList, objectSetToDeleteDocs,
        SearchObject.Type.CUSTOM_DELETE);

    // Sort list by last modified time and ID. We depend on this being
    // a stable sort, because an updated document may appear in both
    // objectSet and objectSetToDeleteDocs with the same modified date.
    Collections.sort(objectList);
    logger.log(Level.INFO, "Number of documents to add, update, or delete: {0}",
        objectList.size());

    return objectList.iterator();
  }

  /**
   * Wraps elements of the object set as SearchObjects and adds them
   * to the list.
   */
  private void addToList(List<SearchObject> objectList,
      IndependentObjectSet objectSet, SearchObject.Type searchType) {
    Iterator<?> iter = objectSet.iterator();
    while (iter.hasNext()) {
      objectList.add(new SearchObject((IndependentObject) iter.next(),
              databaseType, searchType));
    }
  }

  @Override
  public Document nextDocument() throws RepositoryException {
    logger.entering("FileDocumentList", "nextDocument()");

    Document fileDocument;
    if (objects.hasNext()) {
      SearchObject object = objects.next();
      switch (object.getType()) {
        case DELETION_EVENT:
          fileDocumentToDeleteDate = object.getModifyDate();
          docIdToDelete = object.get_Id();
          if (object.isReleasedVersion()) {
            fileDocument = createDeleteDocument(object);
          } else {
            throw new SkippedDocumentException("Skip a deletion event [ID: "
                + docIdToDelete + "] of an unreleased document.");
          }
          break;
        case CUSTOM_DELETE:
          fileDocumentToDeleteDocsDate = object.getModifyDate();
          docIdToDeleteDocs = object.get_Id();
          if (object.isReleasedVersion()) {
            fileDocument = createDeleteDocument(object);
          } else {
            throw new SkippedDocumentException("Skip custom deletion [ID: "
                + docIdToDeleteDocs + "] because document is not a released "
                + "version.");
          }
          break;
        case ADD:
          fileDocumentDate = object.getModifyDate();
          docId = object.get_Id();
          fileDocument = createAddDocument(object);
          break;
        default:
          throw new NullPointerException();
      }
    } else {
      logger.finest("Processing ACL document");
      fileDocument = acls.pollFirst();
    }
    return fileDocument;
  }

  private Document createAddDocument(SearchObject object)
      throws RepositoryException {
    Id id = object.get_Id();
    logger.log(Level.FINEST, "Add document [ID: {0}]", id);
    FileDocument doc =
        new FileDocument(id, objectStore, connector, traversalContext);
    doc.processInheritedPermissions(acls);
    return doc;
  }

  private Document createDeleteDocument(SearchObject object)
      throws RepositoryDocumentException {
    Id id = object.get_Id();
    Id versionSeriesId = object.getVersionSeriesId();
    logger.log(Level.FINEST, "Delete document [ID: {0}, VersionSeriesID: {1}]",
        new Object[] {id, versionSeriesId});
    return new FileDeleteDocument(versionSeriesId, object.getModifyDate());
  }

  @Override
  public String checkpoint() throws RepositoryException {
    checkpoint.setTimeAndUuid(
        JsonField.LAST_MODIFIED_TIME, fileDocumentDate,
        JsonField.UUID, docId);
    checkpoint.setTimeAndUuid(
        JsonField.LAST_CUSTOM_DELETION_TIME, fileDocumentToDeleteDocsDate,
        JsonField.UUID_CUSTOM_DELETED_DOC, docIdToDeleteDocs);
    checkpoint.setTimeAndUuid(
        JsonField.LAST_DELETION_EVENT_TIME, fileDocumentToDeleteDate,
        JsonField.UUID_DELETION_EVENT, docIdToDelete);
    return checkpoint.toString();
  }
}
