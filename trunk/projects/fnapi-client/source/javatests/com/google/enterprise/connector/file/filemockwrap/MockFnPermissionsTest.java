package com.google.enterprise.connector.file.filemockwrap;

import junit.framework.TestCase;

public class MockFnPermissionsTest extends TestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.filemockwrap.MockFnPermissions.asMask(String)'
	 */
	public void testAsMask() {
		String[]users ={"mark","joe"};
		MockFnPermissions perms = new MockFnPermissions(users);
		assertEquals(1,perms.asMask("mark"));
		assertEquals(0,perms.asMask("clara"));
		
	}

}
