package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;

public class IFileSession implements ISession {
	
	Session session = null;
	
	public IFileSession(Session sess){
		session = sess;
	}

	public IUser verify() {
		
		return new IFileUser(session.verify());
	}

	public void setConfiguration(FileInputStream stream) {
		session.setConfiguration(stream);
		
	}

	public Session getSession() {
		
		return session;
	}
	

}
