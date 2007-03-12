package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileAuthenticationManagerTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(String,
	 * String)'
	 */
	public void testAuthenticate() throws LoginException, RepositoryException {
		Connector connector = new FileConnector();

		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnConnection.objectStoreName);
		((FileConnector) connector).setAppId(FnConnection.appId);
		((FileConnector) connector).setCredTag(FnConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnConnection.pathToWcmApiConfig);
		Session sess = (FileSession) connector.login();
		FileAuthenticationManager authentManager = (FileAuthenticationManager) sess
				.getAuthenticationManager();

		assertFalse(authentManager.authenticate("ebouvier", "falsePassword"));
		assertFalse(authentManager.authenticate("p8Admin", null));
		assertFalse(authentManager.authenticate(null, "p@ssw0rd"));
		assertFalse(authentManager.authenticate(null, null));

		assertTrue(authentManager.authenticate("P8Admin", "UnDeuxTrois456"));
		assertTrue(authentManager.authenticate("P8TestUser", "p@ssw0rd"));
		assertTrue(authentManager.authenticate("P8TestUser2", "p@ssw0rd"));

	}

}
