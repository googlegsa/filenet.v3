package com.google.enterprise.connector.file;

import java.util.Iterator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

public class FileMockDocumentTest extends TestCase {

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
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperty(String)'
	 */
	public void testFindProperty() throws RepositoryException {
		FileDocument filePm = new FileDocument(FnMockConnection.FN_ID1,
				((FileSession) sess).getObjectStore(), false,
				FnMockConnection.displayUrl, FnMockConnection.included_meta,
				FnMockConnection.excluded_meta,SpiConstants.ActionType.ADD);
		Property property = filePm.findProperty("google:docid");
		assertTrue(property instanceof FileDocumentProperty);
		Iterator fileProp = filePm.getPropertyNames().iterator();
		if (fileProp.hasNext()) {
			System.out.println(fileProp.next());
			assertEquals("google:ispublic", fileProp.next());
		}

		assertEquals(FnMockConnection.FN_ID1, property.nextValue().toString());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperties()'
	 */
	public void testGetPropertyNames() throws RepositoryException {
		FileDocument filePm = new FileDocument(FnMockConnection.FN_ID1,
				((FileSession) sess).getObjectStore(), false,
				FnMockConnection.displayUrl, FnMockConnection.included_meta,
				FnMockConnection.excluded_meta,SpiConstants.ActionType.ADD);

		Iterator iterator = filePm.getPropertyNames().iterator();
		int counter = 0;
		while (iterator.hasNext()) {
			iterator.next();
			counter++;
		}
		assertEquals(0, counter);

	}

}
