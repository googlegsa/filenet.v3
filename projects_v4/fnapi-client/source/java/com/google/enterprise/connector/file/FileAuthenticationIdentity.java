package com.google.enterprise.connector.file;

import java.util.Set;

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

	public String getCookie(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String setCookie(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getCookieNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
