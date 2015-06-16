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
import com.google.enterprise.connector.filenet4.api.IBaseObject;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.TraversalContext;

import com.filenet.api.constants.DatabaseType;
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
  private final Iterator<ObjectWrapper> objects;
  private final LinkedList<Document> acls;

  private Date fileDocumentDate;
  private Date fileDocumentToDeleteDate;
  private Date fileDocumentToDeleteDocsDate;
  private Id docId;
  private Id docIdToDelete;
  private Id docIdToDeleteDocs;

  public FileDocumentList(IObjectSet objectSet,
      IObjectSet objectSetToDeleteDocs, IObjectSet objectSetToDelete,
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

  private Iterator<ObjectWrapper> mergeAndSortObjects(
      IObjectSet objectSet, IObjectSet objectSetToDelete,
      IObjectSet objectSetToDeleteDocs) {
    List<ObjectWrapper> objectList = new ArrayList<>();

    // Adding documents, deletion events and custom deletion to the object list
    addToList(objectList, objectSet, ObjectType.ADD);
    addToList(objectList, objectSetToDelete, ObjectType.DELETE);
    addToList(objectList, objectSetToDeleteDocs, ObjectType.DELETE);

    // Sort list by last modified time and ID.
    Collections.sort(objectList);
    logger.log(Level.INFO, "Number of documents to add, update, or delete: {0}",
        objectList.size());

    return objectList.iterator();
  }

  /** Adds IBaseObjects from the object set to the list. */
  private void addToList(List<ObjectWrapper> objectList, IObjectSet objectSet,
      ObjectType type) {
    Iterator<?> iter = objectSet.iterator();
    while (iter.hasNext()) {
      objectList.add(new ObjectWrapper((IBaseObject) iter.next(), type));
    }
  }

  public static enum ObjectType { ADD, DELETE };

  /**
   * A {@code Comparable} wrapper on IBaseObject that includes the type,
   * so that added and updated documents can be distinguished from the
   * deleted documents returned by the additional delete query.
   *
   * Note: this class has a natural ordering that is inconsistent with equals.
   */
  private class ObjectWrapper implements Comparable<ObjectWrapper> {
    private final IBaseObject object;
    private final ObjectType type;

    ObjectWrapper(IBaseObject object, ObjectType type) {
      this.object = object;
      this.type = type;
    }

    @Override
    public int compareTo(ObjectWrapper wrapper) {
      IBaseObject other = wrapper.object;
      try {
        int val = object.getModifyDate().compareTo(other.getModifyDate());
        if (val == 0) {
          val = object.get_Id().compareTo(other.get_Id(), databaseType);
        }
        return val;
      } catch (RepositoryDocumentException e) {
        logger.log(Level.WARNING, "Unable to compare time", e);
        return 0;
      }
    }
  }

  @Override
  public Document nextDocument() throws RepositoryException {
    logger.entering("FileDocumentList", "nextDocument()");

    Document fileDocument;
    if (objects.hasNext()) {
      ObjectWrapper wrapper = objects.next();
      IBaseObject object = wrapper.object;
      if (wrapper.type == ObjectType.DELETE && object.isDeletionEvent()) {
        fileDocumentToDeleteDate = object.getModifyDate();
        docIdToDelete = object.get_Id();
        if (object.isReleasedVersion()) {
          fileDocument = createDeleteDocument(object);
        } else {
          throw new SkippedDocumentException("Skip a deletion event [ID: "
              + docIdToDelete + "] of an unreleased document.");
        }
      } else if (wrapper.type == ObjectType.DELETE) {
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

  private Document createAddDocument(IBaseObject object)
      throws RepositoryException {
    Id id = object.get_Id();
    logger.log(Level.FINEST, "Add document [ID: {0}]", id);
    FileDocument doc =
        new FileDocument(id, objectStore, connector, traversalContext);
    doc.processInheritedPermissions(acls);
    return doc;
  }

  private Document createDeleteDocument(IBaseObject object)
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
