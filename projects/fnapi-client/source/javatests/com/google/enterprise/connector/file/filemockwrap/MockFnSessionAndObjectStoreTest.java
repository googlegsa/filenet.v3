package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.file.FnMockConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;

import junit.framework.TestCase;

public class MockFnSessionAndObjectStoreTest extends TestCase {

	MockFnSessionAndObjectStore sessAndObjectStore = null;

	protected void setUp() throws Exception {
		IObjectFactory objectFactory = new MockFnObjectFactory();
		sessAndObjectStore = (MockFnSessionAndObjectStore) objectFactory
				.getSession("mock", FnMockConnection.credTag,
						FnMockConnection.userName, FnMockConnection.password);
		sessAndObjectStore.setConfiguration(new FileInputStream(
				FnMockConnection.completePathToWcmApiConfig));

		sessAndObjectStore = (MockFnSessionAndObjectStore) objectFactory
				.getObjectStore(FnMockConnection.objectStoreName,
						sessAndObjectStore);
		sessAndObjectStore.verify();

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnSessionAndObjectStore.getObject(String)'
	 */
	public void testGetObject() {
		IDocument document = sessAndObjectStore.getObject("doc2");
		assertNotNull(document);
		assertTrue(document instanceof MockFnDocument);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnSessionAndObjectStore.verify()'
	 */
	public void testVerify() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.filemockwrap.MockFnSessionAndObjectStore.getName()'
	 */
	public void testGetName() {

	}

}
