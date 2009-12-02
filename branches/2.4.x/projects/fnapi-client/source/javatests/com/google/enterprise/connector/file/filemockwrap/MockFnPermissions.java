package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IPermissions;

public class MockFnPermissions implements IPermissions {

	String[] users;

	protected MockFnPermissions(String[] users) {
		this.users = users;

	}

	/**
	 * TOASK Deal with public property too?
	 */
	public boolean authorize(String username) {
		for (int i = 0; i < users.length; i++) {
			if (this.users[i].equals(username)) {
				return true;
			}
		}
		return false;
	}

}
