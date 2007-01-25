package com.google.enterprise.connector.file.filejavawrap;

import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;

import junit.framework.TestCase;

public class FnObjectFactoryTest extends TestCase {

	FnObjectFactory objectFactory;

	protected void setUp() throws Exception {

		super.setUp();
		objectFactory = new FnObjectFactory();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getSession(String,
	 * String, String, String)'
	 */
	public void testGetSession() {

		ISession session = objectFactory.getSession("test-getSession", "Clear",
				"P8TestUser", "p@ssw0rd");
		assertNotNull(session);
		assertTrue(session instanceof FnSession);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getObjectStore(String,
	 * ISession)'
	 */
	public void testGetObjectStore() {
		ISession session = objectFactory.getSession("test-getObjectStore",
				"Clear", "P8TestUser", "p@ssw0rd");
		IObjectStore objectStore = objectFactory.getObjectStore("GSA_Filenet",
				session);
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
