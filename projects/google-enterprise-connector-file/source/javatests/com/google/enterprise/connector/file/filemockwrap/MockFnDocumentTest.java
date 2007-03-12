package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.pusher.DocPusher;

public class MockFnDocumentTest extends TestCase {
	
	MockRepositoryDocument document = new MockRepository(
			new MockRepositoryEventList("MockRepositoryEventLog7.txt"))
			.getStore().getDocByID("doc2");

	public void testGetContent() {
		InputStream is;
		try {
			is = document.getContentStream();
		} catch (FileNotFoundException e) {
			assertEquals(true, false);
			return;
		}
		byte[] arg0 = new byte[1];
		StringBuffer sb = new StringBuffer();
		try {
			while (is.read(arg0) > 0) {
				sb.append(new String(arg0, DocPusher.XML_DEFAULT_ENCODING));
			}
		} catch (IOException e) {
			assertEquals(true, false);
		}
		assertEquals(sb.toString(), "This is a document.");
	}

	public void testGetContentSize() {
		assertEquals(document.getContent().length(), "This is a document."
				.length());
	}

	public void testGetPermissions() {
		MockRepositoryPropertyList mrPL = document.getProplist();
		// "acl":{type:string, value:[fred,mark,bill]}
		// "google:ispublic":"false"
		String pub = mrPL.lookupStringValue("google:ispublic");
		assertEquals(pub, "public");
	}

	/**
	 * TOTEST carrefully
	 */
	public void testGetPropertyStringValue() {
		MockRepositoryProperty curProp = document.getProplist().getProperty(
				"acl");
		if (curProp.getType() == MockRepositoryProperty.PropertyType.STRING
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			assertEquals(curProp.getValue(), "joe");
		}
	}

}
