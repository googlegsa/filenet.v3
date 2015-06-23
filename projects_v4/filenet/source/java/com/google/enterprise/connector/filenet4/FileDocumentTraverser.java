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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.SearchWrapper;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.exception.EngineRuntimeException;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for: 1. Construction of FileNet SQL queries for adding and
 * deleting index of documents to GSA. 2. Execution of the SQL query constructed
 * in step 1. 3. Retrieve the results of step 2 and wrap it in DocumentList
 */
public class FileDocumentTraverser implements Traverser {
  private static final Logger LOGGER =
      Logger.getLogger(FileDocumentTraverser.class.getName());

  private static final String tableName = "Document";

  private static final String ORDER_BY =
      " ORDER BY " + PropertyNames.DATE_LAST_MODIFIED + "," + PropertyNames.ID;

  @VisibleForTesting
  static final String WHERE_CLAUSE = " AND (("
      + PropertyNames.DATE_LAST_MODIFIED + "={0} AND (''{1}''<"
      + PropertyNames.ID + ")) OR (" + PropertyNames.DATE_LAST_MODIFIED
      + ">{0}))";

  @VisibleForTesting
  static final String WHERE_CLAUSE_ONLY_DATE = " AND (("
          + PropertyNames.DATE_LAST_MODIFIED + ">={0}))";

  private static final String ORDER_BY_TO_DELETE =
      " ORDER BY " + PropertyNames.DATE_CREATED + "," + PropertyNames.ID;

  @VisibleForTesting
  static final String WHERE_CLAUSE_TO_DELETE = " WHERE (("
          + PropertyNames.DATE_CREATED + "={0} AND (''{1}''<"
          + PropertyNames.ID + ")) OR (" + PropertyNames.DATE_CREATED
          + ">{0}))";

  @VisibleForTesting
  static final String WHERE_CLAUSE_TO_DELETE_ONLY_DATE = " WHERE ("
          + PropertyNames.DATE_CREATED + ">={0})";

  @VisibleForTesting
  static final String WHERE_CLAUSE_TO_DELETE_DOCS = " AND ((("
          + PropertyNames.DATE_LAST_MODIFIED + "={0}  AND (''{1}''<"
          + PropertyNames.ID
          + "))OR ("
          + PropertyNames.DATE_LAST_MODIFIED + ">{0})))";

  @VisibleForTesting
  static final String WHERE_CLAUSE_TO_DELETE_DOCS_ONLY_DATE = " AND ("
          + PropertyNames.DATE_LAST_MODIFIED + ">={0})";

  private final IConnection connection;
  private final IObjectFactory fileObjectFactory;
  private final IObjectStore objectStore;
  private final FileConnector connector;

  private TraversalContext traversalContext;
  private int batchHint = 1000;

  public FileDocumentTraverser(IConnection connection,
      IObjectFactory fileObjectFactory, IObjectStore objectStore,
      FileConnector fileConnector) {
    this.connection = connection;
    this.fileObjectFactory = fileObjectFactory;
    this.objectStore = objectStore;
    this.connector = fileConnector;
  }

  @Override
  public void setTraversalContext(TraversalContext traversalContext) {
    this.traversalContext = traversalContext;
  }

