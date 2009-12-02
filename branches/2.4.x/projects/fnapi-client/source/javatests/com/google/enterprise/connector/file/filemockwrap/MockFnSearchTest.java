package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class MockFnSearchTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnSearch.executeXml(String,
	 * IObjectStore)'
	 */
	public void testExecuteXml() throws RepositoryLoginException,
			RepositoryException {
		String query = "<?xml version=\"1.0\" ?><request><objectstores mergeoption=\"none\"><objectstore id=\"MockRepositoryEventLog7.txt\"/></objectstores><querystatement>SELECT Id, DateLastModified  FROM Document WHERE IsCurrentVersion=true AND DateLastModified >= 1969-01-01 01:00:00.000 ORDER BY Id,DateLastModified;</querystatement><options maxrecords='1' objectasid=\"false\"/></request>";
		MockFnObjectFactory mockFnObjectFactory = new MockFnObjectFactory();
		MockFnSessionAndObjectStore sessionAndObjectStore = null;// new
		// MockFnSessionAndObjectStore("mark","mark");
		sessionAndObjectStore = (MockFnSessionAndObjectStore) mockFnObjectFactory
				.getSession("mock-filenet", "CLEAR", "mark", "mark");

		mockFnObjectFactory.getObjectStore("MockRepositoryEventLog7.txt",
				sessionAndObjectStore);
		MockFnSearch mockFnSearch = new MockFnSearch();
		String expectedResult = "<rs:data>\n<z:row Id='users'/>\n<z:row Id='doc1'/>\n<z:row Id='doc2'/>\n</rs:data>";
		assertEquals(expectedResult, mockFnSearch.executeXml(query,
				sessionAndObjectStore));

	}

}
