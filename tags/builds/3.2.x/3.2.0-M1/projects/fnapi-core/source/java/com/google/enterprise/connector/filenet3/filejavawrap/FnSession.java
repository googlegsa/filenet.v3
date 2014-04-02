package com.google.enterprise.connector.filenet3.filejavawrap;

import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.filenet3.filewrap.IUser;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.wcm.api.InvalidCredentialsException;
import com.filenet.wcm.api.Session;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnSession implements ISession {

	Session session = null;
	private static Logger LOGGER = Logger.getLogger(FnDocument.class.getName());

	public FnSession(Session sess) {
		session = sess;
	}

	public IUser verify() throws RepositoryException {
		IUser user = null;

		try {
			user = new FnUser(session.verify());
		} catch (InvalidCredentialsException de) {
			LOGGER.log(Level.WARNING, "Invalid credentials. User not authenticated");
			throw new RepositoryLoginException(de);
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
