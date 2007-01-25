package com.google.enterprise.connector.file.filejavawrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.InvalidCredentialsException;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;

public class FnSession implements ISession {

	Session session = null;

	FnObjectStore iObjectStore = null;

	public FnSession(Session sess) {
		session = sess;
	}

	public IUser verify() {
		IUser user = null;
		try {
			user = new FnUser(session.verify());
		} catch (InvalidCredentialsException de) {
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

	public FnObjectStore getIObjectStore() {
		return iObjectStore;
	}

	public void setObjectStore(IObjectStore objectStore) {
		iObjectStore = (FnObjectStore) objectStore;

	}
}
