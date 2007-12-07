package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;
import java.io.InputStream;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FnDocumentTest extends TestCase {

	IDocument doc = null;

	IObjectStore objectStore = null;

	protected void setUp() throws Exception {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
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
	public void testGetPermissions() {
		IPermissions perms = doc.getPermissions();

		assertEquals(1, perms.asMask(FnConnection.userLambda1));
		assertEquals(1, perms.asMask(FnConnection.userLambda2));
		assertEquals(1, perms.asMask(FnConnection.userLambda3));

	}

}
