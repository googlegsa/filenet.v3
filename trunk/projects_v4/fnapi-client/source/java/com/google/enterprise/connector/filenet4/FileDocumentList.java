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
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.TraversalContext;

import com.filenet.api.constants.DatabaseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDocumentList implements DocumentList {
  private static final Logger logger = 
      Logger.getLogger(FileDocumentList.class.getName());

  private static final long serialVersionUID = 1L;
  private final IObjectStore objectStore;
  private final DatabaseType databaseType;
  private final Iterator<? extends IBaseObject> objects;
  private final LinkedList<Document> acls;
  private final FileConnector connector;
  private final TraversalContext traversalContext;
  private Document fileDocument;
  private Date fileDocumentDate;
  private Date fileDocumentToDeleteDate;
  private Date fileDocumentToDeleteDocsDate;
  private IId docId;
  private Checkpoint lastCheckPoint;
  private IId docIdToDelete;
  private IId docIdToDeleteDocs;

  public FileDocumentList(IObjectSet objectSet,
      IObjectSet objectSetToDeleteDocs, IObjectSet objectSetToDelete,
      IObjectStore objectStore, FileConnector connector,
      TraversalContext traversalContext, Checkpoint checkPoint) {
    this.objectStore = objectStore;
    this.connector = connector;
    this.traversalContext = traversalContext;
    this.lastCheckPoint = checkPoint;

    this.databaseType = getDatabaseType(objectStore);
    this.objects = mergeAndSortObjects(objectSet, objectSetToDelete,
        objectSetToDeleteDocs);
    this.acls = new LinkedList<Document>();

    // Docs to Add
    logger.log(Level.INFO, "Number of new documents discovered: "
            + objectSet.getSize());

    // Docs to Delete
    logger.log(Level.INFO, "Number of new documents to be removed (Documents deleted from repository): "
            + objectSetToDelete.getSize());

    if (objectSetToDeleteDocs != null) {
      logger.info("Number of new documents to be removed (Documents "
          + "satisfying additional delete clause): "
          + objectSetToDeleteDocs.getSize());
    }
  }

  private Iterator<? extends IBaseObject> mergeAndSortObjects(
      IObjectSet objectSet, IObjectSet objectSetToDelete,
      IObjectSet objectSetToDeleteDocs) {
    int size = objectSet.getSize() + objectSetToDelete.getSize();
    if (objectSetToDeleteDocs != null) {
      size += objectSetToDeleteDocs.getSize();
    }
    List<IBaseObject> objectList = new ArrayList<IBaseObject>(size);

    // Adding documents, deletion events and custom deletion to the object list
    addToList(objectList, objectSet);
    addToList(objectList, objectSetToDelete);
    addCustomDeletionToList(objectList, objectSetToDeleteDocs);

    // Sort list by last modified time and ID.
    Collections.sort(objectList, new Comparator<IBaseObject>() {
        @Override public int compare(IBaseObject obj0, IBaseObject obj1) {
          try {
            int val = obj0.getModifyDate().compareTo(obj1.getModifyDate());
            if (val == 0) {
              val = obj0.get_Id().compareTo(obj1.get_Id(), databaseType);
            }
            return val;
          } catch (RepositoryDocumentException e) {
            logger.log(Level.WARNING, "Unable to compare time", e);
            return 0;
          }
        }
    });
    logger.log(Level.FINEST, "Total objects: {0}", objectList.size());

    return objectList.iterator();
  }

  /*
   * Helper method to retrieve database type
   */
  private DatabaseType getDatabaseType(IObjectStore os) {
    try {
      return os.get_DatabaseType();
    } catch (RepositoryException e) {
      logger.log(Level.WARNING,
          "Unable to retrieve database type from object store", e);
      return null;
    }
  }

  /*
   * Helper method to add objects to list.
   */
  private void addToList(List<IBaseObject> objectList, IObjectSet objectSet) {
    Iterator<? extends IBaseObject> iter = objectSet.getIterator();
    while (iter.hasNext()) {
      objectList.add(iter.next());
    }
  }

  /*
   * Helper method to add deleted objects returned from the custom query to
   * list.  It also wraps the deleted object using FileDeletionObject class.
   */
  private void addCustomDeletionToList(List<IBaseObject> objectList,
      IObjectSet objectSet) {
    if (objectSet != null) {
      Iterator<? extends IBaseObject> iter = objectSet.getIterator();
      while (iter.hasNext()) {
        objectList.add(new FileDeletionObject(iter.next()));
      }
    }
  }

  /***
   * The nextDocument method gets the next document from the document list
   * that the connector acquires from the FileNet repository.
   *
   * @return com.google.enterprise.connector.spi.Document
   */
  @Override
  public Document nextDocument() throws RepositoryException {
    logger.entering("FileDocumentList", "nextDocument()");

    fileDocument = null;
    if (objects.hasNext()) {
      IBaseObject object = objects.next();
      if (object.isDeletionEvent()) {
        fileDocumentToDeleteDate = object.getModifyDate();
        docIdToDelete = object.get_Id();
        if (object.isReleasedVersion()) {
          fileDocument = createDeleteDocument(object);
        } else {
          throw new SkippedDocumentException("Skip a deletion event [ID: "
              + docIdToDelete + "] of an unreleased document.");
        }
      } else if (object instanceof FileDeletionObject) {
        fileDocumentToDeleteDocsDate = object.getModifyDate();
        docIdToDeleteDocs = object.get_Id();
        if (object.isReleasedVersion()) {
          fileDocument = createDeleteDocument(object);
        } else {
          throw new SkippedDocumentException("Skip custom deletion [ID: "
              + docIdToDeleteDocs + "] because document is not a released "
              + "version.");
        }
      } else {
        fileDocumentDate = object.getModifyDate();
        docId = object.get_Id();
        fileDocument = createAddDocument(object);
      }
    } else {
      logger.finest("Processing ACL document");
      fileDocument = acls.pollFirst();
    }
    return fileDocument;
  }

  /*
   * Helper method to create add document.
   */
  private Document createAddDocument(IBaseObject object)
      throws RepositoryException {
    IId id = object.get_Id();
    logger.log(Level.FINEST, "Add document [ID: {0}]", id);
    FileDocument doc =
        new FileDocument(id, objectStore, connector, traversalContext);
    doc.processInheritedPermissions(acls);
    return doc;
  }

  /*
   * Helper method to create delete document.
   */
  private Document createDeleteDocument(IBaseObject object)
      throws RepositoryDocumentException {
    IId id = object.get_Id();
    IId versionSeriesId = object.getVersionSeriesId();
    logger.log(Level.FINEST, "Delete document [ID: {0}, VersionSeriesID: {1}]",
        new Object[] {id, versionSeriesId});
    return new FileDeleteDocument(versionSeriesId, object.getModifyDate());
  }

  /***
   * Checkpoint method indicates the current position within the document
   * list, that is where to start a resumeTraversal method. The checkpoint
   * method returns information that allows the resumeTraversal method to
   * resume on the document that would have been returned by the next call to
   * the nextDocument method.
   *
   * @return String checkPoint - information that allows the resumeTraversal
   *         method to resume on the document
   */
  @Override
  public String checkpoint() throws RepositoryException {
    logger.log(Level.FINEST, "Last checkpoint: {0}", lastCheckPoint);

    lastCheckPoint.setTimeAndUuid(
        JsonField.LAST_MODIFIED_TIME, fileDocumentDate,
        JsonField.UUID, docId);
    lastCheckPoint.setTimeAndUuid(
        JsonField.LAST_CUSTOM_DELETION_TIME, fileDocumentToDeleteDocsDate,
        JsonField.UUID_CUSTOM_DELETED_DOC, docIdToDeleteDocs);
    lastCheckPoint.setTimeAndUuid(
        JsonField.LAST_DELETION_EVENT_TIME, fileDocumentToDeleteDate,
        JsonField.UUID_DELETION_EVENT, docIdToDelete);
    return lastCheckPoint.toString();
  }
}
