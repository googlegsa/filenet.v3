package com.google.enterprise.connector.filenet3.filemockwrap;

import com.google.enterprise.connector.filenet3.filewrap.IUser;

public class MockFnUser implements IUser {

	String userName;

	protected MockFnUser(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return this.userName;
	}

}
