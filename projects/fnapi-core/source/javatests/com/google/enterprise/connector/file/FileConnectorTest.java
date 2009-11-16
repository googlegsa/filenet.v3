package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
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
	public void testLogin() throws RepositoryLoginException,
			RepositoryException {
		Connector connector = new FileConnector();
		((FileConnector) connector).setUsername(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector)
				.setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector).setIs_public("false");
		Session sess = connector.login();
		assertNotNull(sess);
		assertTrue(sess instanceof FileSession);
	}

}
