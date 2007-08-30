package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.FnConnection;

import junit.framework.TestCase;

public class FnUserTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnUser.getName()'
	 */
	public void testGetName() throws FileNotFoundException {
		Session sess = ObjectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userLambda1,
				FnConnection.userLambdaPassword1);
		sess.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		FnUser user = new FnUser(sess.verify());
		assertEquals(FnConnection.userLambda1, user.getName());
	}

}
