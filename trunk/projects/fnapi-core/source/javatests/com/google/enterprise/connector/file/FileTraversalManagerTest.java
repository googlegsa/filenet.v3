package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileTraversalManagerTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	FileDocumentList fdl = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector)
				.setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector)
				.setAdditional_where_clause(FnConnection.additionalWhereClause);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {
		qtm.setBatchHint(50);
		DocumentList set = this.qtm.startTraversal();
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(14, counter);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {
		qtm.setBatchHint(50);
		DocumentList set = this.qtm.resumeTraversal(FnConnection.checkpoint2);
		assertNotNull(set);
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(13, counter);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.setBatchHint(int)'
	 */
	public void testSetBatchHint() throws RepositoryException {
		this.qtm.setBatchHint(10);
		DocumentList set = this.qtm.startTraversal();
		int counter = 0;
		while (set.nextDocument() != null) {
			counter++;
		}
		assertEquals(10, counter);
	}

}
