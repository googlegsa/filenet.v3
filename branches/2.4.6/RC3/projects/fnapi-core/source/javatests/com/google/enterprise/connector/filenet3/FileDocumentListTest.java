package com.google.enterprise.connector.filenet3;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

public class FileDocumentListTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	FileDocumentList fdl = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector).setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector).setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector).setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector).setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector).setAdditional_where_clause(FnConnection.additionalWhereClause);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentList.FileDocumentList(Document,
	 * IObjectStore, boolean, String, HashSet, HashSet)'
	 */
	public void testFileDocumentList() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentList.nextDocument()'
	 */
	public void testNextDocument() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() throws RepositoryException {

		FileDocumentList set = (FileDocumentList) qtm.resumeTraversal(FnConnection.checkpoint);
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(FnConnection.checkpoint, set.checkpoint());
	}

	public void testFetchAndVerifyValueForCheckpoint()
			throws RepositoryException {

		/*
		 * public FileDocument(String docId, String timeStamp, IObjectStore
		 * objectStore, boolean isPublic, String displayUrl, HashSet
		 * included_meta, HashSet excluded_meta, SpiConstants.ActionType action)
		 */

		FileDocument pm = new FileDocument(FnConnection.docId,
				FnConnection.date, ((FileSession) sess).getObjectStore(),
				false, FnConnection.displayUrl, FnConnection.included_meta,
				FnConnection.excluded_meta, SpiConstants.ActionType.ADD);
		fdl = (FileDocumentList) qtm.startTraversal();
		String result = fdl.fetchAndVerifyValueForCheckpoint(pm, SpiConstants.PROPNAME_DOCID).nextValue().toString();
		assertEquals(FnConnection.docId, result);
	}

	public void testExtractDocidFromCheckpoint() {
		String checkPoint = "{\"uuid\":\"" + FnConnection.docVsId
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
		assertEquals(FnConnection.docVsId, uuid);

	}

	public void testExtractNativeDateFromCheckpoint() {

		JSONObject jo = null;
		String modifDate = null;

		try {
			jo = new JSONObject(FnConnection.checkpoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: "
							+ FnConnection.checkpoint);
		}

		modifDate = qtm.extractNativeDateFromCheckpoint(jo, FnConnection.checkpoint);
		assertNotNull(modifDate);
		assertEquals(FnConnection.dateForResume, modifDate);

	}

	public void testMakeCheckpointQueryString() throws RepositoryException {
		String uuid = FnConnection.docId;
		String statement = "";
		try {
			statement = qtm.makeCheckpointQueryString(uuid, FnConnection.date);
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnConnection.DM_CHECKPOINT_QUERY_STRING, statement);
	}

}
