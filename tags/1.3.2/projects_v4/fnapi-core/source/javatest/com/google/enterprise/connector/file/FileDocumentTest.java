package com.google.enterprise.connector.file;

import java.util.Iterator;

import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileDocument;
import com.google.enterprise.connector.file.FileDocumentProperty;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import junit.framework.TestCase;

public class FileDocumentTest extends TestCase {

	FileSession fs;
	IObjectStore ios;
	IConnection conn;
	UserContext uc;
	IObjectFactory iof;
	
	protected void setUp() throws Exception {
		
		FileConnector connec = new FileConnector();
		connec.setLogin(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_uri(TestConnection.uri);
		
		fs = (FileSession)connec.login();

		iof= (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
		IConnection conn = iof.getConnection(TestConnection.uri);
		Domain domain = Factory.Domain.getInstance(conn.getConnection(), "P8.V4");
		ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);

	}

	
	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocument.findProperty(String)'
	 */
	public void testFindProperty() throws RepositoryException {
				
		FileDocument fd = new FileDocument(TestConnection.docId1, null, ios, false, TestConnection.displayURL, TestConnection.included_meta, TestConnection.excluded_meta, ActionType.ADD);
		
		Property prop = fd.findProperty("Id");

		assertTrue(prop instanceof FileDocumentProperty);
		assertEquals(TestConnection.docId1, prop.nextValue().toString());

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocument.getPropertyNames()'
	 */
	public void testGetPropertyNames() throws RepositoryException {

		FileDocument fd = new FileDocument(TestConnection.docId2, null, ios,
			false, TestConnection.displayURL, TestConnection.included_meta,
				TestConnection.excluded_meta, ActionType.ADD);
	//	 Set set = fdpm.getPropertyNames();

		Iterator properties = fd.getPropertyNames().iterator();

		int counter = 0;
		while (properties.hasNext()) {
			properties.next();
			counter++;
		}

		assertEquals(19, counter);

	}

}
