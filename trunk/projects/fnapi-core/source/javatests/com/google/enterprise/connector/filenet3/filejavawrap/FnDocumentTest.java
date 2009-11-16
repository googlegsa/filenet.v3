package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;
import java.io.InputStream;

import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.IPermissions;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FnDocumentTest extends TestCase {

	IDocument doc = null;

	IObjectStore objectStore = null;
	
	ISession session = null;

	protected void setUp() throws Exception {

		IObjectFactory objectFactory = new FnObjectFactory();
		session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
		session.verify();
		objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

		doc = (IDocument) objectStore.getObject(IBaseObject.TYPE_DOCUMENT,
				FnConnection.docId);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getContent()'
	 */
	public void testGetContent() throws RepositoryException {
		InputStream is = doc.getContent();
		assertNotNull(is);
		assertTrue(is instanceof InputStream);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyStringValue(String)'
	 */
	public void testGetPropertyStringValue() throws RepositoryException {

		String mimeTypeExpected = FnConnection.mimeType;
		assertEquals(mimeTypeExpected, doc.getPropertyStringValue("MimeType"));

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyStringValue(String)'
	 */
	public void testGetPropertyStringMultipleValue() throws RepositoryException {
		assertEquals("Emilie", doc.getPropertyStringValue("Authors"));
		doc = (IDocument) objectStore.getObject(IBaseObject.TYPE_DOCUMENT,
				FnConnection.docId3);
		assertEquals("Max, Sylvain", doc.getPropertyStringValue("Authors"));

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPermissions()'
	 */
	public void testGetPermissions() throws RepositoryException {
		IPermissions perms = doc.getPermissions(session.getSession());

		assertEquals(true, perms.authorize(FnConnection.userLambda1));
		assertEquals(true, perms.authorize(FnConnection.userLambda2));
		assertEquals(true, perms.authorize(FnConnection.userLambda3));

	}

}
