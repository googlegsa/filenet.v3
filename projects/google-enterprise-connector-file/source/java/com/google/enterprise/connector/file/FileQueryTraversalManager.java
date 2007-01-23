package com.google.enterprise.connector.file;

import java.text.MessageFormat;
import java.util.ArrayList;

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
import com.google.enterprise.connector.spi.ValueType;

public class FileQueryTraversalManager implements QueryTraversalManager {

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private ISession fileSession;

//	private String boundedTraversalQuery;
//
//	private String unboundedTraversalQuery;
	
	private String order_by = " ORDER BY DateLastModified;";
	
	private String objectStoresQuery = "<objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>";

	// TODO: add possibility for an administrator to change it
	private String tableName = "Document";
	
	private String whereClause = " AND DateLastModified > {0}";
	
	
	private static final Field[] FIELDS;

    static {
        // ListNodes requires the DataID and PermID columns to be
        // included here. This class requires DataID, OwnerID,
        // ModifyDate, and MimeType.
        ArrayList list = new ArrayList();

        list.add(new Field(
            null, ValueType.BINARY, SpiConstants.PROPNAME_CONTENT));
        list.add(new Field(
            null, ValueType.STRING, SpiConstants.PROPNAME_DISPLAYURL));
        list.add(new Field(
            null, ValueType.BOOLEAN, SpiConstants.PROPNAME_ISPUBLIC));

        list.add(new Field(
            "Id", ValueType.LONG, SpiConstants.PROPNAME_DOCID));
        list.add(new Field(
            "DateLastModified", ValueType.DATE, SpiConstants.PROPNAME_LASTMODIFY));
        list.add(new Field(
            "MimeType", ValueType.STRING, SpiConstants.PROPNAME_MIMETYPE));

        list.add(new Field(
            "DocumentTitle", ValueType.STRING, "DocumentTitle"));
        list.add(new Field(
            "Creator", ValueType.STRING, "Creator"));

        FIELDS = (Field[]) list.toArray(new Field[0]);
    }


	public FileQueryTraversalManager(IObjectFactory fileObjectFactory,
			IObjectStore objectStore, ISession fileSession) {
		this.fileObjectFactory = fileObjectFactory;
		this.objectStore = objectStore;
		this.fileSession = fileSession;
		Object [] args = { objectStore.getName()};
		objectStoresQuery = MessageFormat.format(objectStoresQuery,args);

	}

	public ResultSet startTraversal() throws RepositoryException {
		ISearch search = fileObjectFactory.getSearch(fileSession);
		String query = buildQueryString(null);//unboundedTraversalQuery;
		ResultSet set = null;
		try {
			set = search.executeXml(query, objectStore, FIELDS);
		} catch (RepositoryException e) {
			System.out.println("apres executeQuery ");
			e.printStackTrace();
		}
		return set;
	}

	private String buildQueryString(String checkpoint) throws RepositoryException {
		StringBuffer query = new StringBuffer("<?xml version=\"1.0\" ?><request>");
		query.append(objectStoresQuery);
		query.append("<querystatement>SELECT ");
		for(int i = 0 ; i < FIELDS.length; i++){
			if(FIELDS[i].fieldName != null){
				query.append(FIELDS[i].fieldName + ", ");
			}
		}
		query.delete(query.length()-2,query.length());
		query.append(" FROM ");
		query.append(tableName);
		query.append(" WHERE IsCurrentVersion=true");
		if(checkpoint != null){
			query.append(getCheckpointClause(checkpoint));
		}
		query.append(order_by);
		query.append("</querystatement></request>");
		System.out.println(query.toString());
		return query.toString();
	}

	private String getCheckpointClause(String checkPoint) throws RepositoryException{
		System.out.println("checkpoint vaut " + checkPoint);
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
		System.out.println("queryString vaut " + queryString);
		
		return queryString;
		
	}

	public ResultSet resumeTraversal(String checkPoint)
			throws RepositoryException {
		ResultSet resu = null;
		String queryString = buildQueryString( checkPoint);
		ISearch search = this.fileObjectFactory.getSearch(this.fileSession);
		resu = search.executeXml(queryString, this.objectStore, FIELDS);

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

		Object[] arguments = {c };
		String statement = MessageFormat.format(whereClause,
				arguments);
		return statement;
	}



}
