package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.file.TestConnection;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import junit.framework.TestCase;

public class FnConnectionTest extends TestCase {

	FileSession fs;
	IObjectStore ios;
	IConnection conn;
	UserContext uc;
	IObjectFactory iof;
	IDocument fd;

	protected void setUp() throws RepositoryLoginException, RepositoryException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		FileConnector connec = new FileConnector();
		connec.setLogin(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_uri(TestConnection.uri);
		
		fs = (FileSession)connec.login();

		iof= (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
		conn = iof.getConnection(TestConnection.uri);

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnConnection.getConnection()'
	 */
	public void testGetConnection() throws RepositoryException {
		Connection test = conn.getConnection();
		assertNotNull(test);
		assertEquals(TestConnection.uri, test.getURI());
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnConnection.getUserContext()'
	 */
	public void testGetUserContext() throws RepositoryException {
		IUserContext test = conn.getUserContext();
		assertNotNull(test);
		assertEquals("Administrator@p8.v4",test.getName());
	}

}
