package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IPermissions;

public class MockFnPermissions implements IPermissions {

	String[] users;

	String pub;

	protected MockFnPermissions(String[] users, String pub) {
		this.users = users;
		this.pub = pub;
	}

	/**
	 * TOASK Deal with public property too?
	 */
	public int asMask(String username) {
		for (int i = 0; i < users.length; i++) {
			if (this.users[i].equals(username)) {
				return 1;
			}
		}
		return 0;
	}

}
