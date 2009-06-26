package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileAuthenticationIdentity;
import com.google.enterprise.connector.file.FileAuthenticationManager;
import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileSession;

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import junit.framework.TestCase;

public class FileAuthenticationManagerTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(AuthenticationIdentity)'
	 */
	public void testAuthenticate() throws RepositoryLoginException, RepositoryException  {

		FileConnector connec = new FileConnector();
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);
		
		FileSession fs = (FileSession)connec.login();
		FileAuthenticationManager fatm = (FileAuthenticationManager) fs.getAuthenticationManager();		
		
//		Check FileAuthenticationIdentity
		FileAuthenticationIdentity fai = new FileAuthenticationIdentity(TestConnection.username, TestConnection.password);
		assertEquals(TestConnection.username, fai.getUsername());
		assertEquals(TestConnection.password, fai.getPassword());
		
//		Check FileAuthenticationManager
		AuthenticationResponse ar = fatm.authenticate(fai);
		assertEquals(true, ar.isValid());
		
//		Check FileAuthenticationManager for a wrong user
		FileAuthenticationIdentity faiWrong = new FileAuthenticationIdentity(TestConnection.username, TestConnection.wrongPassword);
		AuthenticationResponse arWrong = fatm.authenticate(faiWrong);
		assertEquals(false, arWrong.isValid());
				
	}

}
