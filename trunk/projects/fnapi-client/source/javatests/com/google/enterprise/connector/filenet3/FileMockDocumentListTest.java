package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import junit.framework.TestCase;

public class FileMockDocumentListTest extends TestCase {

	Connector connector = null;

	Session sess = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector).setObject_store(FnMockConnection.objectStoreName);
		// ((FileConnector)
		// connector).setCredential_tag(FnMockConnection.credTag);
		((FileConnector) connector).setWorkplace_display_url(FnMockConnection.displayUrl);
		((FileConnector) connector).setObject_factory(FnMockConnection.objectFactory);
		((FileConnector) connector).setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() {
		String uuid = "doc2";
		String statement = "";

		try {
			statement = qtm.makeCheckpointQueryString(uuid, "1970-01-01 01:00:00.020");
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.fetchAndVerifyValueForCheckpoint(PropertyMap,
	 * String)'
	 */
	public void testFetchAndVerifyValueForCheckpoint()
			throws IllegalArgumentException, RepositoryException {
		FileDocument propertyMap = new FileDocument("doc2",
				((FileSession) sess).getObjectStore(), false, "",
				FnMockConnection.included_meta, FnMockConnection.excluded_meta,
				SpiConstants.ActionType.ADD);
		Calendar calDate = null;

		FileDocumentList fdl = (FileDocumentList) qtm.startTraversal();
		DateValue value = (DateValue) fdl.fetchAndVerifyValueForCheckpoint(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED).nextValue();
		// Value value = qtm.fetchAndVerifyValueForCheckpoint(propertyMap,
		// SpiConstants.PROPNAME_LASTMODIFIED);
		calDate = Calendar.getInstance();
		calDate.set(1970, 0, 1, 1, 0, 0);

		assertTrue(value instanceof ValueImpl);
		// assertEquals(calDate.getTime().toString(),value.getDate().getTime().toString());
		assertEquals("1970-01-01T00:00:00.020Z", value.toIso8601());

		StringValue value2 = (StringValue) fdl.fetchAndVerifyValueForCheckpoint(propertyMap, SpiConstants.PROPNAME_DOCID).nextValue();
		assertTrue(value2 instanceof StringValue);
		assertEquals("doc2", value2.toString());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractDocidFromCheckpoint(JSONObject,
	 * String)'
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
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractNativeDateFromCheckpoint(JSONObject,
	 * String)'
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
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.makeCheckpointQueryString(String,
	 * String)'
	 */
	public void testMakeCheckpointQueryString() {
		String uuid = "doc2";
		String statement = "";

		try {
			statement = qtm.makeCheckpointQueryString(uuid, "1970-01-01 01:00:00.020");
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
	}

}
