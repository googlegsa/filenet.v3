package com.google.enterprise.connector.file;

import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileAuthenticationManager implements AuthenticationManager {

	IConnection conn;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthenticationManager.class.getName());
	}

	public FileAuthenticationManager(IConnection conn) {
		this.conn = conn;
	}

	public AuthenticationResponse authenticate(
			AuthenticationIdentity authenticationIdentity)
			throws RepositoryException {
		String username = authenticationIdentity.getUsername();
		String password = authenticationIdentity.getPassword();

		IUserContext uc = conn.getUserContext();

		try {
			uc.authenticate(username, password);

		} catch (RepositoryLoginException e) {
			logger.info("Failed to authenticate user " + username);
			return new AuthenticationResponse(false, "");
		}
		logger.info("Authentication succeeded for user " + username);
		
		return new AuthenticationResponse(true, "");

	}

}
