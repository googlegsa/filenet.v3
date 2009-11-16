package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnUser;

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
				FnConnection.completePathToWcmApiConfig));
		FnUser user = new FnUser(sess.verify());
		assertEquals(FnConnection.userLambda1, user.getName());
	}

}
