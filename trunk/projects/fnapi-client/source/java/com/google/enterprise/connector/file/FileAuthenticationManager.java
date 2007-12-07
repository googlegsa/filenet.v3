package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Logger;

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

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthenticationManager.class.getName());
	}

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
			URL is = this.getClass().getClassLoader().getResource(
					this.wcmConfigFilePath);
			if (is == null) {
				logger.info("null");
			}
			String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
			FileInputStream fis = new FileInputStream(sFile);
			sess.setConfiguration(fis);
			sess.verify();
		} catch (IOException exp) {
			return new AuthenticationResponse(false, "");
		} catch (RepositoryLoginException e) {
			// Login failed, user not authenticated
			return new AuthenticationResponse(false, "");
		}
		logger.info("Authentication succeeded ");
		return new AuthenticationResponse(true, "");

	}

}
