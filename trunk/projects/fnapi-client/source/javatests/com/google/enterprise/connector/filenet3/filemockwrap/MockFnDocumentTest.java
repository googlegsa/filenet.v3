package com.google.enterprise.connector.filenet3.filemockwrap;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.google.enterprise.connector.filenet3.filewrap.IPermissions;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.pusher.DocPusher;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnDocumentTest extends TestCase {

	MockRepositoryDocument document = new MockRepository(
			new MockRepositoryEventList("MockRepositoryEventLog7.txt"))
			.getStore().getDocByID("doc2");

	MockFnDocument doc = new MockFnDocument(document);

	public void testGetContent() throws RepositoryException {
		InputStream is = doc.getContent();
		byte[] arg0 = new byte[1];
		StringBuffer sb = new StringBuffer();
		try {
			while (is.read(arg0) > 0) {
				sb.append(new String(arg0, "UTF-8"));
			}
		} catch (IOException e) {
			assertEquals(true, false);
		}
		assertEquals(sb.toString(), "This is a document.");
	}

	public void testGetPermissions() {
		IPermissions permissions = doc.getPermissions();

		assertNotNull(permissions);
		assertTrue(permissions instanceof MockFnPermissions);
	}

	public void testGetPropertyStringValue() throws RepositoryException {
		assertEquals("joe", doc.getPropertyStringValue("acl"));
	}

}
