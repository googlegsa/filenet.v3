package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.util.Iterator;

import com.google.enterprise.connector.file.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

public class FileDocumentTest extends TestCase {
	IObjectStore objectStore = null;

	protected void setUp() throws Exception {
		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
		session.verify();
		objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperty(String)'
	 */
	public void testFindProperty() throws RepositoryException {

		FileDocument fdpm = new FileDocument(FnConnection.docId2, objectStore,
				false, FnConnection.displayUrl, FnConnection.included_meta,
				FnConnection.excluded_meta,SpiConstants.ActionType.ADD);

		Property property = fdpm.findProperty("google:docid");

		assertTrue(property instanceof FileDocumentProperty);
		assertEquals(FnConnection.docId2, property.nextValue().toString());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentPropertyMap.getProperties()'
	 */
	public void testGetProperties() throws RepositoryException {
		FileDocument fdpm = new FileDocument(FnConnection.docId2, objectStore,
				false, FnConnection.displayUrl, FnConnection.included_meta,
				FnConnection.excluded_meta,SpiConstants.ActionType.ADD);
		// Set set = fdpm.getPropertyNames();

		Iterator properties = fdpm.getPropertyNames().iterator();

		int counter = 0;
		while (properties.hasNext()) {
			properties.next();
			counter++;
		}

		assertEquals(25, counter);

	}

}