  /**
   * To set BatchHint for traversal.
   */
  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
    this.batchHint = batchHint;
  }

  @Override
  public DocumentList getDocumentList(Checkpoint checkPoint)
      throws RepositoryException {
    connection.refreshSUserContext();
    LOGGER.log(Level.FINE, "Target ObjectStore is: {0}", objectStore);

    SearchWrapper search = fileObjectFactory.getSearch(objectStore);

    try {
      // to add
      String query = buildQueryString(checkPoint);
      LOGGER.log(Level.FINE, "Query for added or updated documents: {0}",
          query);
      IndependentObjectSet objectSet = search.fetchObjects(query, batchHint,
          SearchWrapper.dereferenceObjects, SearchWrapper.ALL_ROWS);
      LOGGER.fine((objectSet.isEmpty()) ? "Found no documents to add or update"
          : "Found documents to add or update");

      // to delete for deleted documents
      String queryStringToDelete = buildQueryToDelete(checkPoint);
      LOGGER.log(Level.FINE, "Query for deleted documents: {0}",
          queryStringToDelete);
      IndependentObjectSet objectSetToDelete = search.fetchObjects(
          queryStringToDelete, batchHint, SearchWrapper.dereferenceObjects,
          SearchWrapper.ALL_ROWS);
      LOGGER.fine((objectSetToDelete.isEmpty()) ? "Found no documents to delete"
          : "Found documents to delete");

      // to delete for additional delete clause
      IndependentObjectSet objectSetToDeleteDocs;
      if (Strings.isNullOrEmpty(connector.getDeleteAdditionalWhereClause())) {
        objectSetToDeleteDocs = new EmptyObjectSet();
      } else {
        String queryStringToDeleteDocs = buildQueryStringToDeleteDocs(
            checkPoint, connector.getDeleteAdditionalWhereClause());
        LOGGER.log(Level.FINE,
            "Query for documents satisfying the delete WHERE clause: {0}",
            queryStringToDeleteDocs);
        objectSetToDeleteDocs = search.fetchObjects(queryStringToDeleteDocs,
            batchHint, SearchWrapper.dereferenceObjects,
            SearchWrapper.ALL_ROWS);
        LOGGER.fine((objectSetToDeleteDocs.isEmpty())
            ? "Found no documents to delete using WHERE clause"
            : "Found documents to delete using WHERE clause");
      }

      if (!objectSet.isEmpty() || !objectSetToDeleteDocs.isEmpty()
          || !objectSetToDelete.isEmpty()) {
        return new FileDocumentList(objectSet, objectSetToDeleteDocs,
            objectSetToDelete, objectStore, connector, traversalContext,
            checkPoint);
      } else {
        return null;
      }
    } catch (EngineRuntimeException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * To construct FileNet query to fetch documents from FileNet repository
   * considering additional delete where clause specified as connector
   * configuration and the previously remembered checkpoint to indicate where
   * to resume acquiring documents from the FileNet repository to send delete
   * feed.
   *
   * @param checkpoint
   * @return
   * @throws RepositoryException
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
    query.append(tableName);
    query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

    String additionalWhereClause = connector.getAdditionalWhereClause();
    if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
      if ((additionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
        query = new StringBuilder(additionalWhereClause);
        query.replace(0, 6, "SELECT TOP " + batchHint + " ");
        LOGGER.log(Level.FINE, "Using Custom Query[{0}]",
            additionalWhereClause);
      } else {
        query.append(additionalWhereClause);
      }
    }
    if (!checkpoint.isNull(JsonField.LAST_MODIFIED_TIME)) {
      query.append(getCheckpointClause(checkpoint, JsonField.LAST_MODIFIED_TIME,
              JsonField.UUID, WHERE_CLAUSE, WHERE_CLAUSE_ONLY_DATE));
    }
    query.append(ORDER_BY);
    return query.toString();
  }

  /**
   * Builds the query to send delete feeds to GSA based on the Additional
   * Delete clause set as Connector Configuration.
   */
  private String buildQueryStringToDeleteDocs(Checkpoint checkpoint,
      String deleteadditionalWhereClause) throws RepositoryException {
    StringBuilder query = new StringBuilder("SELECT TOP ");
    query.append(batchHint);
    query.append(" ");
    query.append(PropertyNames.ID);
    query.append(",");
    query.append(PropertyNames.DATE_LAST_MODIFIED);
    query.append(" FROM ");
    query.append(tableName);
    query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

    if ((deleteadditionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
      query = new StringBuilder(deleteadditionalWhereClause);
      query.replace(0, 6, "SELECT TOP " + batchHint + " ");
      LOGGER.log(Level.FINE, "Using Custom Query[{0}]",
          deleteadditionalWhereClause);
    } else {
      query.append(deleteadditionalWhereClause);
    }
    if (!checkpoint.isNull(JsonField.LAST_CUSTOM_DELETION_TIME)) {
      query.append(getCheckpointClause(checkpoint,
              JsonField.LAST_CUSTOM_DELETION_TIME,
              JsonField.UUID_CUSTOM_DELETED_DOC, WHERE_CLAUSE_TO_DELETE_DOCS,
              WHERE_CLAUSE_TO_DELETE_DOCS_ONLY_DATE));
    }
    query.append(ORDER_BY);
    return query.toString();
  }

  /**
   * Builds the query to send delete feeds to GSA. This query does not include
   * the "Additional Where Clause"(AWC) because the schema of Event Table and
   * the Classes included in AWC are different. Due to the exclusion of AWC in
   * query, there are chances that, connector may send Delete Feed to GSA for
   * documents which were never indexed to GSA
   *
   * @param checkpoint
   * @return
   * @throws RepositoryException
   */
  private String buildQueryToDelete(Checkpoint checkpoint)
          throws RepositoryException {
    StringBuilder query = new StringBuilder("SELECT TOP ");
    query.append(batchHint);
    query.append(" ");

    // GuidConstants.Class_DeletionEvent = Only deleted objects in event
    // table
    query.append(PropertyNames.ID);
    query.append(",");
    query.append(PropertyNames.DATE_CREATED);
    query.append(",");
    query.append(PropertyNames.VERSION_SERIES_ID);
    query.append(",");
    query.append(PropertyNames.SOURCE_OBJECT_ID);
    query.append(" FROM ");
    query.append(GuidConstants.Class_DeletionEvent);
    if (!checkpoint.isNull(JsonField.LAST_DELETION_EVENT_TIME)) {
      query.append(getCheckpointClause(checkpoint,
              JsonField.LAST_DELETION_EVENT_TIME,
              JsonField.UUID_DELETION_EVENT, WHERE_CLAUSE_TO_DELETE,
              WHERE_CLAUSE_TO_DELETE_ONLY_DATE));
    }
    query.append(ORDER_BY_TO_DELETE);
    return query.toString();
  }

  /**
   * Returns a query string for the checkpoint values.
   *
   * @param checkpoint the checkpoint
   * @param dateField the checkpoint date field
   * @param uuidField the checkpoint ID field
   * @param whereClause the date and ID where clause pattern
   * @param whereClauseOnlyDate the date only where clause pattern
   * @return a query string
   * @throws RepositoryException if the checkpoint is uninitialized
   */
  @VisibleForTesting
  String getCheckpointClause(Checkpoint checkPoint, JsonField dateField,
      JsonField uuidField, String whereClause, String whereClauseOnlyDate)
      throws RepositoryException {
    String uuid = checkPoint.getString(uuidField);
    String c = FileUtil.getQueryTimeString(checkPoint.getString(dateField));
    String statement;
    Object[] arguments = { c, uuid };
    if (uuid.equals("") || !connector.useIDForChangeDetection()) {
      statement = MessageFormat.format(whereClauseOnlyDate, arguments);
    } else {
      statement = MessageFormat.format(whereClause, arguments);
    }
    LOGGER.log(Level.FINE, "MakeCheckpointQueryString date: {0}", c);
    LOGGER.log(Level.FINE, "MakeCheckpointQueryString ID: {0}", uuid);
    return statement;
  }
}
