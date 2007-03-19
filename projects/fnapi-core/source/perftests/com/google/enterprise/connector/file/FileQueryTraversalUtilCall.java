package com.google.enterprise.connector.file;

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileQueryTraversalUtilCall extends TestCase {

	private final boolean javaapi = true;

	private String user, password, client, credTag, objectStoreName,
			pathToWcmApiConfig, displayUrl;

	private String isPublic;

	public void testTraversal() {
		if (javaapi) {
			user = "P8Admin";
			password = "UnDeuxTrois456";
			client = "com.google.enterprise.connector.file.filejavawrap.FnObjectFactory";
			credTag = "Clear";
			objectStoreName = "GSA_Filenet";
			pathToWcmApiConfig = "C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties";
			displayUrl = "http://swp-vm-fnet352:8080/Workplace/";
			isPublic = "false";

		} else {
			user = "mark";
			password = "mark";
			credTag = "";
			objectStoreName = "";
			pathToWcmApiConfig = "";
			displayUrl = "";
			client = "com.google.enterprise.connector.file.filemockwrap.FileMockObjectFactory";
			isPublic = "false";

		}

		Session session = null;
		Connector connector = null;
		QueryTraversalManager qtm = null;

		connector = new FileConnector();

		/**
		 * Simulation of the setters used by Instance.xml
		 */
		((FileConnector) connector).setLogin(user);
		((FileConnector) connector).setPassword(password);
		((FileConnector) connector).setObjectStoreName(objectStoreName);
		((FileConnector) connector).setCredTag(credTag);
		((FileConnector) connector).setDisplayUrl(displayUrl);
		((FileConnector) connector).setObjectFactory(client);
		((FileConnector) connector).setPathToWcmApiConfig(pathToWcmApiConfig);
		((FileConnector) connector).setIsPublic(isPublic);
		/**
		 * End simulation
		 */

		try {
			session = (FileSession) connector.login();
			qtm = (FileQueryTraversalManager) session
					.getQueryTraversalManager();
			FileQueryTraversalUtil.runTraversal(qtm, 1000);

		} catch (LoginException le) {
			System.out.println("Root Cause : " + le.getCause()
					+ " ; Message : " + le.getMessage());
		} catch (RepositoryException re) {
			System.out.println("Root Cause : " + re.getCause()
					+ " ; Message : " + re.getMessage());
		}

	}
}
