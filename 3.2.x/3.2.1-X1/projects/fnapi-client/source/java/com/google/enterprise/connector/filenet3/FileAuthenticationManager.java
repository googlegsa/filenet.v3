package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuthenticationManager implements AuthenticationManager {

	IObjectFactory objectFactory;

	String wcmConfigFilePath;

	private static Logger LOGGER = Logger.getLogger(FileAuthenticationManager.class.getName());

	public FileAuthenticationManager(IObjectFactory object, String wcm) {
		objectFactory = object;
		wcmConfigFilePath = wcm;
	}

	/**
	 * authenticates the user
	 * 
	 * @param authenticationIdentity: contains user credentials
	 */
	public AuthenticationResponse authenticate(
	        AuthenticationIdentity authenticationIdentity)
	        throws RepositoryException {
		LOGGER.log(Level.FINEST, "Entering into authenticate(AuthenticationIdentity authenticationIdentity)");

		String username = authenticationIdentity.getUsername();
		String password = authenticationIdentity.getPassword();
		ISession sess = objectFactory.getSession("gsa-authenticate", null, username, password);

		try {
			URL is = this.getClass().getClassLoader().getResource(this.wcmConfigFilePath);
			if (is == null) {
				LOGGER.log(Level.SEVERE, "WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ");
			}
			String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
			FileInputStream fis = new FileInputStream(sFile);
			sess.setConfiguration(fis);
			sess.verify();
		} catch (IOException exp) {
			LOGGER.log(Level.INFO, "Authentication failed for user " + username);
			return new AuthenticationResponse(false, "");
		} catch (RepositoryLoginException e) {
			// Login failed, user not authenticated
			LOGGER.log(Level.INFO, "Authentication failed for user " + username);
			String shortName = FileUtil.getShortName(username);
			LOGGER.log(Level.INFO, "Trying to authenticate with Short Name: "
			        + shortName);

			sess = objectFactory.getSession("gsa-authenticate", null, shortName, password);
			try {
				URL is = this.getClass().getClassLoader().getResource(this.wcmConfigFilePath);
				if (is == null) {
					LOGGER.log(Level.SEVERE, "WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ");
				}
				String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
				FileInputStream fis = new FileInputStream(sFile);
				sess.setConfiguration(fis);
				sess.verify();
			} catch (Exception ecp) {
				LOGGER.log(Level.WARNING, "Authentication Failed for user "
				        + shortName);
				LOGGER.log(Level.FINE, "While authenticating got exception", ecp);
				return new AuthenticationResponse(false, "");
			}
		}
		LOGGER.log(Level.INFO, "Authentication succeeded for user " + username);
		LOGGER.log(Level.FINEST, "Exiting from authenticate(AuthenticationIdentity authenticationIdentity)");
		return new AuthenticationResponse(true, "");

	}

}
