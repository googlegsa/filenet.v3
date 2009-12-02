package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.FnMockConnection;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class MockFnObjectFactoryTest extends TestCase {

	MockFnObjectFactory objectFactory;

	protected void setUp() throws Exception {
		objectFactory = new MockFnObjectFactory();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnObjectFactory.getSession(String,
	 * String, String, String)'
	 */
	public void testGetSession() throws RepositoryException {
		ISession session = objectFactory.getSession("test-getSession",
				FnMockConnection.credTag, FnMockConnection.userName,
				FnMockConnection.password);
		assertNotNull(session);
		assertTrue(session instanceof MockFnSessionAndObjectStore);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnObjectFactory.getObjectStore(String,
	 * ISession)'
	 */
	public void testGetObjectStore() throws FileNotFoundException,
			RepositoryLoginException, RepositoryException {
		ISession session = objectFactory.getSession("test-getObjectStore",
				FnMockConnection.credTag, FnMockConnection.userName,
				FnMockConnection.password);
		session.setConfiguration(new FileInputStream(
				FnMockConnection.completePathToWcmApiConfig));
		IObjectStore objectStore = objectFactory.getObjectStore(
				FnMockConnection.objectStoreName, session);
		session.verify();
		assertNotNull(objectStore);
		assertTrue(objectStore instanceof MockFnSessionAndObjectStore);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnObjectFactory.getSearch(ISession)'
	 */
	public void testGetSearch() throws RepositoryLoginException,
			RepositoryException {
		ISession session = objectFactory.getSession("test-getSearch",
				FnMockConnection.credTag, FnMockConnection.userName,
				FnMockConnection.password);

		objectFactory.getObjectStore(FnMockConnection.objectStoreName, session);
		session.verify();
		ISearch search = objectFactory.getSearch(session);
		assertNotNull(search);
		assertTrue(search instanceof MockFnSearch);
	}

}
