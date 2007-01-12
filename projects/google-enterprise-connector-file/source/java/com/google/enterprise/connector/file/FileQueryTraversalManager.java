package com.google.enterprise.connector.file;

import java.text.MessageFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
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

	private String boundedTraversalQuery;

	private String unboundedTraversalQuery;

	public FileQueryTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, ISession fileSession) {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object [] args = { objectStore.getName()};
		boundedTraversalQuery = MessageFormat.format(FileInstantiator.QUERY_STRING_BOUNDED_DEFAULT, args);
		unboundedTraversalQuery = MessageFormat.format(FileInstantiator.QUERY_STRING_UNBOUNDED_DEFAULT, args);
	}

	public ResultSet startTraversal() throws RepositoryException {
		ISearch search = fileObjectFactory.getSearch(fileSession);
		String query = unboundedTraversalQuery;
		ResultSet set = search.executeXml(query, objectStore);
		return set;
	}

	public ResultSet resumeTraversal(String checkPoint)
			throws RepositoryException {
		System.out.println("checkpoint vaut " + checkPoint);
		JSONObject jo = null;
		ResultSet resu = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
		}
		String uuid = extractDocidFromCheckpoint(jo, checkPoint);
		String c = extractNativeDateFromCheckpoint(jo, checkPoint);
		String queryString = makeCheckpointQueryString(uuid, c);
		System.out.println("queryString vaut " + queryString);

		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);
		resu = search.executeXml(queryString, this.objectStore);

		return resu;
	}

	public String checkpoint(PropertyMap pm) throws RepositoryException {
		String uuid = fetchAndVerifyValueForCheckpoint(pm,
				SpiConstants.PROPNAME_DOCID).getString();

		String nativeFormatDate = fetchAndVerifyValueForCheckpoint(pm,
				SpiConstants.PROPNAME_LASTMODIFY).getString();

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
		// TODO Auto-generated method stub

	}

	public Value fetchAndVerifyValueForCheckpoint(PropertyMap pm, String pName)
			throws RepositoryException {
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

	public String extractDocidFromCheckpoint(JSONObject jo, String checkPoint) {
		String uuid = null;
		try {
			uuid = jo.getString("uuid");
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"could not get uuid from checkPoint string: " + checkPoint);
		}
		return uuid;
	}

	public String extractNativeDateFromCheckpoint(JSONObject jo,
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

	public String makeCheckpointQueryString(String uuid, String c)
			throws RepositoryException {

		Object[] arguments = { null,c };
		String statement = MessageFormat.format(boundedTraversalQuery,
				arguments);
		return statement;
	}



}
