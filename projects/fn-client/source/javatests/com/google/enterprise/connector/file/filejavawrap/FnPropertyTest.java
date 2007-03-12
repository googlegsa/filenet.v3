package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.file.FnConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.ValueType;

import junit.framework.TestCase;

public class FnPropertyTest extends TestCase {
	IProperty property;

	protected void setUp() throws Exception {
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

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filejavawrap.FnProperty.getValueType()'
	 */
	public void testGetValueType() {
		assertEquals(ValueType.STRING, property.getValueType());
	}

}
