package com.google.enterprise.connector.file;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.enterprise.connector.spi.Connector;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

public class FileQueryTraversalManagerTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	FileQueryTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnConnection.pathToWcmApiConfig);
		sess = (FileSession) connector.login();
		qtm = (FileQueryTraversalManager) sess.getQueryTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {
		qtm.setBatchHint(50);
		ResultSet set = this.qtm.startTraversal();
		Iterator iter = set.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			iter.next();
			counter++;
		}
		assertEquals(50, counter);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {

		ResultSet set = this.qtm.resumeTraversal(FnConnection.checkpoint);

		assertNotNull(set);
		Iterator iter = set.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			iter.next();
			counter++;
		}
		assertEquals(17113, counter);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() throws RepositoryException {

		FileDocumentPropertyMap pm = new FileDocumentPropertyMap(
				FnConnection.docId, ((FileSession) sess).getObjectStore(),
				"false", FnConnection.displayUrl);

		assertEquals(FnConnection.checkpoint, qtm.checkpoint(pm));

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.setBatchHint(int)'
	 */
	public void testSetBatchHint() throws RepositoryException {
		this.qtm.setBatchHint(10);
		ResultSet set = this.qtm.startTraversal();
		Iterator iter = set.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			iter.next();
			counter++;
		}
		assertEquals(10, counter);
	}

	public void testFetchAndVerifyValueForCheckpoint()
			throws RepositoryException {

		FileDocumentPropertyMap pm = new FileDocumentPropertyMap(
				FnConnection.docId, ((FileSession) sess).getObjectStore(),
				"false", FnConnection.displayUrl);
		String result = qtm.fetchAndVerifyValueForCheckpoint(pm,
				SpiConstants.PROPNAME_DOCID).getString();
		assertEquals(FnConnection.docId, result);
		result = FileDocumentValue.calendarToIso8601(qtm
				.fetchAndVerifyValueForCheckpoint(pm,
						SpiConstants.PROPNAME_LASTMODIFY).getDate());
		assertEquals(FnConnection.date, result);

	}

	public void extractDocidFromCheckpoint() {
		String checkPoint = "{\"uuid\":\"" + FnConnection.docId
				+ "\",\"lastModified\":\"" + FnConnection.date + "\"}";
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
		assertEquals(FnConnection.docId, uuid);

	}

	public void extractNativeDateFromCheckpoint() {

		JSONObject jo = null;
		String modifDate = null;

		try {
			jo = new JSONObject(FnConnection.checkpoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: "
							+ FnConnection.checkpoint);
		}

		modifDate = qtm.extractNativeDateFromCheckpoint(jo,
				FnConnection.checkpoint);
		assertNotNull(modifDate);
		assertEquals(FnConnection.date, modifDate);

	}

	public void makeCheckpointQueryString() throws RepositoryException {
		String uuid = FnConnection.docId;
		String statement = "";
		try {
			statement = qtm.makeCheckpointQueryString(uuid, FnConnection.date);
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnConnection.checkpoint, statement);

	}

}
