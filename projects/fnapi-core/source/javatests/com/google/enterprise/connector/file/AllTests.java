package com.google.enterprise.connector.file;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.google.enterprise.connector.file");
		// $JUnit-BEGIN$
		suite.addTestSuite(FileMockAuthorizationManagerTest.class);
		suite.addTestSuite(FileDocumentValueTest.class);
		suite.addTestSuite(FileMockAuthenticationManagerTest.class);
		suite.addTestSuite(FileQueryTraversalManagerTest.class);
		suite.addTestSuite(FileConnectorTest.class);
		suite.addTestSuite(FileMockDocumentPropertyMapTest.class);
		// $JUnit-END$
		return suite;
	}

}
