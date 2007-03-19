package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileConnectorTest extends TestCase {

	protected void setUp() throws Exception {
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnector.login()'
	 */
	public void testLogin() throws LoginException, RepositoryException {
		Connector connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnConnection.pathToWcmApiConfig);

		Session sess = connector.login();
		assertNotNull(sess);
		assertTrue(sess instanceof FileSession);
	}

}
