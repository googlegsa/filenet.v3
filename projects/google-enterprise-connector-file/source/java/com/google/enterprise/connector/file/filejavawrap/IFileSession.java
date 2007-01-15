package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.InvalidCredentialsException;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class IFileSession implements ISession {
	
	Session session = null;
	
	public IFileSession(Session sess){
		session = sess;
	}

	public IUser verify() {
		IUser user = null;
		try{
			user =new IFileUser(session.verify());
		}catch(InvalidCredentialsException de){
			return null;
		}
		return user;
	}

	public void setConfiguration(FileInputStream stream) {
		session.setConfiguration(stream);
		
	}

	public Session getSession() {
		
		return session;
	}
	

}
