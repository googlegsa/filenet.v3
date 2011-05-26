/*
 * Copyright 2009 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.filenet4.filewrap.ISession;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Responsible for: 1. Construction of FileNet SQL queries for adding and
 * deleting index of documents to GSA. 2. Execution of the SQL query constructed
 * in step 1. 3. Retrieve the results of step 2 and wrap it in DocumentList
 * 
 * @author pankaj_chouhan
 */
public class FileTraversalManager implements TraversalManager {

	private static Logger logger;
	private static String dateFirstPush;
	static {
		logger = Logger.getLogger(FileTraversalManager.class.getName());
	}
	private IObjectFactory fileObjectFactory;
	private IObjectStore objectStore;
	private int batchint;
	private ISession fileSession;
	private String tableName = "Document d";
	private String order_by = " ORDER BY DateLastModified,Id";
	private String whereClause = " AND ((DateLastModified={0} AND (''{1}''<id)) OR (DateLastModified>{0}))";
	private String whereClauseOnlyDate = " AND ((DateLastModified>{0}))";
	private String orderByToDelete = " ORDER BY " + PropertyNames.DATE_CREATED
			+ "," + PropertyNames.ID;

	private String whereClauseToDelete = " WHERE (("
			+ PropertyNames.DATE_CREATED + "={0} AND (''{1}''<"
			+ PropertyNames.ID + ")) OR (" + PropertyNames.DATE_CREATED
			+ ">{0}))";

	private String whereClauseToDeleteOnlyDate = " WHERE ("
			+ PropertyNames.DATE_CREATED + ">{0})";

	private String whereClauseToDeleteDocs = " AND ((("
			+ PropertyNames.DATE_LAST_MODIFIED + "={0}  AND (''{1}''<id))OR ("
			+ PropertyNames.DATE_LAST_MODIFIED + ">{0})))";

	private String whereClauseToDeleteDocsOnlyDate = " AND ("
			+ PropertyNames.DATE_LAST_MODIFIED + ">{0})";

	private String additionalWhereClause;
	private String deleteadditionalWhereClause;
	private String displayUrl;
	private boolean isPublic;
	private boolean useIDForChangeDetection;
	private HashSet included_meta;
	private HashSet excluded_meta;
	private String db_timezone;

