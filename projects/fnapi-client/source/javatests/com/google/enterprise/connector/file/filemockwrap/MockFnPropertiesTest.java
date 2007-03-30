package com.google.enterprise.connector.file.filemockwrap;

import java.util.Iterator;

import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;

import junit.framework.TestCase;

public class MockFnPropertiesTest extends TestCase {

	MockFnProperties mockFnProperties = null;
	protected void setUp() throws Exception {
		MockRepositoryDocument document = new MockRepository(
				new MockRepositoryEventList("MockRepositoryEventLog7.txt"))
				.getStore().getDocByID("doc2");
		MockRepositoryPropertyList propList = document.getProplist();
	
		mockFnProperties = new MockFnProperties(propList);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filemockwrap.MockFnProperties.get(int)'
	 */
	public void testGet() {
		IProperty prop = mockFnProperties.get(0);
		assertNotNull(prop);
		assertTrue(prop instanceof MockFnProperty);
		

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filemockwrap.MockFnProperties.iterator()'
	 */
	public void testIterator() {
		
		Iterator itera = mockFnProperties.iterator();
		assertNotNull(itera);
		assertTrue(itera instanceof Iterator);

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filemockwrap.MockFnProperties.size()'
	 */
	public void testSize() {
		assertEquals(2,mockFnProperties.size());
	}

}
