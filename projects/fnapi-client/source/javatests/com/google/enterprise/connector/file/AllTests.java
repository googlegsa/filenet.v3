package com.google.enterprise.connector.file;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.google.enterprise.connector.file");
		// $JUnit-BEGIN$
		suite.addTestSuite(FileAuthorizationManagerTest.class);
		suite.addTestSuite(FileDocumentValueTest.class);
		suite.addTestSuite(FileAuthenticationManagerTest.class);
		suite.addTestSuite(FileQueryTraversalManagerTest.class);
		suite.addTestSuite(FileConnectorTest.class);
		suite.addTestSuite(FileDocumentPropertyMapTest.class);
		// $JUnit-END$
		return suite;
	}

}
