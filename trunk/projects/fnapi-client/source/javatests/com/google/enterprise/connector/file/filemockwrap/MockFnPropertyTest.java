package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.file.FnMockConnection;
import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.file.filewrap.ISession;

import junit.framework.TestCase;

public class MockFnPropertyTest extends TestCase {
	IProperty property;

	protected void setUp() throws Exception {
		IObjectFactory objectFactory = new MockFnObjectFactory();
		ISession session = objectFactory.getSession("mock",
				FnMockConnection.credTag, FnMockConnection.userName,
				FnMockConnection.password);
		session.setConfiguration(new FileInputStream(
				FnMockConnection.completePathToWcmApiConfig));

		IObjectStore objectStore = objectFactory.getObjectStore(
				FnMockConnection.objectStoreName, session);
		session.verify();
		IDocument doc = (IDocument) objectStore.getObject(
				IBaseObject.TYPE_DOCUMENT, "doc2");
		IProperties properties = doc.getProperties();
		property = properties.get(1);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnProperty.getName()'
	 */
	public void testGetName() {
		assertEquals("google:ispublic", property.getName());

	}

	// /*
	// * Test method for
	// 'com.google.enterprise.connector.file.filemockwrap.MockFnProperty.getValueType()'
	// */
	// public void testGetValueType() {
	// assertEquals(ValueType.STRING,property.getValueType());
	//	
	// }

}
