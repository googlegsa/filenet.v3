package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileConnectorTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileConnector.login()'
	 */
	public void testLogin() throws RepositoryLoginException, RepositoryException {
	
		FileConnector connec = new FileConnector();
		connec.setLogin(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_uri(TestConnection.uri);
		
		Session fs = connec.login();
		assertNotNull(fs);
		assertTrue(fs instanceof FileSession);
	
	}

}
