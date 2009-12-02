package com.google.enterprise.connector.file;

import java.util.StringTokenizer;
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
 * @author amit_kagrawal
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
		} catch (Throwable e) {
			logger.log(Level.WARNING,"Authentication Failed for user " + username);
			
//			tokenize the string on comma
			StringTokenizer strtok = new StringTokenizer(username,",");
			while ((null!=strtok) && (strtok.hasMoreTokens())){

				String mytok1 = strtok.nextToken();
				if(null!=mytok1){
					//filter for the shortened name
					StringTokenizer innerToken = new StringTokenizer(mytok1,"=");
					if((null!=innerToken)&&(innerToken.countTokens()==2)){
						String key = innerToken.nextToken();
						if(null!=key){
							if((key.equalsIgnoreCase("cn"))||(key.equalsIgnoreCase("uid"))){
								String shortUserName = innerToken.nextToken();
//								System.out.println("User: "+shortUserName);
								logger.info("Trying user Authentication with simple name: "+shortUserName);
								
								try{
									uc.authenticate(shortUserName, password);
									return new AuthenticationResponse(true, ""); 
								}catch(Throwable th){
									logger.log(Level.WARNING,"Authentication Failed for user " + shortUserName);
									logger.log(Level.FINE,"While authenticating got exception",th);
									return new AuthenticationResponse(false, "");
								}
								
							}
						}
					}
				}//end:if(null!=mytok1){
			}//end: while
			
			return new AuthenticationResponse(false, "");
		}
		
		logger.info("Authentication Succeeded for user " + username);
		return new AuthenticationResponse(true, "");

	}

}
