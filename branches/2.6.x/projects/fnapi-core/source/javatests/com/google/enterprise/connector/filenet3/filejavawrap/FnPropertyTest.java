package com.google.enterprise.connector.filenet3.filejavawrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.filenet3.FnConnection;
import com.google.enterprise.connector.filenet3.filejavawrap.FnObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IProperty;
import com.google.enterprise.connector.filenet3.filewrap.ISession;

import junit.framework.TestCase;

public class FnPropertyTest extends TestCase {
	IProperty property;

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
		IProperties properties = doc.getProperties();
		property = properties.get(1);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnProperty.getName()'
	 */
	public void testGetName() {
		assertEquals("LastModifier", property.getName());
	}

}
