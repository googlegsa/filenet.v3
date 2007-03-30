package com.google.enterprise.connector.file;

import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import junit.framework.TestCase;

public class FileMockQueryTraversalManagerTest extends TestCase {

	Connector connector = null;

	Session sess = null;

	FileQueryTraversalManager qtm = null;
	
	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnMockConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnMockConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		sess = (FileSession) connector.login();
		qtm = (FileQueryTraversalManager) sess.getQueryTraversalManager();
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {
		ResultSet resultSet = qtm.startTraversal();	
		assertTrue(resultSet instanceof FileResultSet);
		Iterator iter = resultSet.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			iter.next();
			counter++;
		}
		assertEquals(27, counter);

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {
		FileResultSet resultSet = null;
		String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1969-01-01 01:00:00.010\"}";
		resultSet = (FileResultSet) qtm.resumeTraversal(checkPoint);
		assertNotNull(resultSet);
		int counter = 0;
		for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
			iter.next();
			counter++;
		}
		assertEquals(27, counter);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() {
		String uuid = "doc2";
		String statement = "";

		try {
			statement = qtm.makeCheckpointQueryString(uuid,
					"1970-01-01 01:00:00.020");
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
	}

		/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.fetchAndVerifyValueForCheckpoint(PropertyMap, String)'
	 */
	public void testFetchAndVerifyValueForCheckpoint() throws IllegalArgumentException, RepositoryException {
		FileDocumentPropertyMap propertyMap = new FileDocumentPropertyMap("doc2",((FileSession)sess).getObjectStore(),"false","");
		Calendar calDate = null;
		Value value = qtm.fetchAndVerifyValueForCheckpoint(propertyMap,
				SpiConstants.PROPNAME_LASTMODIFY);
		calDate = Calendar.getInstance();
		calDate.set(1970,0,1,1,0,0);
		
		assertTrue(value instanceof FileDocumentValue);
		assertEquals(calDate.getTime().toString(),value.getDate().getTime().toString());
		
		value = (FileDocumentValue) qtm.fetchAndVerifyValueForCheckpoint(propertyMap,
				SpiConstants.PROPNAME_DOCID);
		assertTrue(value instanceof FileDocumentValue);
		assertEquals("doc2",value.getString());
		
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractDocidFromCheckpoint(JSONObject, String)'
	 */
	public void testExtractDocidFromCheckpoint() {
		String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01 01:00:00.020\"}";
		String uuid = null;
		JSONObject jo = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
		}

		uuid = qtm.extractDocidFromCheckpoint(jo, checkPoint);
		assertNotNull(uuid);
		assertEquals(uuid, "doc2");
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractNativeDateFromCheckpoint(JSONObject, String)'
	 */
	public void testExtractNativeDateFromCheckpoint() {
		String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01 01:00:00.020\"}";
		JSONObject jo = null;
		String modifDate = null;

		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
		}

		modifDate = qtm.extractNativeDateFromCheckpoint(jo, checkPoint);
		assertNotNull(modifDate);
		assertEquals(modifDate, "1970-01-01 01:00:00.020");
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileQueryTraversalManager.makeCheckpointQueryString(String, String)'
	 */
	public void testMakeCheckpointQueryString() {
		String uuid = "doc2";
		String statement = "";

		try {
			statement = qtm.makeCheckpointQueryString(uuid,
					"1970-01-01 01:00:00.020");
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
	}
	
	public void testResumeTraversalWithSimilarDate() throws RepositoryException {
		ResultSet resultSet = null;

		String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01 01:00:00.010\"}";

		qtm.setBatchHint(1);
		resultSet = qtm.resumeTraversal(checkPoint);

		FileDocumentIterator iter = (FileDocumentIterator) resultSet
				.iterator();
		FileDocumentPropertyMap map ;
		String docId ;
		String modifyDate;
		String [] tabDocIds = {"users","doc1","doc2"};
		String [] tabTimeStamp = {"1970-01-01T01:00:00.000","1970-01-01T01:00:00.010","1970-01-01T01:00:00.010"};
		int i = 0;
		while (iter.hasNext()) {
			map = (FileDocumentPropertyMap) iter
					.next();
			docId = map.getProperty(SpiConstants.PROPNAME_DOCID)
					.getValue().getString();
			assertEquals(tabDocIds[i], docId);			
			modifyDate = FileDocumentValue.calendarToIso8601(map
					.getProperty(SpiConstants.PROPNAME_LASTMODIFY).getValue()
					.getDate());
			assertEquals(tabTimeStamp[i], modifyDate);
			i++;
		}
	}

}
