package com.google.enterprise.connector.filenet3.filemockwrap;

import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.ISearch;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnObjectFactory implements IObjectFactory {

	private MockFnSessionAndObjectStore mfnOS;

	public ISession getSession(String appId, String credTag, String userId,
			String password) throws RepositoryException {
		this.mfnOS = new MockFnSessionAndObjectStore(userId, password);
		return this.mfnOS;

	}

	public IObjectStore getObjectStore(String objectStoreName,
			ISession fileSession) throws RepositoryException {
		this.mfnOS.valuateEventList(objectStoreName);
		return this.mfnOS;
	}

	public ISearch getSearch(ISession fileSession) {
		if (((MockFnSessionAndObjectStore) fileSession).hasBeenAuthenticated()) {
			return new MockFnSearch();
		} else {
			return null;
		}
	}

}
