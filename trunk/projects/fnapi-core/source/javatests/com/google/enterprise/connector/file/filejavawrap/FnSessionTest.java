package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FnSessionTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnSession.verify()'
	 */
	public void testVerify() throws FileNotFoundException,
			RepositoryLoginException, RepositoryException {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession("test-verify",
				FnConnection.credTag, FnConnection.userLambda1,
				FnConnection.userLambdaPassword1);
		session.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		IUser user = session.verify();
		assertNotNull(user);
		assertEquals(FnConnection.userLambda1, user.getName());
	}

}
