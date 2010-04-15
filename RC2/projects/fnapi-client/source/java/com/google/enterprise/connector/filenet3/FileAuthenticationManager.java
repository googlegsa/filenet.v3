package com.google.enterprise.connector.filenet3;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
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

	public AuthenticationResponse authenticate(AuthenticationIdentity authenticationIdentity)
	throws RepositoryException {
		logger.log(Level.FINEST, "Entering into authenticate(AuthenticationIdentity authenticationIdentity)");

		String username = authenticationIdentity.getUsername();
		String password = authenticationIdentity.getPassword();
		ISession sess = objectFactory.getSession("gsa-authenticate", null, username, password);

		try {
			URL is = this.getClass().getClassLoader().getResource(this.wcmConfigFilePath);
			if (is == null) {
				logger.log(Level.SEVERE,"WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ");
			}
			String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
			FileInputStream fis = new FileInputStream(sFile);
			sess.setConfiguration(fis);
			sess.verify();
		} catch (IOException exp) {
			logger.log(Level.INFO, "Authentication failed for user "+username);
			return new AuthenticationResponse(false, "");
		} catch (RepositoryLoginException e) {
			// Login failed, user not authenticated
			logger.log(Level.INFO, "Authentication failed for user "+username);
			return new AuthenticationResponse(false, "");
		}
		logger.log(Level.INFO, "Authentication succeeded for user "+username);
		logger.log(Level.FINEST, "Exiting from authenticate(AuthenticationIdentity authenticationIdentity)");
		return new AuthenticationResponse(true, "");

	}

}
