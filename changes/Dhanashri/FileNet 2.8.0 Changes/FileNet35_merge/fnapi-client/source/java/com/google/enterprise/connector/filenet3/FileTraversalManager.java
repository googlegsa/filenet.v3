// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.ISearch;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.wcm.api.VersionableObject;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FileTraversalManager implements TraversalManager {

	private static Logger LOGGER = Logger.getLogger(FileTraversalManager.class.getName());
	private static String dateFirstPush;
	private IObjectFactory fileObjectFactory;
	private IObjectStore objectStore;
	private ISession fileSession;
	// private final String ORDER_BY = " ORDER BY DateLastModified,Id";
	private final String ORDER_BY = " ORDER BY DateLastModified,Id";
	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore Id=\"{0}\"/></objectstores>";
	private String tableName = "Document";
	private String whereClause = " AND ((DateLastModified={0} AND ({1}&lt;Id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseOnlyDate = " AND (DateLastModified&gt;{0})";
	private String tableNameEventToDelete = "Event";
	private String whereClauseToDelete = " WHERE ((DateLastModified={0} AND ({1}&lt;Id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseToDeleteOnlyDate = " WHERE (DateLastModified&gt;{0})";
	private String whereClauseAsDeleteClause = " AND ((DateLastModified={0} AND ({1}&lt;Id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseAsDeleteClauseOnlyDate = " AND (DateLastModified&gt;{0})";
	private String orderByToDelete = " ORDER BY DateLastModified,Id";
	private String displayUrl;
	private String additionalWhereClause;
	private String additionalDeleteWhereClause;
	private int batchint;
	private boolean isPublic;
	private boolean useIDForChangeDetection;
	private HashSet included_meta;
	private HashSet excluded_meta;
	private final static String eventAllias = " e";
	private final static String AND_OPERATOR = " AND ";
	private final static String OR_OPERATOR = " OR ";
	private final static String EVENT_DELETE = "DeletionEvent";
	private final static String VERSION_STATUS_RELEASED = VersionableObject.VERSION_STATUS_RELEASED
	        + " ";
	private final static String VERSION_STATUS_IN_PROCESS = VersionableObject.VERSION_STATUS_IN_PROCESS
	        + " ";

	public FileTraversalManager(IObjectFactory fileObjectFactory,
	        IObjectStore objectStore, ISession fileSession, boolean b,
	        boolean useID, String displayUrl, String additionalWhereClause,
	        String additionalDeleteWhereClause, HashSet included_meta,
	        HashSet excluded_meta) throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object[] args = { objectStore.getName() };
		objectStoresQuery = MessageFormat.format(objectStoresQuery, args);
		this.isPublic = b;
		this.useIDForChangeDetection = useID;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.additionalDeleteWhereClause = additionalDeleteWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
	}

	/**
	 * To start acquiring documents from the content management system. And to
	 * return a list of documents to connector manager.
	 */
	public DocumentList startTraversal() throws RepositoryException {
		return resumeTraversal(null);
	}

	/**
	 * Accepts the previously remembered checkpoint to indicate where to resume
	 * acquiring documents from the FileNet repository and to return a
	 * DocumentList identifying documents to traverse in this batch.
	 */
	public DocumentList resumeTraversal(String checkPoint)
	        throws RepositoryException {
		LOGGER.info((checkPoint == null) ? "Starting traversal..."
		        : "Resuming traversal...");
		DocumentList resultSet = null;
		String queryString = buildQueryString(checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);

		LOGGER.log(Level.INFO, "Target ObjectStore is: " + this.objectStore);
		LOGGER.log(Level.FINE, "Query to Add document: " + queryString);

		String strResultDoc = search.executeXml(queryString, this.objectStore);
		Document resultDoc = this.stringToDom(strResultDoc);

		LOGGER.log(Level.FINE, "String resultDoc before stringToDom"
		        + strResultDoc);
		// Added: document remove all
		String queryStringToDelete = buildQueryToDelete(checkPoint);
		ISearch searchToDelete = this.fileObjectFactory.getSearch(this.fileSession);
		LOGGER.log(Level.FINE, "Query to get removed documents: "
		        + queryStringToDelete);
		// logger.log(Level.INFO, "objectStore: " + this.objectStore);
		// logger.log(Level.INFO, "searchToDelete: " + searchToDelete);

		String strResultDocDelete = searchToDelete.executeXml(queryStringToDelete, this.objectStore);
		Document resultDocToDelete = this.stringToDom(searchToDelete.executeXml(queryStringToDelete, this.objectStore));

		LOGGER.log(Level.FINE, "String ResultDeleteDoc before stringToDom"
		        + strResultDocDelete);

		Document resultDocAsDeleteClause = null;
		if ((additionalDeleteWhereClause != null)
		        && !(additionalDeleteWhereClause.equals(""))) {
			// Delete document matching with delete clause
			String queryStringAsDeleteClause = buildQueryStringAsDeleteClause(checkPoint);
			ISearch searchAsDeleteClause = this.fileObjectFactory.getSearch(this.fileSession);
			LOGGER.log(Level.FINE, "Query to get removed documents matching with delete clause : "
			        + queryStringAsDeleteClause);
			// logger.log(Level.INFO, "objectStore: " + this.objectStore);
			// logger.log(Level.INFO, "searchToDelete: " + searchToDelete);

			String strResultDeleteDoc = searchAsDeleteClause.executeXml(queryStringAsDeleteClause, this.objectStore);
			resultDocAsDeleteClause = this.stringToDom(searchAsDeleteClause.executeXml(queryStringAsDeleteClause, this.objectStore));
			LOGGER.log(Level.FINE, "String ResultDeleteDoc matching with delete clause before stringToDom"
			        + strResultDeleteDoc);

		}

		LOGGER.log(Level.FINE, "StringDelete before stringToDom"
		        + search.executeXml(queryStringToDelete, objectStore));
		LOGGER.log(Level.INFO, "Number of documents sent to GSA: "
		        + resultDoc.getElementsByTagName("z:row").getLength());
		int deleteCountAsDeleteClause = 0;
		if (resultDocAsDeleteClause != null) {
			deleteCountAsDeleteClause = resultDocAsDeleteClause.getElementsByTagName("z:row").getLength();
			LOGGER.log(Level.INFO, "Number of documents matching with delete clause whose index will be deleted from GSA: "
			        + resultDocAsDeleteClause.getElementsByTagName("z:row").getLength());
		}
		LOGGER.log(Level.INFO, "Number of documents whose index will be deleted from GSA: "
		        + resultDocToDelete.getElementsByTagName("z:row").getLength());
		// Make the document list
		if ((resultDoc.getElementsByTagName("z:row").getLength() > 0)
		        || (resultDocToDelete.getElementsByTagName("z:row").getLength() > 0)
		        || (deleteCountAsDeleteClause > 0)) {
			resultSet = new FileDocumentList(resultDoc,
			        resultDocAsDeleteClause, resultDocToDelete, objectStore,
			        this.isPublic, this.displayUrl, this.included_meta,
			        this.excluded_meta, dateFirstPush, checkPoint);
		}
		return resultSet;
	}

	/**
	 * To construct FileNet query to fetch documents from FileNet repository
	 * considering additional where clause specified as connector configuration
	 * and the previously remembered checkpoint to indicate where to resume
	 * acquiring documents from the FileNet repository
	 * 
	 * @param checkpoint
	 * @return
	 * @throws RepositoryException
	 */
	private String buildQueryString(String checkpoint)
	        throws RepositoryException {
		StringBuffer query = new StringBuffer(
		        "<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);
		// query.append("<querystatement>SELECT Id,DateLastModified FROM ");
		// query.append(tableName);
		// query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

		// if (additionalWhereClause != null &&
		// !additionalWhereClause.equals("")) {
		// query.append(additionalWhereClause);
		// }

		if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
			if ((additionalWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {
				query.append("<querystatement>" + additionalWhereClause);
				// query = new StringBuffer(additionalWhereClause);
				LOGGER.fine("Using Custom Query[" + additionalWhereClause
				        + "], will add Checkpoint in next step.");
			} else {
				query.append("<querystatement>SELECT Id,DateLastModified FROM ");
				query.append(tableName);
				query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
				query.append(additionalWhereClause);

			}
		} else {
			query.append("<querystatement>SELECT Id,DateLastModified FROM ");
			query.append(tableName);
			query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
		}

		if (checkpoint != null) {
			query.append(getCheckpointClause(checkpoint, "uuid", "lastModified"));
		}
		query.append(ORDER_BY);
		query.append("</querystatement>");

		if (batchint > 0) {
			query.append("<options maxrecords='" + batchint
			        + "' objectasid=\"false\"/>");
		}
		query.append("</request>");
		return query.toString();
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
	private String buildQueryStringAsDeleteClause(String checkpoint)
	        throws RepositoryException {

		LOGGER.log(Level.FINE, "Build query to get the documents matching with delete here clause : ");

		StringBuffer query = new StringBuffer(
		        "<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);

		if (additionalDeleteWhereClause != null
		        && !additionalDeleteWhereClause.equals("")) {

			if ((additionalDeleteWhereClause.toUpperCase()).startsWith("SELECT ID,DATELASTMODIFIED FROM ")) {

				query.append("<querystatement>" + additionalDeleteWhereClause);
				// query = new StringBuffer(additionalDeleteWhereClause);
				LOGGER.fine("Using Custom Query[" + additionalDeleteWhereClause
				        + "], will add Checkpoint in next step.");
			} else {
				query.append("<querystatement>SELECT Id,DateLastModified FROM ");
				query.append(tableName);
				query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
				query.append(additionalDeleteWhereClause);
			}
		} else {
			query.append("<querystatement>SELECT Id,DateLastModified FROM ");
			query.append(tableName);
			query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
		}
		if (checkpoint != null) {
			query.append(getCheckpointClause(checkpoint, "uuidAsDeleteClause", "lastModifiedForDeleteDoc"));
		}
		query.append(ORDER_BY);
		query.append("</querystatement>");

		if (batchint > 0) {
			query.append("<options maxrecords='" + batchint
			        + "' objectasid=\"false\"/>");
		}
		query.append("</request>");
		return query.toString();
	}

	/**
	 * To construct FileNet query to fetch documents deleted from FileNet
	 * repository previously remembered checkpoint to indicate where to resume
	 * acquiring documents from the FileNet repository to send delete feed.
	 * 
	 * @param checkpoint
	 * @return
	 * @throws RepositoryException
	 */
	private String buildQueryToDelete(String checkpoint)
	        throws RepositoryException {

		LOGGER.log(Level.FINE, "Build query to get the documents removed : ");
		StringBuffer query = null;
		if (checkpoint != null) {
			query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
			query.append(objectStoresQuery);
			query.append("<querystatement>SELECT Id,DateLastModified,VersionSeriesId  FROM ");
			query.append(tableNameEventToDelete);
			query.append(eventAllias);
			query.append(getCheckpointClause(checkpoint, "uuidToDelete", "lastRemoveDate"));
			query.append(AND_OPERATOR);
			query.append(isClassSQLFunction(eventAllias, EVENT_DELETE));
			query.append(orderByToDelete);
			query.append("</querystatement>");

			if (batchint > 0) {
				query.append("<options maxrecords='" + batchint
				        + "' objectasid=\"false\"/>");
			}
			query.append("</request>");
		} else {
			// Get the date of today, corresponding to the date of first push
			Calendar cal = Calendar.getInstance();
			LOGGER.log(Level.FINEST, "BuildQueryToDelete cal : " + cal);

			Date d = cal.getTime();
			SimpleDateFormat dateStandard = new SimpleDateFormat(
			        "yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateStandard.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateFirstPush = dateStandard.format(d);
			LOGGER.log(Level.FINEST, "buildQueryToDelete dateFirstPush : "
			        + dateFirstPush);
			// /
			query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
			query.append(objectStoresQuery);
			query.append("<querystatement>SELECT Id,DateLastModified,VersionSeriesId  FROM ");
			query.append(tableNameEventToDelete);
			query.append(eventAllias);
			query.append(" WHERE ");
			// //////////////////////////////////////////////////////////////////////
			query.append(" (DateLastModified >" + dateFirstPush + ")");
			query.append(AND_OPERATOR);
			query.append(isClassSQLFunction(eventAllias, EVENT_DELETE));
			// //////////////////////////////////////////////////////////////////////

			query.append(orderByToDelete);
			query.append("</querystatement>");
			if (batchint > 0) {
				query.append("<options maxrecords='" + batchint
				        + "' objectasid=\"false\"/>");
			}
			query.append("</request>");
		}

		return query.toString();
	}

	/**
	 * To construct check point query using check point values provided as
	 * parameters
	 * 
	 * @param checkPoint
	 * @param uuidParam
	 * @param dateParam
	 * @return
	 * @throws RepositoryException
	 */
	private String getCheckpointClause(String checkPoint, String uuidParam,
	        String dateParam) throws RepositoryException {

		LOGGER.info(checkPoint);

		JSONObject jo = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "CheckPoint string does not parse as JSON: "
			        + checkPoint);
			throw new IllegalArgumentException(
			        "CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint, uuidParam);
		String c = extractNativeDateFromCheckpoint(jo, checkPoint, dateParam);
		String queryString = makeCheckpointQueryString(uuid, c, uuidParam);
		return queryString;

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
			LOGGER.log(Level.WARNING, "Could not get uuid from checkPoint string: "
			        + checkPoint, e);
			throw new IllegalArgumentException(
			        "Could not get uuid from checkPoint string: " + checkPoint,
			        e);
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
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Could not get last modified date from checkPoint string: "
			        + checkPoint);
			throw new IllegalArgumentException(
			        "Could not get last modified date from checkPoint string: "
			        + checkPoint);
		}

		return dateString;
	}

	/**
	 * To construct check point query using check point values provided as
	 * parameters
	 * 
	 * @param uuid
	 * @param c
	 * @param uuidParam
	 * @return
	 * @throws RepositoryException
	 */
	protected String makeCheckpointQueryString(String uuid, String c,
	        String uuidParam) throws RepositoryException {

		String statement = null;
		Object[] arguments = { c, uuid };

		if (uuidParam.equalsIgnoreCase("uuid")) {
			if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
				statement = MessageFormat.format(whereClauseOnlyDate, arguments);
			} else {
				statement = MessageFormat.format(whereClause, arguments);
			}
		} else if (uuidParam.equalsIgnoreCase("uuidToDelete")) {
			if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
				statement = MessageFormat.format(whereClauseToDeleteOnlyDate, arguments);
			} else {
				statement = MessageFormat.format(whereClauseToDelete, arguments);
			}
		} else if (uuidParam.equalsIgnoreCase("uuidAsDeleteClause")) {
			if (uuid.equals("") || (this.useIDForChangeDetection == (false))) {
				statement = MessageFormat.format(whereClauseAsDeleteClauseOnlyDate, arguments);
			} else {
				statement = MessageFormat.format(whereClauseAsDeleteClause, arguments);
			}
		}
		LOGGER.log(Level.FINE, "MakeCheckpointQueryString date: [" + c + "]");
		LOGGER.log(Level.FINE, "MakeCheckpointQueryString ID: [" + uuid + "]");
		return statement;
	}

	private Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlSource)));
		} catch (ParserConfigurationException de) {
			LOGGER.log(Level.WARNING, "Unable to configure parser for parsing XML string");
			RepositoryException re = new RepositoryLoginException(
			        "Unable to configure parser for parsing XML string", de);
			throw re;
		} catch (SAXException de) {
			LOGGER.log(Level.WARNING, "Unable to parse XML string");
			RepositoryException re = new RepositoryLoginException(
			        "Unable to parse XML string", de);
			throw re;
		} catch (IOException de) {
			LOGGER.log(Level.WARNING, "XML source string to be parsed not found.");
			RepositoryException re = new RepositoryLoginException(
			        "XML source string to be parsed not found.", de);
			throw re;
		}
	}

	/**
	 *To construct InClass part of the FileNet query for traversal.
	 * 
	 * @param refEventAllias
	 * @param eventType
	 * @return
	 */
	private String isClassSQLFunction(String refEventAllias, String eventType) {
		return new String("IsClass(" + refEventAllias + ", " + eventType + " )");
	}
}
