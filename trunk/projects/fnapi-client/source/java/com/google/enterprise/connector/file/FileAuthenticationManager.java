package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileAuthenticationManager implements AuthenticationManager {

	IObjectFactory objectFactory;

	String wcmConfigFilePath;

	public FileAuthenticationManager(IObjectFactory object, String wcm) {
		objectFactory = object;
		wcmConfigFilePath = wcm;
	}

	public boolean authenticate(String username, String password)
			throws RepositoryException {
		ISession sess = objectFactory.getSession("gsa-authenticate", null,
				username, password);
		try {
			
			sess.setConfiguration(new FileInputStream(wcmConfigFilePath));
			sess.verify();
		} catch (FileNotFoundException e) {
			RepositoryException re = new RepositoryException(e);
			throw re;
		} catch (LoginException e) {
			// Login failed, user not authenticated
			return false;
		}

		return true;

	}

}
