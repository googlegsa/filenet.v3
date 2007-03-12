package com.google.enterprise.connector.file.filejavawrap;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FnApiDepedenciesTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.google.enterprise.connector.file.filejavawrap");
		//$JUnit-BEGIN$
		suite.addTestSuite(FnSearchTest.class);
		suite.addTestSuite(FnDocumentTest.class);
		suite.addTestSuite(FnPropertyTest.class);
		suite.addTestSuite(FnPermissionsTest.class);
		suite.addTestSuite(FnSessionTest.class);
		suite.addTestSuite(FnPropertiesTest.class);
		suite.addTestSuite(FnObjectStoreTest.class);
		suite.addTestSuite(FnObjectFactoryTest.class);
		//$JUnit-END$
		return suite;
	}
	
	public static void main(String[] args){
		FnApiDepedenciesTests.suite();
	}

}
