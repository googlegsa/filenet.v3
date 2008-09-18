package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.AuthenticationIdentity;

public class FileAuthenticationIdentity implements AuthenticationIdentity {

	private String username;

	private String password;

	public FileAuthenticationIdentity(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
