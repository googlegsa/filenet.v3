package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileAuthenticationManager implements AuthenticationManager {

	IObjectFactory objectFactory;

	String wcmConfigFilePath;

	public FileAuthenticationManager(IObjectFactory object, String wcm) {
		objectFactory = object;
		wcmConfigFilePath = wcm;
	}

	public AuthenticationResponse authenticate(
			AuthenticationIdentity authenticationIdentity)
			throws RepositoryException {
		String username = authenticationIdentity.getUsername();
		String password = authenticationIdentity.getPassword();
		ISession sess = objectFactory.getSession("gsa-authenticate", null,
				username, password);
		try {
			sess.setConfiguration(new FileInputStream(wcmConfigFilePath));
			sess.verify();

		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		} catch (RepositoryLoginException e) {
			// Login failed, user not authenticated
			return new AuthenticationResponse(false, "");
		}

		return new AuthenticationResponse(true, "");

	}

}
