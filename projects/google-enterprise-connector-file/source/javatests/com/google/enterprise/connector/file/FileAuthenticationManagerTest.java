package com.google.enterprise.connector.file;


import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileAuthenticationManagerTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(String, String)'
	 */
	public void testAuthenticate() throws LoginException, RepositoryException{
		Connector myconn = new FileConnector();
		
//		((FileConnector)myconn).setLogin("admin");
//		((FileConnector)myconn).setPassword("UnDeuxTrois456");
		Session sess = (FileSession) myconn.login();
		FileAuthenticationManager authentManager = (FileAuthenticationManager) sess.getAuthenticationManager();
		
		assertFalse(authentManager.authenticate("ebouvier","falsePassword"));
		assertFalse(authentManager.authenticate("p8Admin",null));
		assertFalse(authentManager.authenticate(null,"p@ssw0rd"));
		assertFalse(authentManager.authenticate(null,null));
		
		
		assertTrue(authentManager.authenticate("P8Admin","UnDeuxTrois456"));
		

	}

}
