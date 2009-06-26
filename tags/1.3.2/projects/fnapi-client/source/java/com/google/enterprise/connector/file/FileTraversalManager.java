package com.google.enterprise.connector.file;

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

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
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
	private ISession fileSession;
	private String order_by = " ORDER BY DateLastModified,Id";
	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>";
	// TODO: add possibility for an administrator to change it
	private String tableName = "Document";
	//private String whereClause = " AND ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&lt;{2} AND ({1}&lt;>id)))";
	//private String whereClause = " AND ((DateLastModified={0} AND ({1}<id)) OR (DateLastModified<{2} AND ({1}<>id)))";
	private String whereClause = " AND ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&gt;{0}))";
	private String tableNameEventToDelete = "Event";
	//private String whereClauseToDelete = " WHERE ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseToDelete = " WHERE ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified&gt;{0}))";
	private String whereClauseToDeleteOnlyDate = " WHERE (DateLastModified&gt;{0})";
	private String orderByToDelete  = " ORDER BY DateLastModified,Id";
	private String displayUrl;
	private String additionalWhereClause;
	private int batchint;
	private boolean isPublic;
	private HashSet included_meta;
	private HashSet excluded_meta;
	private final static String eventAllias = " e";
	private final static String AND_OPERATOR = " AND ";
	private final static String EVENT_DELETE = "DeletionEvent";

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

	public DocumentList resumeTraversal(String checkPoint) throws RepositoryException {
		//TODO:Execute query to get the removed documents
		DocumentList resultSet = null;
		String queryString = buildQueryString(checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);
		logger.log(Level.INFO, "query: " + queryString);
		logger.log(Level.INFO, "objectStore: " + this.objectStore);
		
		Document resultDoc = this.stringToDom(search.executeXml(queryString,
					this.objectStore));
			
		
		logger.info("String before stringToDom" + search.executeXml(queryString,objectStore));
		//Added: document remove all
		String queryStringToDelete = buildQueryToDelete(checkPoint);
		ISearch searchToDelete = this.fileObjectFactory.getSearch(this.fileSession);
		logger.log(Level.INFO, "query to get removed documents: " + queryStringToDelete);
		logger.log(Level.INFO, "objectStore: " + this.objectStore);
		logger.log(Level.INFO, "searchToDelete: " + searchToDelete);
				
		Document resultDocToDelete = this.stringToDom(searchToDelete.executeXml(queryStringToDelete,
				this.objectStore));
		
		logger.info("StringDelete before stringToDom" + search.executeXml(queryStringToDelete,objectStore));
		logger.info("number of docs "+resultDoc.getElementsByTagName("z:row").getLength());
		//Make the document list
		if ((resultDoc.getElementsByTagName("z:row").getLength() > 0)||(resultDocToDelete.getElementsByTagName("z:row").getLength() > 0)) {
			logger.info("in if length > 0");
			resultSet = new FileDocumentList(resultDoc,resultDocToDelete, objectStore, this.isPublic,
					this.displayUrl, this.included_meta, this.excluded_meta,dateFirstPush,checkPoint);
		}	
		logger.info("before return");
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
		
		///query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL and DateLastModified>20071030T094545Z");
		
		if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
			query.append(additionalWhereClause);
		}
		if (checkpoint != null) {
			query.append(getCheckpointClause(checkpoint));
		}
		
		query.append(order_by);
		
		query.append("</querystatement>");
		if (batchint > 0) {
			query.append("<options maxrecords='" + batchint
					+ "' objectasid=\"false\"/>");
		}
		query.append("</request>");
		logger.info(query.toString());

		return query.toString();
	}

	private String buildQueryToDelete(String checkpoint) throws RepositoryException {
		
		logger.fine("Build query to get the documents removed : ");
		StringBuffer query= null;
		if(checkpoint != null)
		{
			query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
			query.append(objectStoresQuery);
			query.append("<querystatement>SELECT Id,DateLastModified,VersionSeriesId  FROM ");
			///query.append("<querystatement>SELECT Id,CONVERT(VARCHAR,DateLastModified,126),VersionSeriesId FROM ");
			query.append(tableNameEventToDelete);
			query.append(eventAllias);
			
			query.append(getCheckpointClauseToDelete(checkpoint));
			query.append(AND_OPERATOR);
			query.append(isClassSQLFunction(eventAllias, EVENT_DELETE));
			query.append(orderByToDelete);
					
			query.append("</querystatement>");
			if (batchint > 0) {
				query.append("<options maxrecords='" + batchint
						+ "' objectasid=\"false\"/>");
			}
			query.append("</request>");
			logger.info("query in buildQueryToDelete"+ query.toString());
		}else{
			//Get the date of today, corresponding to the date of first push
			
			/*
			java.util.Date d = new java.util.Date();
			java.text.SimpleDateFormat dateStandard = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateFirstPush = dateStandard.format(d);
			logger.info("Date of first push :"+ dateStandard.format(d));	
			*/
			///
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			///Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Calendar cal = Calendar.getInstance();
			logger.info("buildQueryToDelete cal : " + cal);
			
			Date d=cal.getTime();
			java.text.SimpleDateFormat dateStandard = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateFirstPush = dateStandard.format(d);
			logger.info("buildQueryToDelete dateFirstPush : " + dateFirstPush);
			///
			
			
			query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
			query.append(objectStoresQuery);
			query.append("<querystatement>SELECT Id,DateLastModified,VersionSeriesId  FROM ");
			query.append(tableNameEventToDelete);
			query.append(eventAllias);
			query.append(" WHERE ");
			////////////////////////////////////////////////////////////////////////
			// a enlever
			query.append(" (DateLastModified >" + dateFirstPush + ")");	
			///query.append(" (DateLastModified >" + "2008-06-16T14:20:41.006" + ")");	 
			
			query.append(AND_OPERATOR);
			query.append(isClassSQLFunction(eventAllias, EVENT_DELETE));
			////////////////////////////////////////////////////////////////////////			
			
//			if (additionalWhereClause != null && !additionalWhereClause.equals("")) {
//				query.append(additionalWhereClause);
//			}
			
			query.append(orderByToDelete);
			
			query.append("</querystatement>");
			if (batchint > 0) {
				query.append("<options maxrecords='" + batchint
						+ "' objectasid=\"false\"/>");
			}
			query.append("</request>");
			logger.info("buildQueryToDelete : "+query.toString());
		}
		
		return query.toString();		
	}



	private String getCheckpointClause(String checkPoint)
	throws RepositoryException {

		logger.info(checkPoint);

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

		logger.info(checkPoint);

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
//		Date d=null;
		try {
			dateString = jo.getString("lastRemoveDate");
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
		logger.info("makeCheckpointQueryString date: " + c);
		logger.info("makeCheckpointQueryString ID: " + uuid);
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
		logger.info("makeCheckpointQueryString ID: " + uuid);
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
	
	private String isClassSQLFunction(String eventAllias, String eventType){
		return new String("IsClass("+eventAllias+", "+eventType+" )");
	}
}
