package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.util.Iterator;

import com.google.enterprise.connector.file.FileResultSet;
import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import junit.framework.TestCase;

public class FnSearchTest extends TestCase {
	ISearch search;

	IObjectStore objectStore;

	protected void setUp() throws Exception {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		session.verify();
		objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

		search = objectFactory.getSearch(session);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnSearch.executeXml(String,
	 * IObjectStore)'
	 */
	public void testExecuteXml() throws RepositoryException {

		String query = "<?xml version=\"1.0\" ?><request>"
				+ "<objectstores mergeoption=\"none\"><objectstore id=\""
				+ FnConnection.objectStoreName
				+ "\"/></objectstores>"
				+ "<querystatement>SELECT Id FROM Document where IsCurrentVersion=true;"
				+ "</querystatement>"
				+ "<options maxrecords='100' objectasid=\"false\"/></request>";
		ResultSet result = search.executeXml(query, objectStore);

		assertTrue(result instanceof FileResultSet);
		Iterator iter = result.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			counter++;
			iter.next();
		}
		assertEquals(100, counter);

	}

}
