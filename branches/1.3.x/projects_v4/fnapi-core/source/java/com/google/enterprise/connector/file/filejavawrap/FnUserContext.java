package com.google.enterprise.connector.file.filejavawrap;

import java.util.logging.Logger;

import javax.security.auth.Subject;

import com.filenet.api.core.Factory;

import com.filenet.api.security.User;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.file.FileAuthenticationManager;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public class FnUserContext implements IUserContext {

	IConnection conn;
	
	private static Logger logger=null;
	static {
		logger = Logger.getLogger(FnUserContext.class.getName());
	}

	public FnUserContext(IConnection conn) {
		this.conn = conn;
	}

	public String getName() throws RepositoryException {
		User user = Factory.User.fetchCurrent(conn.getConnection(), null);
		return user.get_Name();
	}

	public void authenticate(String username, String password)
			throws RepositoryLoginException {
		
		if (FnCredentialMap.isNull()) 
			FnCredentialMap.init();
		
		if (password == null) {
			if (FnCredentialMap.containsUserCred(username)) {
				password = FnCredentialMap.getUserCred(username);
			}
		}
		
		if (password == null)
			throw new RepositoryLoginException();
		
		UserContext uc = UserContext.get();
		try {
			
			Subject s=UserContext.createSubject(conn.getConnection(),
					username, password, "FileNetP8");
			
			uc.pushSubject(s);
			
			User u=Factory.User.fetchCurrent(conn.getConnection(), null);
			logger.info("User authenticated: "+u.get_Name());
		 
			
		} catch (Exception e) {
			throw new RepositoryLoginException(e);
		}
		
		FnCredentialMap.putUserCred(username,password);
	}

}