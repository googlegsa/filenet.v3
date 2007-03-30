package com.google.enterprise.connector.file;

import java.util.Iterator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockDocumentPropertyMapTest extends TestCase {

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
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperty(String)'
	 */
	public void testGetProperty() throws RepositoryException {
		FileDocumentPropertyMap filePm = new FileDocumentPropertyMap(
				FnMockConnection.FN_ID1,((FileSession)sess).getObjectStore(),"false",FnMockConnection.displayUrl);
		Property property = filePm.getProperty("google:docid");
		assertTrue(property instanceof FileDocumentProperty);
		assertEquals("google:docid", property.getName());
		assertEquals(FnMockConnection.FN_ID1, property.getValue().getString());
	
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperties()'
	 */
	public void testGetProperties() throws RepositoryException {
		FileDocumentPropertyMap filePm = new FileDocumentPropertyMap(
				FnMockConnection.FN_ID1,((FileSession)sess).getObjectStore(),"false",FnMockConnection.displayUrl);

		Iterator iterator = filePm.getProperties();
		int counter = 0;
		while (iterator.hasNext()) {
			iterator.next();
			counter++;
		}
		assertEquals(3, counter);


	}

}
