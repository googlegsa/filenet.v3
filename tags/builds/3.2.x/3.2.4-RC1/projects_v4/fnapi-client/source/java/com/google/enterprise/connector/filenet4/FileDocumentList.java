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

import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.util.Id;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDocumentList implements DocumentList {
  private static final Logger logger = 
      Logger.getLogger(FileDocumentList.class.getName());

  private static final long serialVersionUID = 1L;
  private final IObjectStore objectStore;
  private final DatabaseType databaseType;
  private final Iterator<? extends IBaseObject> objects;
  private FileDocument fileDocument;
  private Date fileDocumentDate;
  private Date fileDocumentToDeleteDate;
  private Date fileDocumentToDeleteDocsDate;
  private String docId = "";
  private String displayUrl;
  private String lastCheckPoint;
  private String dateFirstPush;
  private String docIdToDelete = "";
  private String docIdToDeleteDocs = "";
  private boolean isPublic;
  private Set<String> included_meta;
  private Set<String> excluded_meta;
  private final String globalNamespace;

  public FileDocumentList(IObjectSet objectSet,
      IObjectSet objectSetToDeleteDocs, IObjectSet objectSetToDelete,
      IObjectStore objectStore, boolean isPublic, String displayUrl,
      Set<String> included_meta, Set<String> excluded_meta,
      String dateFirstPush, String checkPoint, String globalNamespace) {
    this.objectStore = objectStore;
    this.isPublic = isPublic;
    this.displayUrl = displayUrl;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.dateFirstPush = dateFirstPush;
    this.lastCheckPoint = checkPoint;
    this.globalNamespace = globalNamespace;

    this.databaseType = getDatabaseType(objectStore);
    this.objects = mergeAndSortObjects(objectSet, objectSetToDelete,
        objectSetToDeleteDocs);

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

  public FileDocumentList(IObjectSet objectSet, IObjectSet objectSetToDelete,
      IObjectStore objectStore, boolean isPublic, String displayUrl,
      Set<String> included_meta, Set<String> excluded_meta,
      String dateFirstPush, String checkPoint, String globalNamespace) {
    this.objectStore = objectStore;
    this.isPublic = isPublic;
    this.displayUrl = displayUrl;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.dateFirstPush = dateFirstPush;
    this.lastCheckPoint = checkPoint;
    this.globalNamespace = globalNamespace;

    this.databaseType = getDatabaseType(objectStore);
    this.objects = mergeAndSortObjects(objectSet, objectSetToDelete, null);

    // Docs to Add
    logger.log(Level.INFO, "Number of new documents discovered: "
            + objectSet.getSize());

    // Docs to Delete
    logger.log(Level.INFO, "Number of new documents to be removed: (Documents deleted from repository) "
            + objectSetToDelete.getSize());

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
              // TODO(tdnguyen): Refactor IBaseObject.getId() to return IId
              // interface instead of a string.
              Id id0 = new Id(obj0.getId());
              Id id1 = new Id(obj1.getId());
              val = id0.compareTo(id1, databaseType);
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
  public com.google.enterprise.connector.spi.Document nextDocument()
          throws RepositoryDocumentException {
    logger.entering("FileDocumentList", "nextDocument()");

    fileDocument = null;
    if (objects.hasNext()) {
      IBaseObject object = objects.next();
      if (object.isDeletionEvent()) {
        fileDocumentToDeleteDate = object.getModifyDate();
        docIdToDelete = object.getId();
        if (object.isReleasedVersion()) {
          fileDocument = createDeleteDocument(object);
        } else {
          throw new SkippedDocumentException("Skip a deletion event [ID: "
              + docIdToDelete + "] of an unreleased document.");
        }
      } else if (object instanceof FileDeletionObject) {
        fileDocumentToDeleteDocsDate = object.getModifyDate();
        docIdToDeleteDocs = object.getId();
        if (object.isReleasedVersion()) {
          fileDocument = createDeleteDocument(object);
        } else {
          throw new SkippedDocumentException("Skip custom deletion [ID: "
              + docIdToDeleteDocs + "] because document is not a released "
              + "version.");
        }
      } else {
        fileDocumentDate = object.getModifyDate();
        docId = object.getId();
        fileDocument = createAddDocument(object);
      }
    }
    return fileDocument;
  }

  /*
   * Helper method to create add document.
   */
  private FileDocument createAddDocument(IBaseObject object)
      throws RepositoryDocumentException {
    String id = object.getId();
    if (!Strings.isNullOrEmpty(id)) {
      logger.log(Level.FINEST, "Add document [ID: {0}]", id);
      return new FileDocument(id, object.getModifyDate(), objectStore, isPublic,
          displayUrl, included_meta, excluded_meta,
          SpiConstants.ActionType.ADD);
    } else {
      logger.log(Level.FINEST, "Failed to add document due to missing info"
          + "[ID: {0}, Last modified time: {1}]",
          new Object[] {id, object.getModifyDate()});
      return null;
    }
  }

  /*
   * Helper method to create delete document.
   */
  private FileDocument createDeleteDocument(IBaseObject object)
      throws RepositoryDocumentException {
    String id = object.getId();
    String versionSeriesId = object.getVersionSeriesId();
    if (!Strings.isNullOrEmpty(id) && !Strings.isNullOrEmpty(versionSeriesId)) {
      logger.log(Level.FINEST,
          "Delete document [ID: {0}, VersionSeriesID: {1}]",
          new Object[] {id, versionSeriesId});
      return new FileDocument(id, versionSeriesId, object.getModifyDate(),
          objectStore, isPublic, displayUrl, included_meta, excluded_meta,
          SpiConstants.ActionType.DELETE, globalNamespace);
    } else {
      logger.log(Level.FINEST, "Failed to delete document due to missing info"
          + "[ID: {0}, Last modified time: {1}]",
          new Object[] {id, object.getModifyDate()});
      return null;
    }
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
  public String checkpoint() throws RepositoryException {
    logger.log(Level.FINEST, "Last checkpoint: {0}", lastCheckPoint);

    JSONObject jo;
    if (lastCheckPoint == null) {
      jo = new JSONObject();
    } else {
      try {
        jo = new JSONObject(lastCheckPoint);
      } catch (JSONException e) {
        throw new RepositoryException(
            "Unable to initialize a JSON object for the checkpoint", e);
      }
    }
    setCheckpointTimeAndUuid(JsonField.LAST_MODIFIED_TIME, fileDocumentDate,
        docId, JsonField.UUID, jo);
    setCheckpointTimeAndUuid(JsonField.LAST_CUSTOM_DELETION_TIME,
        fileDocumentToDeleteDocsDate, docIdToDeleteDocs,
        JsonField.UUID_CUSTOM_DELETED_DOC, jo);
    setCheckpointTimeAndUuid(JsonField.LAST_DELETION_EVENT_TIME,
        fileDocumentToDeleteDate, docIdToDelete,
        JsonField.UUID_DELETION_EVENT, jo);
    return jo.toString();
  }

  /*
   * Helper method to compute the checkpoint date and UUID value.
   */
  private void setCheckpointTimeAndUuid(JsonField jsonDateField,
      Date nextCheckpointDate, String uuid, JsonField jsonUuidField,
      JSONObject jo) throws RepositoryException {
    Calendar cal = Calendar.getInstance();
    String dateString;
    try {
      if (nextCheckpointDate == null) {
        if (jo.isNull(jsonDateField.name())) {
          dateString = Value.calendarToIso8601(cal);
        } else {
          dateString = jo.getString(jsonDateField.name());
        }
      } else {
        cal.setTime(nextCheckpointDate);
        dateString = Value.calendarToIso8601(cal);
      }
      if (uuid == null && !jo.isNull(jsonUuidField.name())) {
        uuid = jo.getString(jsonUuidField.name());
      }
      jo.put(jsonUuidField.toString(), uuid);
      jo.put(jsonDateField.toString(), dateString);
      logger.log(Level.FINE, "Set new checkpoint for {0} field to {1}, "
          + "{2} field to {3}", new Object[] {
              jsonDateField.toString(), dateString,
              jsonUuidField.toString(), uuid});
    } catch (JSONException e) {
      throw new RepositoryException("Failed to set JSON values for fields: "
          + jsonDateField.toString() + " or " + jsonUuidField.toString(), e);
    }
  }
}
