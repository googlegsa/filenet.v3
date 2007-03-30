package com.google.enterprise.connector.file.filemockwrap;

import junit.framework.TestCase;

public class MockFnUserTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.filemockwrap.MockFnUser.getName()'
	 */
	public void testGetName() {
		MockFnUser user = new MockFnUser("mark");
		assertEquals("mark",user.getName());

	}

}
