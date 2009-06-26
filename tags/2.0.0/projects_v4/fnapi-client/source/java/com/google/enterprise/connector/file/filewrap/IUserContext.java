package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public interface IUserContext {

	public String getName() throws RepositoryException;

	void authenticate(String username, String password)
			throws RepositoryLoginException;

}
