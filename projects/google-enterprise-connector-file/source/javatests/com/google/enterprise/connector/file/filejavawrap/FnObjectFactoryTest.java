package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;

import junit.framework.TestCase;

public class FnObjectFactoryTest extends TestCase {

	FnObjectFactory objectFactory;

	protected void setUp() throws Exception {

		objectFactory = new FnObjectFactory();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getSession(String,
	 * String, String, String)'
	 */
	public void testGetSession() {

		ISession session = objectFactory.getSession("test-getSession",
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		assertNotNull(session);
		assertTrue(session instanceof FnSession);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getObjectStore(String,
	 * ISession)'
	 */
	public void testGetObjectStore() throws FileNotFoundException {
		ISession session = objectFactory.getSession("test-getObjectStore",
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		session.verify();
		IObjectStore objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);
		assertNotNull(objectStore);
		assertTrue(objectStore instanceof FnObjectStore);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getSearch(ISession)'
	 */
	public void testGetSearch() {
		ISession session = objectFactory.getSession("test-getSearch", "Clear",
				"P8TestUser", "p@ssw0rd");
		ISearch search = objectFactory.getSearch(session);
		assertNotNull(search);
		assertTrue(search instanceof FnSearch);
	}

}
