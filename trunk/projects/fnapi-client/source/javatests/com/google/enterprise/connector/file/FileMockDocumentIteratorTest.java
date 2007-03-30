package com.google.enterprise.connector.file;

import java.util.Iterator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

public class FileMockDocumentIteratorTest extends TestCase {

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
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentIterator.hasNext()'
	 */
	public void testHasNext() throws RepositoryException {
		ResultSet resultSet = qtm.startTraversal();
		Iterator iter = resultSet.iterator();
		boolean rep = iter.hasNext();
		assertTrue(rep);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentIterator.next()'
	 */
	public void testNext() throws RepositoryException {
		int counter = 0;
		ResultSet resultSet = qtm.startTraversal();
		PropertyMap pm = null;
		Property prop = null;
		Iterator iter = resultSet.iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();
			assertTrue(obj instanceof PropertyMap);
			assertTrue(obj instanceof FileDocumentPropertyMap);
			
			pm = (PropertyMap) obj;
			prop = pm.getProperty(SpiConstants.PROPNAME_DOCID);

			assertNotNull(prop);

			if (counter == 2) {

				break;
			}

		}
	}

}
