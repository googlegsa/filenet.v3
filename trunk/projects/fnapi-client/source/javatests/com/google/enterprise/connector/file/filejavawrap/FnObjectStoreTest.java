package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FnObjectStoreTest extends TestCase {

	private IObjectStore objectStore;

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
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectStore.getObject(String)'
	 */
	public void testGetObject() throws RepositoryException {

		IDocument doc = objectStore.getObject(FnConnection.docId);
		assertNotNull(doc);
		assertEquals(FnConnection.docIdTitle, doc
				.getPropertyStringValue("Name"));
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectStore.getName()'
	 */
	public void testGetName() {
		assertEquals(FnConnection.objectStoreName, objectStore.getName());
	}

}
