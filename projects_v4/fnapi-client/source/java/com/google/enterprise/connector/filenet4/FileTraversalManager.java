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

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.filenet4.filewrap.ISession;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
/**
 * Responsible for:
 * 1. Construction of FileNet SQL queries for adding and deleting index of documents to GSA.
 * 2. Execution of the SQL query constructed in step 1.
 * 3. Retrieve the results of step 2 and wrap it in DocumentList
 * @author pankaj_chouhan
 *
 */
public class FileTraversalManager implements TraversalManager {

	private static Logger logger;
	private static String dateFirstPush ;
	static{
		logger = Logger.getLogger(FileTraversalManager.class.getName());
	}
	private IObjectFactory fileObjectFactory;
	private IObjectStore objectStore;
	private int batchint;
	private ISession fileSession;
	private String tableName = "Document";
	private String order_by = " ORDER BY DateLastModified,Id";
	private String whereClause = " AND ((DateLastModified={0} AND (''{1}''<id)) OR (DateLastModified>{0}))";
	private String orderByToDelete  = " ORDER BY "+PropertyNames.DATE_CREATED +","+PropertyNames.ID;
	private String whereClauseToDelete = " WHERE (("+PropertyNames.DATE_CREATED +"={0} AND (''{1}''<"+PropertyNames.ID+")) OR ("+PropertyNames.DATE_CREATED +">{0}))";
	private String whereClauseToDeleteOnlyDate = " WHERE ("+PropertyNames.DATE_CREATED +">{0})";
	private String additionalWhereClause;
	private String displayUrl;
	private boolean isPublic;
	private HashSet included_meta;
	private HashSet excluded_meta;

