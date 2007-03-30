package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.InvalidCredentialsException;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.spi.LoginException;

public class FnSession implements ISession {

	Session session = null;

	public FnSession(Session sess) {
		session = sess;
	}

	public IUser verify() throws LoginException {
		IUser user = null;
		try {
			user = new FnUser(session.verify());
		} catch (InvalidCredentialsException de) {
			throw new LoginException(de);
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
