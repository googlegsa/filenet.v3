package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.ISession;

import junit.framework.TestCase;

public class FnPermissionsTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnPermissions.asMask(String)'
	 */
	public void testAsMask() throws FileNotFoundException {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.pathToWcmApiConfig));
		session.verify();
		IObjectStore objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

		IDocument doc = objectStore.getObject(FnConnection.docId);

		IPermissions permissions = doc.getPermissions();
		assertNotNull(permissions);
		assertEquals(1, permissions.asMask(FnConnection.userLambda1));

	}

}
