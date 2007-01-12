package com.google.enterprise.connector.file;

import java.util.List;

import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class FileAuthorizationManager implements AuthorizationManager {
	
	public FileAuthorizationManager(){
		
	}

	public ResultSet authorizeDocids(List docidList, String username)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet authorizeTokens(List tokenList, String username)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
