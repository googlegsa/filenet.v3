package com.google.enterprise.connector.file;

import java.io.InputStream;
import java.util.Calendar;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.ValueType;

import junit.framework.TestCase;

public class FileDocumentValueTest extends TestCase {

	Connector connector = null;

	Session sess = null;

	IDocument doc = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnConnection.pathToWcmApiConfig);
		sess = (FileSession) connector.login();
		doc = ((FileSession) sess).getObjectStore().getObject(
				"{8AE0301C-2F52-46FD-B487-FC7A468A902A}");
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentValue.getString()'
	 */
	public void testGetString() throws IllegalArgumentException,
			RepositoryException {
		String stringToGet = "String to get";
		FileDocumentValue fdv = new FileDocumentValue(ValueType.STRING,
				stringToGet);
		FileDocumentValue fileDocumentValue = new FileDocumentValue(
				ValueType.STRING, "MimeType", doc);

		assertEquals(stringToGet, fdv.getString());
		assertEquals("application/msword", fileDocumentValue.getString());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentValue.getStream()'
	 */
	public void testGetStream() throws IllegalArgumentException,
			IllegalStateException, RepositoryException {

		InputStream is = null;
		FileDocumentValue fileDocumentValue = new FileDocumentValue(
				ValueType.BINARY, "content", doc);
		is = fileDocumentValue.getStream();
		assertNotNull(is);
		assertTrue(is instanceof InputStream);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentValue.calendarToIso8601(Calendar)'
	 */
	public void testCalendarToIso8601() {
		Calendar c = Calendar.getInstance();
		c.set(2007, Calendar.JANUARY, 28, 14, 11, 0);
		String milliseconds = c.get(Calendar.MILLISECOND) + "";
		if (milliseconds.length() == 2) {
			milliseconds = "0" + milliseconds;
		}
		String expectedDate = "2007-01-28T14:11:00." + milliseconds;
		String receivedDate = FileDocumentValue.calendarToIso8601(c);
		assertEquals(expectedDate, receivedDate);
	}

}
