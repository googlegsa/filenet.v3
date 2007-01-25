package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;

import junit.framework.TestCase;

public class FnSessionTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnSession.verify()'
	 */
	public void testVerify() throws FileNotFoundException {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession("test-verify", "Clear",
				"P8TestUser", "p@ssw0rd");
		session
				.setConfiguration(new FileInputStream(
						"C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties"));
		IUser user = session.verify();
		System.out.println(user.getName());
		assertNotNull(user);
		assertEquals(user.getName(), "P8TestUser@SWORD.FR");
	}

}