	public FileTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, boolean b,
			boolean useIDForChangeDetection, String displayUrl,
			String additionalWhereClause, String deleteadditionalWhereClause,
			HashSet included_meta, HashSet excluded_meta, String db_timezone)
			throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.isPublic = b;
		this.useIDForChangeDetection = useIDForChangeDetection;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.deleteadditionalWhereClause = deleteadditionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.db_timezone = db_timezone;
	}

	public FileTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, ISession fileSession, boolean b,
			boolean useIDForChangeDetection, String displayUrl,
			String additionalWhereClause, String deleteadditionalWhereClause,
			HashSet included_meta, HashSet excluded_meta, String db_timezone)
			throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object[] args = { objectStore.getName() };
		this.isPublic = b;
		this.useIDForChangeDetection = useIDForChangeDetection;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.deleteadditionalWhereClause = deleteadditionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.db_timezone = db_timezone;
	}

	public FileTraversalManager() {

	}

	public DocumentList startTraversal() throws RepositoryException {
		return resumeTraversal(null);
	}

	public DocumentList resumeTraversal(String checkPoint)
			throws RepositoryException {
		logger.info((checkPoint == null) ? "Starting traversal..."
				: "Resuming traversal...");
		DocumentList resultSet = null;
		objectStore.refreshSUserContext();
		setdateFirstPush();
		// to add
		String query = buildQueryString(checkPoint);

		ISearch search = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query to Add document: " + query);
		IObjectSet objectSet = search.execute(query);

		// to delete for deleted documents
		String queryStringToDelete = buildQueryToDelete(checkPoint);
		ISearch searchToDelete = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query to get deleted documents: "
				+ queryStringToDelete);
		IObjectSet objectSetToDelete = search.execute(queryStringToDelete);

		// to delete for additional delete clause

		if ((deleteadditionalWhereClause != null)
				&& !(deleteadditionalWhereClause.equals(""))) {
			String queryStringToDeleteDocs = buildQueryStringToDeleteDocs(checkPoint);

			ISearch searchToDeleteDocs = fileObjectFactory.getSearch(objectStore);
			logger.log(Level.INFO, "Query to get documents matching to the Delete where clause: "
					+ queryStringToDeleteDocs);
			IObjectSet objectSetToDeleteDocs = search.execute(queryStringToDeleteDocs);
			logger.log(Level.INFO, "Number of documents whose index will be deleted from GSA (Documents matching to the Delete where clause): "
					+ objectSetToDeleteDocs.getSize());

			if ((objectSet.getSize() > 0)
					|| (objectSetToDeleteDocs.getSize() > 0)
					|| (objectSetToDelete.getSize() > 0)) {
				resultSet = new FileDocumentList(objectSet,
						objectSetToDeleteDocs, objectSetToDelete, objectStore,
						this.isPublic, this.displayUrl, this.included_meta,
						this.excluded_meta, dateFirstPush, checkPoint);

			}
		} else {
			if ((objectSet.getSize() > 0) || (objectSetToDelete.getSize() > 0)) {
				resultSet = new FileDocumentList(objectSet, objectSetToDelete,
						objectStore, this.isPublic, this.displayUrl,
						this.included_meta, this.excluded_meta, dateFirstPush,
						checkPoint);
			}
			logger.log(Level.INFO, "Target ObjectStore is: " + this.objectStore);
			logger.log(Level.INFO, "Number of documents sent to GSA: "
					+ objectSet.getSize());
			logger.log(Level.INFO, "Number of documents whose index will be deleted from GSA (Documents deleted form Repository): "
					+ objectSetToDelete.getSize());
		}
		return resultSet;
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
	private String buildQueryString(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer("SELECT ");
		if (batchint > 0) {
			query.append("TOP " + batchint + " ");
		}
		query.append(PropertyNames.ID);
		query.append(",");
		query.append(PropertyNames.DATE_LAST_MODIFIED);
		query.append(" FROM ");
		query.append(tableName);
		query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

		if (additionalWhereClause != null && !additionalWhereClause.equals("")) {

			if ((additionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
				query = new StringBuffer(additionalWhereClause);
				query.replace(0, 6, "SELECT TOP " + batchint + " ");
				logger.fine("Using Custom Query[" + additionalWhereClause
						+ "], will add Checkpoint in next step.");
			} else {
				query.append(additionalWhereClause);
			}
		}

		if (checkpoint != null) {
			query.append(getCheckpointClause(checkpoint));
		}
		query.append(order_by);
		return query.toString();

	}

	public void setAdditionalWhereClause(String additionalWhereClaue) {
		this.additionalWhereClause = additionalWhereClaue;
	}

	/**
	 * Builds the query to send delete feeds to GSA based on the Additional
	 * Delete clause set as Connector Configuration.
	 * 
	 * @param checkpoint
	 * @return Query String
	 * @throws RepositoryException
	 */

	private String buildQueryStringToDeleteDocs(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer("SELECT ");
		if (batchint > 0) {
			query.append("TOP " + batchint + " ");
		}
		query.append(PropertyNames.ID);
		query.append(",");
		query.append(PropertyNames.DATE_LAST_MODIFIED);
		query.append(" FROM ");
		query.append(tableName);
		query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

		if (deleteadditionalWhereClause != null
				&& !deleteadditionalWhereClause.equals("")) {
			if ((deleteadditionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
				query = new StringBuffer(deleteadditionalWhereClause);
				query.replace(0, 6, "SELECT TOP " + batchint + " ");
				logger.fine("Using Custom Query[" + deleteadditionalWhereClause
						+ "], will add Checkpoint in next step.");
			} else {
				query.append(deleteadditionalWhereClause);
			}
		}
		if (checkpoint != null) {
			query.append(getCheckpointClauseToDeleteDocs(checkpoint));
		}
		query.append(order_by);
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
	private String buildQueryToDelete(String checkpoint)
			throws RepositoryException {

		logger.fine("Build query to get the documents removed : ");
		StringBuffer query = new StringBuffer("SELECT ");
		if (batchint > 0) {
			query.append("TOP " + batchint + " ");
		}
		// GuidConstants.Class_DeletionEvent = Only deleted objects in event
		// table
		query.append(PropertyNames.ID + "," + PropertyNames.DATE_CREATED + ","
				+ PropertyNames.VERSION_SERIES_ID + " FROM "
				+ GuidConstants.Class_DeletionEvent);
		if (checkpoint != null) {
			logger.fine("Checkpoint is not null");
			query.append(getCheckpointClauseToDelete(checkpoint));
		}
		query.append(orderByToDelete);

		return query.toString();
	}

	/**
	 * Sets Date and Time of First Push.
	 */

	private void setdateFirstPush() {
		logger.fine("Checkpoint is null");
		Calendar cal = Calendar.getInstance();
		DateFormat dateStandard = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");
		dateStandard.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFirstPush = dateStandard.format(cal.getTime());
	}

	/**
	 * Returns query string to send feeds to GSA by adding where clause
	 * condition for checkPoint values,
	 * 
	 * @param checkpoint
	 * @return Query String
	 * @throws JSONException
	 */
	private String getCheckpointClause(String checkPoint)
			throws RepositoryException {

		JSONObject jo = null;
		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint, "uuid");
		String c = extractNativeDateFromCheckpoint(jo, checkPoint, "lastModified");
		String queryString = makeCheckpointQueryString(uuid, c);
		return queryString;
	}

	/**
	 * Returns query string to send delete feed to GSA by adding delete where
	 * clause condition for checkPoint values,
	 * 
	 * @param checkpoint
	 * @return Query String
	 * @throws JSONException
	 */

	private String getCheckpointClauseToDeleteDocs(String checkPoint)
			throws RepositoryException {

		JSONObject jo = null;
		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint, "uuidToDeleteDocs");
		String c = extractNativeDateFromCheckpoint(jo, checkPoint, "lastModifiedDate");
		String queryString = makeCheckpointQueryStringToDeleteDocs(uuid, c);
		return queryString;
	}

	/**
	 * Returns query string to send delete Feed for documents matching for
	 * checkPoint value condition.
	 * 
	 * @param checkpoint
	 * @return Query String
	 * @throws JSONException
	 */

	private String getCheckpointClauseToDelete(String checkPoint)
			throws RepositoryException {

		JSONObject jo = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint, "uuidToDelete");
		String c = extractNativeDateFromCheckpoint(jo, checkPoint, "lastRemoveDate");
		String queryString = makeCheckpointQueryStringToDelete(uuid, c);
		return queryString;

	}

	/**
	 * Returns query string to send delete feed to GSA by adding where clause
	 * condition for checkPoint values.
	 * 
	 * @param checkpoint values (String ID, String Date)
	 * @return Query String
	 */

	protected String makeCheckpointQueryStringToDelete(String uuid, String c)
			throws RepositoryException {
		String statement = "";
		Object[] arguments = { c, uuid };
		if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
			statement = MessageFormat.format(whereClauseToDeleteOnlyDate, arguments);
		} else {
			statement = MessageFormat.format(whereClauseToDelete, arguments);
		}
		logger.log(Level.FINE, "MakeCheckpointQueryString date: " + c);
		logger.log(Level.FINE, "MakeCheckpointQueryString ID: " + uuid);
		return statement;
	}

	/**
	 * Returns query string to send delete feed to GSA by adding where clause
	 * condition for checkPoint values.
	 * 
	 * @param checkpoint values (String ID, String Date)
	 * @return Query String
	 */
	protected String makeCheckpointQueryStringToDeleteDocs(String uuid, String c)
			throws RepositoryException {
		String statement = "";
		Object[] arguments = { c, uuid };

		if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
			statement = MessageFormat.format(whereClauseToDeleteDocsOnlyDate, arguments);
		} else {
			statement = MessageFormat.format(whereClauseToDeleteDocs, arguments);
		}
		logger.log(Level.FINE, "MakeCheckpointQueryString date: " + c);
		logger.log(Level.FINE, "MakeCheckpointQueryString ID: " + uuid);

		return statement;
	}

	/**
	 * To set BatchHint for traversal.
	 */
	public void setBatchHint(int batchHint) throws RepositoryException {
		this.batchint = batchHint;
	}

	/**
	 * To extract Docid from Checkpoint string
	 * 
	 * @param jo
	 * @param checkPoint
	 * @param param
	 * @return
	 */
	protected String extractDocidFromCheckpoint(JSONObject jo,
			String checkPoint, String param) {
		String uuid = null;
		try {
			uuid = jo.getString(param);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get uuid from checkPoint string: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"Could not get uuid from checkPoint string: " + checkPoint);
		}
		return uuid;
	}

	/**
	 * To extract date part from Checkpoint string
	 * 
	 * @param jo
	 * @param checkPoint
	 * @param param
	 * @return
	 */
	protected String extractNativeDateFromCheckpoint(JSONObject jo,
			String checkPoint, String param) {
		String dateString = null;
		try {
			dateString = jo.getString(param);
			// dateString = "2008-09-05T09:40:04.073";
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get last modified/removed date from checkPoint string: "
					+ checkPoint, e);
			throw new IllegalArgumentException(
					"Could not get last modified/removed date from checkPoint string: "
							+ checkPoint, e);
		}

		String timeZoneOffset = null;
		if (this.db_timezone == null || this.db_timezone.equalsIgnoreCase("")) {
			timeZoneOffset = FileUtil.getTimeZone(Calendar.getInstance());
		} else {
			timeZoneOffset = FileUtil.getTimeZone(this.db_timezone);
		}

		return dateString + timeZoneOffset;
	}

	/**
	 * To construct check point query to fetch documents form FileNet repository
	 * using check point values provided as parameters
	 * 
	 * @param uuid
	 * @param c
	 * @return
	 * @throws RepositoryException
	 */
	protected String makeCheckpointQueryString(String uuid, String c)
			throws RepositoryException {
		String statement;
		Object[] arguments = { c, uuid };
		if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
			statement = MessageFormat.format(whereClauseOnlyDate, arguments);
		} else {
			statement = MessageFormat.format(whereClause, arguments);
		}
		logger.log(Level.FINE, "MakeCheckpointQueryString date: " + c);
		logger.log(Level.FINE, "MakeCheckpointQueryString ID: " + uuid);
		return statement;
	}

	private Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlSource)));
		} catch (ParserConfigurationException de) {
			logger.log(Level.WARNING, "Unable to configure parser for parsing XML string");
			RepositoryException re = new RepositoryLoginException(
					"Unable to configure parser for parsing XML string", de);
			throw re;
		} catch (SAXException de) {
			logger.log(Level.WARNING, "Unable to parse XML string");
			RepositoryException re = new RepositoryLoginException(
					"Unable to parse XML string", de);
			throw re;
		} catch (IOException de) {
			logger.log(Level.WARNING, "XML source string to be parsed not found.");
			RepositoryException re = new RepositoryLoginException(
					"XML source string to be parsed not found.", de);
			throw re;
		}

	}
}
