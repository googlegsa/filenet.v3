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

		((FileConnector) connector).setLogin("P8Admin");
		((FileConnector) connector).setPassword("UnDeuxTrois456");
		((FileConnector) connector).setObjectStoreName("GSA_Filenet");
		((FileConnector) connector).setAppId("file-connector");
		((FileConnector) connector).setCredTag("Clear");
		((FileConnector) connector)
				.setDisplayUrl("http://swp-vm-fnet352:8080/Workplace/");
		((FileConnector) connector)
				.setObjectFactory("com.google.enterprise.connector.file.filejavawrap.FnObjectFactory");
		((FileConnector) connector)
				.setPathToWcmApiConfig("C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties");
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
