package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.filenet3.filewrap.IUser;
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
				FnConnection.completePathToWcmApiConfig));
		IUser user = session.verify();
		assertNotNull(user);
		assertEquals(FnConnection.userLambda1, user.getName());
	}

}
