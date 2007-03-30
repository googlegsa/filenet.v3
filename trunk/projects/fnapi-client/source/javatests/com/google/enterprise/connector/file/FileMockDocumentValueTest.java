package com.google.enterprise.connector.file;

import java.io.InputStream;
import java.util.Calendar;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;


import junit.framework.TestCase;

public class FileMockDocumentValueTest extends TestCase {

	Connector connector = null;

	Session sess = null;
	
	IDocument document;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnMockConnection.objectStoreName);
		((FileConnector) connector).setCredTag(FnMockConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		sess = (FileSession) connector.login();
		document = ((FileSession)sess).getObjectStore().getObject(FnMockConnection.FN_ID1);
		
	}


	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentValue.getString()'
	 */
	public void testGetString() throws IllegalArgumentException, RepositoryException {
		FileDocumentValue fileDocumentValue = new FileDocumentValue(ValueType.STRING,SpiConstants.PROPNAME_DOCID,document );
		assertEquals(FnMockConnection.FN_ID1, fileDocumentValue.getString());
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentValue.getStream()'
	 */
	public void testGetStream() throws IllegalArgumentException, IllegalStateException, RepositoryException {
		InputStream is = null;
		
		
		FileDocumentValue fileDocumentValue = new FileDocumentValue(ValueType.BINARY,SpiConstants.PROPNAME_CONTENT,document);

		is = fileDocumentValue.getStream();
		assertNotNull(is);
		assertTrue(is instanceof InputStream);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentValue.calendarToIso8601(Calendar)'
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
