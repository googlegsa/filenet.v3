package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectStore;
import com.google.enterprise.connector.filenet3.filejavawrap.FnSearch;
import com.google.enterprise.connector.filenet3.filejavawrap.FnSession;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.ISearch;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

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
	public void testGetSession() throws RepositoryException,
			FileNotFoundException {

		ISession session = objectFactory.getSession("test-getSession",
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		assertNotNull(session);
		assertTrue(session instanceof FnSession);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
		session.verify();

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getObjectStore(String,
	 * ISession)'
	 */
	public void testGetObjectStore() throws FileNotFoundException,
			RepositoryLoginException, RepositoryException {
		ISession session = objectFactory.getSession("test-getObjectStore",
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
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
	public void testGetSearch() throws RepositoryException {
		ISession session = objectFactory.getSession("test-getSearch", "Clear",
				"P8TestUser", "p@ssw0rd");
		ISearch search = objectFactory.getSearch(session);
		assertNotNull(search);
		assertTrue(search instanceof FnSearch);
	}

}
