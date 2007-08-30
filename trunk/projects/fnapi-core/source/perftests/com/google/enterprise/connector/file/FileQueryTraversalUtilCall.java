package com.google.enterprise.connector.file;

import junit.framework.TestCase;

import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileQueryTraversalUtilCall extends TestCase {

	private final boolean javaapi = true;

	private String user, password, client, objectStoreName, pathToWcmApiConfig,
			displayUrl;

	private String isPublic;

	private String additionalWhereClause;

	public void testTraversal() {
		if (javaapi) {
			user = "P8Admin";
			password = "UnDeuxTrois456";
			client = "com.google.enterprise.connector.file.filejavawrap.FnObjectFactory";

			objectStoreName = "GSA_Filenet";
			pathToWcmApiConfig = "C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties";
			displayUrl = "http://swp-vm-fnet352:8080/Workplace/properties/ObjectInfo.jsp";
			isPublic = "false";
			additionalWhereClause = "and Document.This INSUBFOLDER '/testdata'";

		} else {
			user = "mark";
			password = "mark";
			objectStoreName = "";
			pathToWcmApiConfig = "";
			displayUrl = "";
			client = "com.google.enterprise.connector.file.filemockwrap.FileMockObjectFactory";
			isPublic = "false";

		}

		Session session = null;
		Connector connector = null;
		TraversalManager qtm = null;

		connector = new FileConnector();

		/**
		 * Simulation of the setters used by Instance.xml
		 */
		((FileConnector) connector).setLogin(user);
		((FileConnector) connector).setPassword(password);
		((FileConnector) connector).setObject_store(objectStoreName);
		((FileConnector) connector).setWorkplace_display_url(displayUrl);
		((FileConnector) connector).setObject_factory(client);
		((FileConnector) connector).setPath_to_WcmApiConfig(pathToWcmApiConfig);
		((FileConnector) connector).setIs_public(isPublic);
		((FileConnector) connector)
				.setAdditional_where_clause(additionalWhereClause);
		/**
		 * End simulation
		 */

		try {
			session = (FileSession) connector.login();
			qtm = (FileTraversalManager) session.getTraversalManager();
			FileQueryTraversalUtil.runTraversal(qtm, 1000);

		} catch (RepositoryLoginException le) {
			System.out.println("Root Cause : " + le.getCause()
					+ " ; Message : " + le.getMessage());
		} catch (RepositoryException re) {
			System.out.println("Root Cause : " + re.getCause()
					+ " ; Message : " + re.getMessage());
		} catch (PushException e) {
			e.printStackTrace();
		}

	}
}
