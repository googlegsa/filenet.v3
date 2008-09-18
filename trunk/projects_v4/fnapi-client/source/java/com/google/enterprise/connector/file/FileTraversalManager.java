package com.google.enterprise.connector.file;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.Factory.ObjectChangeEvent;
import com.filenet.api.util.Id;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectSet;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;

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
		logger.info("Start traversal");
		DocumentList resultSet = null;
		objectStore.refreshSUserContext();
		
		ISearch search = fileObjectFactory.getSearch(objectStore);
		String query = buildQueryString(null);
		
		logger.log(Level.INFO, "Query add : " + query);
		
		IObjectSet objectSet = search.execute(query);

		//Added: document remove all
		ISearch searchToDelete = fileObjectFactory.getSearch(objectStore);
		String queryStringToDelete = buildQueryToDelete(null);
		logger.log(Level.INFO, "Query del : " + queryStringToDelete);
		
		IObjectSet objectSetToDelete = searchToDelete.execute(queryStringToDelete);
				
		resultSet = new FileDocumentList(objectSet, objectSetToDelete, objectStore, isPublic,
				displayUrl, this.included_meta, this.excluded_meta,dateFirstPush,null);
		return resultSet;
	}

	public DocumentList resumeTraversal(String checkPoint) throws RepositoryException {
		logger.info("Resume traversal");
		DocumentList resultSet = null;
		objectStore.refreshSUserContext();
		
		//to add
		String query = buildQueryString(checkPoint);
		
		ISearch search = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query add : " + query);
		IObjectSet objectSet = search.execute(query);
				
		//to delete
		String queryStringToDelete = buildQueryToDelete(checkPoint);
		
		ISearch searchToDelete = fileObjectFactory.getSearch(objectStore);
		logger.log(Level.INFO, "Query del : " + queryStringToDelete);
		IObjectSet objectSetToDelete = search.execute(queryStringToDelete);
		
		resultSet = new FileDocumentList(objectSet, objectSetToDelete, objectStore, this.isPublic,
				this.displayUrl, this.included_meta, this.excluded_meta,dateFirstPush,checkPoint);
		return resultSet;
	}

	private String buildQueryString(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer("SELECT ");
		if (batchint > 0) {
			query.append("TOP " + batchint + " ");
		}
		query.append("d.Id, d.DateLastModified FROM ");
		query.append(tableName);
		query.append(" AS d");
		query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
		query.append(" AND (ISCLASS(d, Document) OR ISCLASS(d, WorkflowDefinition)) ");
		
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
		if(checkpoint != null)
		{
			logger.fine("Checkpoint is not null");
			if (batchint > 0) {
				query.append("TOP " + batchint + " ");
			}
			//GuidConstants.Class_DeletionEvent = Only deleted objects in event table
			query.append(PropertyNames.ID +","+PropertyNames.DATE_CREATED+","+PropertyNames.VERSION_SERIES_ID +" FROM " +GuidConstants.Class_DeletionEvent);
			query.append(getCheckpointClauseToDelete(checkpoint));
			if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
				query.append(additionalWhereClause);
			}
			query.append(orderByToDelete);
		}else{
			//Get the date of today, corresponding to the date of first push
			logger.fine("Checkpoint is null");
			
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			Calendar cal = Calendar.getInstance();
			
			Date d=cal.getTime();
			java.text.SimpleDateFormat dateStandard = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateFirstPush = dateStandard.format(d);
			logger.info("buildQueryToDelete dateFirstPush : " + dateFirstPush);
									
			if (batchint > 0) {
				query.append("TOP " + batchint + " ");
			}
			//GuidConstants.Class_DeletionEvent = Only deleted objects in event table
			query.append(PropertyNames.ID +","+PropertyNames.DATE_CREATED+","+PropertyNames.VERSION_SERIES_ID +" FROM " +GuidConstants.Class_DeletionEvent);
			query.append(" WHERE ");
			if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
				query.append(additionalWhereClause);
			}
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
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
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
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
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
			throw new IllegalArgumentException(
					"could not get uuid from checkPoint string: " + checkPoint);
		}
		return uuid;
	}

	protected String extractNativeDateFromCheckpointToDelete(JSONObject jo,
			String checkPoint) {
		String dateString = null;
		Date d=null;
		try {
			dateString = jo.getString("lastRemoveDate");
			//dateString = "2008-09-04T11:00:16.000";
		}catch (JSONException e) {
			throw new IllegalArgumentException(
					"could not get lastmodify from checkPoint string: "
					+ checkPoint);
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
			throw new IllegalArgumentException(
					"could not get uuid from checkPoint string: " + checkPoint);
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
			throw new IllegalArgumentException(
					"could not get lastmodify from checkPoint string: "
							+ checkPoint);
		}

		return dateString;
	}

	protected String makeCheckpointQueryString(String uuid, String c)
			throws RepositoryException {

		Object[] arguments = { c, uuid};
		String statement = MessageFormat.format(whereClause, arguments);
		return statement;
	}
	private Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
			logger.info("In stringToDom");
			DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

			builder = factory.newDocumentBuilder();

			return builder.parse(new InputSource(new StringReader(xmlSource)));

		} catch (ParserConfigurationException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		} catch (SAXException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		} catch (IOException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		}

	}
}
