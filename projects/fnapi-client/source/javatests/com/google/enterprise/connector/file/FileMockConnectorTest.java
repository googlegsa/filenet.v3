package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockConnectorTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileConnector.login()'
	 */
	public void testLogin() throws LoginException, RepositoryException {
		Connector connector = new FileConnector();
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnMockConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnMockConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		Session sess = (FileSession) connector.login();
		assertNotNull(sess);
		assertTrue(sess instanceof FileSession);
	}

}
