package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnObjectFactory implements IObjectFactory {

	public ISession getSession(String appId, String credTag, String userId,
			String password) {
		return new MockFnObjectStore(appId, credTag, userId, password);
	}

	public IObjectStore getObjectStore(String string, ISession fileSession)
			throws RepositoryException {
		return null;// new MockFnObjectStore(string, (MockFnSession)
		// fileSession);
	}

	public ISearch getSearch(ISession fileSession) {
		// TODO Auto-generated method stub
		return null;
	}

}
