package com.google.enterprise.connector.filenet4.filejavawrap;

import com.filenet.api.core.Connection;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
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
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);
		
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
