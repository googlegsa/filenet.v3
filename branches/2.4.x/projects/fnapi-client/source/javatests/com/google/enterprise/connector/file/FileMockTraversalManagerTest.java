package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

public class FileMockTraversalManagerTest extends TestCase {

	Connector connector = null;

	Session sess = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObject_store(FnMockConnection.objectStoreName);
		// ((FileConnector)
		// connector).setCredential_tag(FnMockConnection.credTag);
		((FileConnector) connector)
				.setWorkplace_display_url(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		((FileConnector) connector)
				.setAdditional_where_clause(FnMockConnection.additionalWhereClause);
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {
		DocumentList resultSet = qtm.startTraversal();
		assertTrue(resultSet instanceof DocumentList);
		int counter = 0;
		while (resultSet.nextDocument() != null) {
			// resultSet.nextDocument();
			counter++;
		}
		assertEquals(27, counter);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {
		DocumentList resultSet = null;
		String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1969-01-01 01:00:00.010\"}";
		resultSet = (DocumentList) qtm.resumeTraversal(checkPoint);
		assertNotNull(resultSet);
		int counter = 0;
		while (resultSet.nextDocument() != null) {
			counter++;
		}
		assertEquals(27, counter);
	}

	public void testResumeTraversalWithSimilarDate() throws RepositoryException {
		DocumentList resultSet = null;

		// String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01
		// 01:00:00.010\"}";

		String checkPoint = "{\"uuid\":\"users\",\"lastModified\":\"1994-11-15 12:45:26.010\"}";

		qtm.setBatchHint(1);
		resultSet = qtm.resumeTraversal(checkPoint);
		// FileDocumentIterator iter = (FileDocumentIterator) resultSet
		// .iterator();
		FileDocument map;
		String docId;
		String modifyDate;
		String[] tabDocIds = { "users" };
		String[] tabTimeStamp = { "1970-01-01T00:00:00.000Z" };
		int i = 0;
		if ((map = (FileDocument) resultSet.nextDocument()) != null) {
			docId = map.findProperty(SpiConstants.PROPNAME_DOCID).nextValue()
					.toString();
			assertEquals(tabDocIds[i], docId);
			modifyDate = map.findProperty(SpiConstants.PROPNAME_LASTMODIFIED)
					.nextValue().toString();
			assertEquals(tabTimeStamp[i], modifyDate);
			i++;
		}
	}

}
