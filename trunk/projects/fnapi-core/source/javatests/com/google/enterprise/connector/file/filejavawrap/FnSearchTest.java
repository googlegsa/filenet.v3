package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

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
				FnConnection.completePathToWcmApiConfig));
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
		String result = search.executeXml(query, objectStore);

		Document resultDocument = this.stringToDom(result);

		assertTrue(resultDocument instanceof Document);

	}

	private Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			builder = factory.newDocumentBuilder();

			return builder.parse(new InputSource(new StringReader(xmlSource)));

		} catch (ParserConfigurationException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		} catch (SAXException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		} catch (IOException de) {
			RepositoryException re = new RepositoryLoginException(de);
			throw re;
		}

	}

}
