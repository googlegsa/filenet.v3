package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockSessionTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnMockConnection.userName);
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
		sess = (FileSession) connector.login();

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileSession.getQueryTraversalManager()'
	 */
	public void testGetQueryTraversalManager() throws RepositoryException {
		TraversalManager qtm = ((FileSession) sess).getTraversalManager();
		assertNotNull(qtm);
		assertTrue(qtm instanceof FileTraversalManager);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileSession.getAuthenticationManager()'
	 */
	public void testGetAuthenticationManager() throws RepositoryException {
		AuthenticationManager authent = ((FileSession) sess)
				.getAuthenticationManager();
		assertNotNull(authent);
		assertTrue(authent instanceof FileAuthenticationManager);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileSession.getAuthorizationManager()'
	 */
	public void testGetAuthorizationManager() throws RepositoryException {
		AuthorizationManager authorize = ((FileSession) sess)
				.getAuthorizationManager();
		assertNotNull(authorize);
		assertTrue(authorize instanceof FileAuthorizationManager);
	}

}
