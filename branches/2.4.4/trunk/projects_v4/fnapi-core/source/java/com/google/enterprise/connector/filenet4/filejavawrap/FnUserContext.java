package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import com.filenet.api.core.Factory;
import com.filenet.api.security.User;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

/**
 * FileNet user context
 * @author amit_kagrawal
 * */
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
		logger.config("user name from connection: "+user.get_Name());
		return user.get_Name();
	}

	public void authenticate(String username, String password)
			throws RepositoryLoginException {
		
		if (FnCredentialMap.isNull()){
			logger.info("Initializing the FileNet credentials...");
			FnCredentialMap.init();
		}
		
		if (password == null) {
			if (FnCredentialMap.containsUserCred(username)) {
				password = FnCredentialMap.getUserCred(username);
			}
		}
		if (password == null){
			logger.log(Level.WARNING, "Password is NULL");
			throw new RepositoryLoginException();
		}
		
		UserContext uc = UserContext.get();
		try {
			
			Subject s=UserContext.createSubject(conn.getConnection(), username, password, "FileNetP8");
			uc.pushSubject(s);
			User u=Factory.User.fetchCurrent(conn.getConnection(), null);
			logger.info("User: "+u.get_Name()+ " is authenticated");
			
		} catch (RepositoryException e) {
			logger.log(Level.WARNING, "Unable to GET connection");
			throw new RepositoryLoginException(e);
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Unable to GET connection or user is not authenticated");
			throw new RepositoryLoginException(e);
		}
		
		FnCredentialMap.putUserCred(username,password);
	}

}