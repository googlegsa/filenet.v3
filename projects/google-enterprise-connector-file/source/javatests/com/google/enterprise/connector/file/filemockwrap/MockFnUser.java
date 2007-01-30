package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IUser;

public class MockFnUser implements IUser {

	String userName;

	protected MockFnUser(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return this.userName;
	}

}
