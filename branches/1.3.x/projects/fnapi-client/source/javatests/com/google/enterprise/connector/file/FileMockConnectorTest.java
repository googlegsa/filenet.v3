package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockConnectorTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnector.login()'
	 */
	public void testLogin() throws RepositoryLoginException,
			RepositoryException {
		Connector connector = new FileConnector();
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
		Session sess = (FileSession) connector.login();
		assertNotNull(sess);
		assertTrue(sess instanceof FileSession);
	}

}
