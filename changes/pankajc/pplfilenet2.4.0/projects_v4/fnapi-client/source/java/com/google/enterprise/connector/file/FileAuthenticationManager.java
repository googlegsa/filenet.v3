package com.google.enterprise.connector.file;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * FileNet Authentication Manager. It contains method for authenticating the user while search is performed.
 * @author pankaj_chouhan
 * */
public class FileAuthenticationManager implements AuthenticationManager {

	IConnection conn;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthenticationManager.class.getName());
	}

	public FileAuthenticationManager(IConnection conn) {
		this.conn = conn;
	}

	/**
	 * authenticates the user
	 * @param authenticationIdentity: contains user credentials
	 * */
	public AuthenticationResponse authenticate(AuthenticationIdentity authenticationIdentity)throws RepositoryException {
		String username = authenticationIdentity.getUsername();
		String password = authenticationIdentity.getPassword();

		IUserContext uc = conn.getUserContext();

		try {
			uc.authenticate(username, password);
			logger.info("Authentication Succeeded for user " + username);
			return new AuthenticationResponse(true, "");
		} catch (Throwable e) {
			logger.log(Level.WARNING,"Authentication Failed for user " + username);
			String shortName = FileUtil.getShortName(username);
			logger.log(Level.INFO,"Trying to authenticate with Short Name: " + shortName);
			try{
				uc.authenticate(shortName, password);
				logger.info("Authentication Succeeded for user " + shortName);
				return new AuthenticationResponse(true, "");
			}catch(Throwable th){
				logger.log(Level.WARNING,"Authentication Failed for user " + shortName);
				logger.log(Level.FINE,"While authenticating got exception",th);
				return new AuthenticationResponse(false, "");
			}
		}
	}
}
