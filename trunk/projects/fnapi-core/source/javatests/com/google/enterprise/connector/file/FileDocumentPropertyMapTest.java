package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.util.Iterator;

import com.google.enterprise.connector.file.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FileDocumentPropertyMapTest extends TestCase {
	IObjectStore objectStore = null;

	protected void setUp() throws Exception {
		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		session.verify();
		objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperty(String)'
	 */
	public void testGetProperty() throws RepositoryException {

		FileDocumentPropertyMap fdpm = new FileDocumentPropertyMap(
				FnConnection.docId, objectStore, "false",
				FnConnection.displayUrl);
		Property property = fdpm.getProperty("Id");
		assertTrue(property instanceof FileDocumentProperty);
		assertEquals("Id", property.getName());
		assertEquals(FnConnection.docId, property.getValue().getString());
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperties()'
	 */
	public void testGetProperties() throws RepositoryException {
		FileDocumentPropertyMap fdpm = new FileDocumentPropertyMap(
				FnConnection.docId, objectStore, "false",
				FnConnection.displayUrl);
		Iterator properties = fdpm.getProperties();

		int counter = 0;
		while (properties.hasNext()) {
			properties.next();
			counter++;
		}
		assertEquals(26, counter);

	}

}
