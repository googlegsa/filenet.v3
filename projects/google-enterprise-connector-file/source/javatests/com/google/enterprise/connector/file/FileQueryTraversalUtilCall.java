package com.google.enterprise.connector.file;

import junit.framework.TestCase;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

//import com.google.enterprise.connector.test.QueryTraversalUtil;

public class FileQueryTraversalUtilCall extends TestCase {
	
	private final boolean DFC = true;
	private String user, password, client;

	public void testTraversal() {		
		if (DFC) {
			user="P8Admin";
			password="UnDeuxTrois456";
			client="com.google.enterprise.connector.file.filejavawrap.IFileObjectFactory";
			
		} else {
			user="mark";
			password="mark";
			client="com.google.enterprise.connector.file.filemockwrap.FileMockObjectFactory";
			
		}

		Session session = null;
		Connector connector = null;
		QueryTraversalManager qtm = null;

		connector = new FileConnector();

		
		/**
		 * Simulation of the setters used by Instance.xml
		 */
//		((FileConnector) connector).setLogin(user);
//		((FileConnector) connector).setPassword(password);
//		((FileConnector) connector).setDocbase(docbase);
		IObjectFactory cl = null;
		try {
			cl = (IObjectFactory) Class.forName(client).newInstance();
		} catch (InstantiationException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		}
//		((FileConnector) connector).setIObjectFactory(cl);
		/**
		 * End simulation
		 */
		
		
		try {
			session = (FileSession) connector.login();
			qtm = (FileQueryTraversalManager) session.getQueryTraversalManager();
			FileQueryTraversalUtil.runTraversal(qtm, 100000);

		} catch (LoginException le) {
			System.out.println("Root Cause : " + le.getCause() + " ; Message : " + le.getMessage());
		} catch (RepositoryException re) {
			System.out.println("Root Cause : " + re.getCause() + " ; Message : " + re.getMessage());
		}

	}
}
