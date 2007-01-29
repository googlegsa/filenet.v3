package com.google.enterprise.connector.file;

import java.util.Iterator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;

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
		((FileConnector) connector).setAppId(FnConnection.appId);
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
		assertEquals(0, counter);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() throws RepositoryException {

		FileDocumentPropertyMap pm = new FileDocumentPropertyMap(
				FnConnection.docId, ((FileSession) sess).getObjectStore());

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

}
