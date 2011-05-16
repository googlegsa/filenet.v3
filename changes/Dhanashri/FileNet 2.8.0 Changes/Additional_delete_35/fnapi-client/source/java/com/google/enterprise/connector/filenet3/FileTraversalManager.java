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

	private static Logger logger;
	private static String dateFirstPush;
	static {
		logger = Logger.getLogger(FileTraversalManager.class.getName());
	}
	private IObjectFactory fileObjectFactory;
	private IObjectStore objectStore;
	private ISession fileSession;
	private final String ORDER_BY = " ORDER BY DateLastModified,Id";
	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>";
	private String tableName = "Document";
	private String whereClause = " AND ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&gt;{0}))";
	private String tableNameEventToDelete = "Event";
	private String whereClauseToDelete = " WHERE ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseToDeleteOnlyDate = " WHERE (DateLastModified&gt;{0})";
	private String orderByToDelete = " ORDER BY DateLastModified,Id";
	private String displayUrl;
	private String additionalWhereClause;
	private int batchint;
	private boolean isPublic;
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
			String displayUrl, String additionalWhereClause,
			HashSet included_meta, HashSet excluded_meta)
			throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object[] args = { objectStore.getName() };
		objectStoresQuery = MessageFormat.format(objectStoresQuery, args);
		this.isPublic = b;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
	}

	public DocumentList startTraversal() throws RepositoryException {
		return resumeTraversal(null);
	}

	public DocumentList resumeTraversal(String checkPoint)
			throws RepositoryException {
		logger.info((checkPoint == null) ? "Starting traversal..."
				: "Resuming traversal...");
		DocumentList resultSet = null;
		String queryString = buildQueryString(checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);

		logger.log(Level.INFO, "Target ObjectStore is: " + this.objectStore);
		logger.log(Level.FINE, "Query to Add document: " + queryString);

		String strResultDoc = search.executeXml(queryString, this.objectStore);
		Document resultDoc = this.stringToDom(strResultDoc);

		logger.log(Level.FINE, "String resultDoc before stringToDom"
				+ strResultDoc);
		// Added: document remove all
		String queryStringToDelete = buildQueryToDelete(checkPoint);
		ISearch searchToDelete = this.fileObjectFactory.getSearch(this.fileSession);
		logger.log(Level.FINE, "Query to get removed documents: "
				+ queryStringToDelete);
		// logger.log(Level.INFO, "objectStore: " + this.objectStore);
		// logger.log(Level.INFO, "searchToDelete: " + searchToDelete);

		Document resultDocToDelete = this.stringToDom(searchToDelete.executeXml(queryStringToDelete, this.objectStore));

		logger.log(Level.FINE, "StringDelete before stringToDom"
				+ search.executeXml(queryStringToDelete, objectStore));
		logger.log(Level.INFO, "Number of documents sent to GSA: "
				+ resultDoc.getElementsByTagName("z:row").getLength());
		logger.log(Level.INFO, "Number of documents whose index will be deleted from GSA: "
				+ resultDocToDelete.getElementsByTagName("z:row").getLength());
		// Make the document list
		if ((resultDoc.getElementsByTagName("z:row").getLength() > 0)
				|| (resultDocToDelete.getElementsByTagName("z:row").getLength() > 0)) {
			resultSet = new FileDocumentList(resultDoc, resultDocToDelete,
					objectStore, this.isPublic, this.displayUrl,
					this.included_meta, this.excluded_meta, dateFirstPush,
					checkPoint);
		}
		return resultSet;
	}

	private String buildQueryString(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer(
				"<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);
		query.append("<querystatement>SELECT Id,DateLastModified FROM ");
		query.append(tableName);
		query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");

		if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
			query.append(additionalWhereClause);
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

	private String buildQueryToDelete(String checkpoint)
			throws RepositoryException {

		logger.log(Level.FINE, "Build query to get the documents removed : ");
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
			logger.log(Level.FINEST, "BuildQueryToDelete cal : " + cal);

			Date d = cal.getTime();
			SimpleDateFormat dateStandard = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateStandard.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateFirstPush = dateStandard.format(d);
			logger.log(Level.FINEST, "buildQueryToDelete dateFirstPush : "
					+ dateFirstPush);
			// /
			query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
			query.append(objectStoresQuery);
			query.append("<querystatement>SELECT Id,DateLastModified,VersionSeriesId  FROM ");
			query.append(tableNameEventToDelete);
			query.append(eventAllias);
			query.append(" WHERE ");
			// ///////////////////////////////////////////////////////////////////////
			query.append(" (DateLastModified >" + dateFirstPush + ")");
			query.append(AND_OPERATOR);
			query.append(isClassSQLFunction(eventAllias, EVENT_DELETE));
			// ///////////////////////////////////////////////////////////////////////

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

	private String getCheckpointClause(String checkPoint, String uuidParam,
			String dateParam) throws RepositoryException {

		logger.info(checkPoint);

		JSONObject jo = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint, uuidParam);
		String c = extractNativeDateFromCheckpoint(jo, checkPoint, dateParam);
		String queryString = makeCheckpointQueryString(uuid, c, uuidParam);
		return queryString;

	}

	public void setBatchHint(int batchHint) throws RepositoryException {
		this.batchint = batchHint;

	}

	protected String extractDocidFromCheckpoint(JSONObject jo,
			String checkPoint, String param) {
		String uuid = null;
		try {
			uuid = jo.getString(param);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get uuid from checkPoint string: "
					+ checkPoint, e);
			throw new IllegalArgumentException(
					"Could not get uuid from checkPoint string: " + checkPoint,
					e);
		}
		return uuid;
	}

	protected String extractNativeDateFromCheckpoint(JSONObject jo,
			String checkPoint, String param) {
		String dateString = null;
		try {
			dateString = jo.getString(param);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get last modified date from checkPoint string: "
					+ checkPoint);
			throw new IllegalArgumentException(
					"Could not get last modified date from checkPoint string: "
							+ checkPoint);
		}

		return dateString;
	}

	protected String makeCheckpointQueryString(String uuid, String c,
			String uuidParam) throws RepositoryException {

		String statement = null;
		Object[] arguments = { c, uuid };
		if (uuidParam.equalsIgnoreCase("uuid")) {
			statement = MessageFormat.format(whereClause, arguments);
		} else if (uuidParam.equalsIgnoreCase("uuidToDelete")) {
			if (uuid.equals("")) {
				statement = MessageFormat.format(whereClauseToDeleteOnlyDate, arguments);
			} else {
				statement = MessageFormat.format(whereClauseToDelete, arguments);
			}
		}
		logger.log(Level.FINE, "MakeCheckpointQueryString date: [" + c + "]");
		logger.log(Level.FINE, "MakeCheckpointQueryString ID: [" + uuid + "]");
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

	private String isClassSQLFunction(String refEventAllias, String eventType) {
		return new String("IsClass(" + refEventAllias + ", " + eventType + " )");
	}
}
