package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileAuthenticationManagerTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(String,
	 * String)'
	 */
	public void testAuthenticate() throws RepositoryLoginException,
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
		Session sess = (FileSession) connector.login();
		FileAuthenticationManager authentManager = (FileAuthenticationManager) sess
				.getAuthenticationManager();

		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity("ebouvier", "falsePassword"))
				.isValid());
		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity("p8Admin", null)).isValid());
		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity(null, "p@ssw0rd")).isValid());
		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity(null, null)).isValid());

		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity("P8Admin", "UnDeuxTrois456"))
				.isValid());
		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity("P8TestUser", "p@ssw0rd"))
				.isValid());
		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity("P8TestUser2", "p@ssw0rd"))
				.isValid());

	}

}
