package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.filenet3.filejavawrap.FnProperty;
import com.google.enterprise.connector.filenet3.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IProperty;
import com.google.enterprise.connector.filenet3.filewrap.ISession;

import junit.framework.TestCase;

public class FnPropertiesTest extends TestCase {

	IProperties properties;

	protected void setUp() throws Exception {

		IObjectFactory objectFactory = new FnObjectFactory();
		ISession session = objectFactory.getSession(FnConnection.appId,
				FnConnection.credTag, FnConnection.userName,
				FnConnection.password);
		session.setConfiguration(new FileInputStream(
				FnConnection.completePathToWcmApiConfig));
		session.verify();
		IObjectStore objectStore = objectFactory.getObjectStore(
				FnConnection.objectStoreName, session);

		IDocument doc = (IDocument) objectStore.getObject(
				IBaseObject.TYPE_DOCUMENT, FnConnection.docId);
		properties = doc.getProperties();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnProperties.get(int)'
	 */
	public void testGet() {
		IProperty property = (IProperty) properties.get(0);
		assertTrue(property instanceof FnProperty);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnProperties.size()'
	 */
	public void testSize() {
		assertEquals(51, properties.size());
	}

}
