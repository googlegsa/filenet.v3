package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockSessionTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	
	
	protected void setUp() throws Exception {
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
		sess = (FileSession) connector.login();
		
	}
	/*
	 * Test method for 'com.google.enterprise.connector.file.FileSession.getQueryTraversalManager()'
	 */
	public void testGetQueryTraversalManager() throws RepositoryException {
		QueryTraversalManager qtm = ((FileSession)sess).getQueryTraversalManager();
		assertNotNull(qtm);
		assertTrue(qtm instanceof FileQueryTraversalManager);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileSession.getAuthenticationManager()'
	 */
	public void testGetAuthenticationManager() throws RepositoryException {
		AuthenticationManager authent = ((FileSession)sess).getAuthenticationManager();
		assertNotNull(authent);
		assertTrue(authent instanceof FileAuthenticationManager);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileSession.getAuthorizationManager()'
	 */
	public void testGetAuthorizationManager() throws RepositoryException {
		AuthorizationManager authorize = ((FileSession)sess).getAuthorizationManager();
		assertNotNull(authorize);
		assertTrue(authorize instanceof FileAuthorizationManager);
	}

}
