package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockAuthenticationManagerTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(String,
	 * String)'
	 */
	public void testAuthenticate() throws RepositoryLoginException,
			RepositoryException {
		Connector connector = new FileConnector();
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObject_store(FnMockConnection.objectStoreName);
		((FileConnector) connector)
				.setWorkplace_display_url(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		((FileConnector) connector)
				.setAdditional_where_clause(FnMockConnection.additionalWhereClause);
		((FileConnector) connector).setIs_public("false");
		Session sess = (FileSession) connector.login();

		FileAuthenticationManager authentManager = (FileAuthenticationManager) sess
				.getAuthenticationManager();

		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK1,
						FnMockConnection.FN_PWD_OK1)).isValid());
		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK2,
						FnMockConnection.FN_PWD_KO)).isValid());
		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK2,
						FnMockConnection.FN_PWD_OK2)).isValid());
		assertFalse(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK2,
						FnMockConnection.FN_PWD_KO)).isValid());
		// assertFalse(authentManager
		// .authenticate(new
		// FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK2,
		// null)).isValid());
		// assertFalse(authentManager.authenticate(new
		// FileAuthenticationIdentity(null,
		// FnMockConnection.FN_PWD_OK1)).isValid());
		// assertFalse(authentManager.authenticate(new
		// FileAuthenticationIdentity(null, null)).isValid());

		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK3,
						FnMockConnection.FN_PWD_OK3)).isValid());
		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK1,
						FnMockConnection.FN_PWD_OK1)).isValid());
		assertTrue(authentManager.authenticate(
				new FileAuthenticationIdentity(FnMockConnection.FN_LOGIN_OK5,
						FnMockConnection.FN_PWD_OK5)).isValid());
	}

}