	public FileTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, boolean b, String displayUrl,
			String additionalWhereClause, HashSet included_meta,
			HashSet excluded_meta) throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.isPublic = b;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
	}

	public FileTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, ISession fileSession, boolean b,
			String displayUrl, String additionalWhereClause,
			HashSet included_meta, HashSet excluded_meta)
	throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object[] args = { objectStore.getName() };
		this.isPublic = b;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;

	}

	public DocumentList startTraversal() throws RepositoryException {
		return resumeTraversal(null);
	}

	public DocumentList resumeTraversal(String checkPoint) throws RepositoryException {
		logger.info((checkPoint == null) ? "Starting traversal..." : "Resuming traversal...");
		DocumentList resultSet = null;
		objectStore.refreshSUserContext();

		//to add
		String query = buildQueryString(checkPoint);

		ISearch search = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query to Add document: " + query);
		IObjectSet objectSet = search.execute(query);

		//to delete
		String queryStringToDelete = buildQueryToDelete(checkPoint);

		ISearch searchToDelete = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query to get deleted documents: " + queryStringToDelete);
		IObjectSet objectSetToDelete = search.execute(queryStringToDelete);

		logger.log(Level.INFO, "Target ObjectStore is: " + this.objectStore);
		logger.log(Level.INFO, "Number of documents sent to GSA: "+objectSet.getSize());
		logger.log(Level.INFO, "Number of documents whose index will be deleted from GSA: "+objectSetToDelete.getSize());

		if ((objectSet.getSize() > 0) || (objectSetToDelete.getSize() > 0)) {
			resultSet = new FileDocumentList(objectSet, objectSetToDelete, objectStore, this.isPublic,
					this.displayUrl, this.included_meta, this.excluded_meta,dateFirstPush,checkPoint);
		}
		return resultSet;
	}

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
			query.append(additionalWhereClause);
		}
		if (checkpoint != null) {
			query.append(getCheckpointClause(checkpoint));
		}
		query.append(order_by);

		return query.toString();
	}
	private String buildQueryToDelete(String checkpoint) throws RepositoryException {

		logger.fine("Build query to get the documents removed : ");
		StringBuffer query= new StringBuffer("SELECT ");
		if (batchint > 0) {
			query.append("TOP " + batchint + " ");
		}
		//GuidConstants.Class_DeletionEvent = Only deleted objects in event table
		query.append(PropertyNames.ID +","+PropertyNames.DATE_CREATED+","+PropertyNames.VERSION_SERIES_ID +" FROM " +GuidConstants.Class_DeletionEvent);
		if(checkpoint != null)
		{
			logger.fine("Checkpoint is not null");
			query.append(getCheckpointClauseToDelete(checkpoint));
			//Commented the below code so that additionial where clause should not be included in FileNet
			//query. In some cases like finding all the deleted records of a particular folder, query
			//does not work i.e. "DOCUMENT.this INSUBFOLDER '/folder_name', throws exception: Document
			//class not found.
			/*if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
				query.append(additionalWhereClause);
			}*/
			query.append(orderByToDelete);
		}else{
			//Get the date of today, corresponding to the date of first push
			logger.fine("Checkpoint is null");
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			Calendar cal = Calendar.getInstance();
			Date d=cal.getTime();
			java.text.SimpleDateFormat dateStandard = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateFirstPush = dateStandard.format(d);
			query.append(" WHERE ");
			//Commented the below code so that additionial where clause should not be included in FileNet
			//query. In some cases like finding all the deleted records of a particular folder, query
			//does not work i.e. "DOCUMENT.this INSUBFOLDER '/folder_name', throws exception: Document
			//class not found.
			/*if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
				query.append(additionalWhereClause);
			}*/
			query.append(" ("+PropertyNames.DATE_CREATED +">" + dateFirstPush + ")");
			query.append(orderByToDelete);
		}
		return query.toString();
	}

	private String getCheckpointClause(String checkPoint)
			throws RepositoryException {

		JSONObject jo = null;
		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: " + checkPoint);
			throw new IllegalArgumentException("CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint);
		String c = extractNativeDateFromCheckpoint(jo, checkPoint);
		String queryString = makeCheckpointQueryString(uuid, c);
		return queryString;
	}

	private String getCheckpointClauseToDelete(String checkPoint)
	throws RepositoryException {

		JSONObject jo = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "CheckPoint string does not parse as JSON: " + checkPoint);
			throw new IllegalArgumentException("CheckPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpointToDelete(jo, checkPoint);
		String c = extractNativeDateFromCheckpointToDelete(jo, checkPoint);
		String queryString = makeCheckpointQueryStringToDelete(uuid, c);
		return queryString;

	}

	protected String extractDocidFromCheckpointToDelete(JSONObject jo, String checkPoint) {
		String uuid = null;
		try {
			uuid = jo.getString("uuidToDelete");
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get uuid from checkPoint string: " + checkPoint);
			throw new IllegalArgumentException("Could not get uuid from checkPoint string: " + checkPoint);
		}
		return uuid;
	}

	protected String extractNativeDateFromCheckpointToDelete(JSONObject jo,
			String checkPoint) {
		String dateString = null;
//		Date d=null;
		try {
			dateString = jo.getString("lastRemoveDate");
			//dateString = "2008-09-04T11:00:16.000";
		}catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get last modified date from checkPoint string: " + checkPoint);
			throw new IllegalArgumentException("Could not get last modified date from checkPoint string: " + checkPoint);
		}

		return dateString;
	}
	protected String makeCheckpointQueryStringToDelete(String uuid, String c)
	throws RepositoryException {
		String statement="";
		Object[] arguments = { c, uuid};
		if(uuid.equals(""))
		{
			statement = MessageFormat.format(whereClauseToDeleteOnlyDate, arguments);
		}else{
			statement = MessageFormat.format(whereClauseToDelete, arguments);
		}
		logger.log(Level.FINE, "MakeCheckpointQueryString date: " + c);
		logger.log(Level.FINE, "MakeCheckpointQueryString ID: " + uuid);
		return statement;
	}

	public void setBatchHint(int batchHint) throws RepositoryException {
		this.batchint = batchHint;

	}

	protected String extractDocidFromCheckpoint(JSONObject jo, String checkPoint) {
		String uuid = null;
		try {
			uuid = jo.getString("uuid");
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get uuid from checkPoint string: " + checkPoint);
			throw new IllegalArgumentException("Could not get uuid from checkPoint string: " + checkPoint);
		}
		return uuid;
	}

	protected String extractNativeDateFromCheckpoint(JSONObject jo,
			String checkPoint) {
		String dateString = null;
		try {
			dateString = jo.getString("lastModified");
			//dateString = "2008-09-05T09:40:04.073";
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Could not get last modified date from checkPoint string: " + checkPoint);
			throw new IllegalArgumentException("Could not get last modified date from checkPoint string: " + checkPoint);
		}

		return dateString;
	}

	protected String makeCheckpointQueryString(String uuid, String c)
			throws RepositoryException {

		Object[] arguments = { c, uuid};
		String statement = MessageFormat.format(whereClause, arguments);
		logger.log(Level.INFO, "MakeCheckpointQueryString ID: " + uuid);
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
			RepositoryException re = new RepositoryLoginException("Unable to configure parser for parsing XML string", de);
			throw re;
		} catch (SAXException de) {
			logger.log(Level.WARNING, "Unable to parse XML string");
			RepositoryException re = new RepositoryLoginException("Unable to parse XML string", de);
			throw re;
		} catch (IOException de) {
			logger.log(Level.WARNING, "XML source string to be parsed not found.");
			RepositoryException re = new RepositoryLoginException("XML source string to be parsed not found.", de);
			throw re;
		}

	}
}
