package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileAuthenticationManager implements AuthenticationManager {
	
	public FileAuthenticationManager(){
		
	}

	public boolean authenticate(String username, String password)
			throws LoginException, RepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

}
