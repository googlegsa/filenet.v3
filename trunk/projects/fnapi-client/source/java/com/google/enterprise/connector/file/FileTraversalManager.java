package com.google.enterprise.connector.file;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private ISession fileSession;

	private String order_by = " ORDER BY DateLastModified,Id";

	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>";

	// TODO: add possibility for an administrator to change it
	private String tableName = "Document";

	private String whereClause = " AND ((DateLastModified={0} AND ({1}&lt;id)) OR (DateLastModified>{2} AND ({1}&lt;>id)))";

	private int batchint;

	private String displayUrl;

	private boolean isPublic;

	private String additionalWhereClause;

	private HashSet included_meta;

	private HashSet excluded_meta;

	private static Logger logger;

	{
		logger = Logger.getLogger(FileTraversalManager.class.getName());
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
		objectStoresQuery = MessageFormat.format(objectStoresQuery, args);
		this.isPublic = b;
		this.displayUrl = displayUrl;
		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;

	}

	public DocumentList startTraversal() throws RepositoryException {
		DocumentList set = null;
		ISearch search = fileObjectFactory.getSearch(fileSession);
		String query = buildQueryString(null);
		logger.log(Level.INFO,"query: "+query);
		logger.log(Level.INFO,"objectStore: "+this.objectStore);
		Document resultDoc = this.stringToDom(search.executeXml(query,
				objectStore));
		resultDoc.getElementsByTagName("Id");
		set = new FileDocumentList(resultDoc, objectStore, isPublic,
				displayUrl, this.included_meta, this.excluded_meta);
		return set;
	}

	private String buildQueryString(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer(
				"<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);
		query.append("<querystatement>SELECT Id,DateLastModified  FROM ");
		query.append(tableName);
		query.append(" WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
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

	public DocumentList resumeTraversal(String checkPoint)
			throws RepositoryException {
		DocumentList resultSet = null;
		String queryString = buildQueryString(checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);
		logger.log(Level.INFO,"query: "+queryString);
		logger.log(Level.INFO,"objectStore: "+this.objectStore);
		Document resultDoc = this.stringToDom(search.executeXml(queryString,
				this.objectStore));
		resultSet = new FileDocumentList(resultDoc, objectStore, this.isPublic,
				this.displayUrl, this.included_meta, this.excluded_meta);
		return resultSet;
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

		Object[] arguments = { c, uuid, c };
		String statement = MessageFormat.format(whereClause, arguments);
		logger.info("makeCheckpointQueryString ID: "+uuid);
		return statement;
	}

	private Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
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
