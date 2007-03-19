package com.google.enterprise.connector.file;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
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
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

public class FileQueryTraversalManager implements QueryTraversalManager {

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private ISession fileSession;

	private String order_by = " ORDER BY Id,DateLastModified;";

	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>";

	// TODO: add possibility for an administrator to change it
	private String tableName = "Document";

	private String whereClause = " AND DateLastModified >= {0} AND Id > {1}";

	private int batchint;

	private String displayUrl;

	private String isPublic;

	private static Logger logger;

	{
		logger = Logger.getLogger(FileQueryTraversalManager.class.getName());
		logger.setLevel(Level.ALL);
	}

	public FileQueryTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, ISession fileSession, String isPublic,
			String displayUrl) throws RepositoryException {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object[] args = { objectStore.getName() };
		objectStoresQuery = MessageFormat.format(objectStoresQuery, args);
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;

	}

	public ResultSet startTraversal() throws RepositoryException {
		ISearch search = fileObjectFactory.getSearch(fileSession);
		String query = buildQueryString(null);
		ResultSet set = null;
		Document resultDoc = this.stringToDom(search.executeXml(query,
				objectStore));
		set = new FileResultSet(resultDoc, objectStore, isPublic, displayUrl);
		return set;
	}

	private String buildQueryString(String checkpoint)
			throws RepositoryException {
		StringBuffer query = new StringBuffer(
				"<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);
		query.append("<querystatement>SELECT Id, DateLastModified  FROM ");
		query.append(tableName);
		query.append(" WHERE IsCurrentVersion=true");
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
		if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL >= 1) {
			logger.info(query.toString());
		}
		return query.toString();
	}

	private String getCheckpointClause(String checkPoint)
			throws RepositoryException {
		if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL >= 1) {
			logger.info(checkPoint);
		}
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

	public ResultSet resumeTraversal(String checkPoint)
			throws RepositoryException {
		ResultSet resultSet = null;
		String queryString = buildQueryString(checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);
		Document resultDoc = this.stringToDom(search.executeXml(queryString,
				this.objectStore));
		resultSet = new FileResultSet(resultDoc, objectStore, this.isPublic,
				this.displayUrl);
		return resultSet;
	}

	public String checkpoint(PropertyMap pm) throws RepositoryException {
		String uuid = fetchAndVerifyValueForCheckpoint(pm,
				SpiConstants.PROPNAME_DOCID).getString();

		Value val = fetchAndVerifyValueForCheckpoint(pm,
				SpiConstants.PROPNAME_LASTMODIFY);
		String nativeFormatDate = FileDocumentValue.calendarToIso8601(val
				.getDate());

		String dateString = nativeFormatDate;

		String result = null;
		try {
			JSONObject jo = new JSONObject();
			jo.put("uuid", uuid);
			jo.put("lastModified", dateString);
			result = jo.toString();
		} catch (JSONException e) {
			throw new RepositoryException("Unexpected JSON problem", e);
		}
		return result;
	}

	public void setBatchHint(int batchHint) throws RepositoryException {
		this.batchint = batchHint;

	}

	protected Value fetchAndVerifyValueForCheckpoint(PropertyMap pm,
			String pName) throws RepositoryException {
		Property property = pm.getProperty(pName);
		if (property == null) {
			throw new IllegalArgumentException("checkpoint must have a "
					+ pName + " property");
		}
		Value value = property.getValue();
		if (value == null) {
			throw new IllegalArgumentException("checkpoint " + pName
					+ " property must have a non-null value");
		}
		return value;
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

		Object[] arguments = { c, uuid };
		String statement = MessageFormat.format(whereClause, arguments);
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
			RepositoryException re = new LoginException(de);
			throw re;
		} catch (SAXException de) {
			RepositoryException re = new LoginException(de);
			throw re;
		} catch (IOException de) {
			RepositoryException re = new LoginException(de);
			throw re;
		}

	}
}
