package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;

public class FileMockObjectFactory implements IObjectFactory {

	public ISession getSession(String appId, String credTag, String userId,
			String password) {
		// TODO Auto-generated method stub
		return null;
	}

	public IObjectStore getObjectStore(String string, ISession fileSession) {
		// TODO Auto-generated method stub
		return null;
	}

	public ISearch getSearch(ISession fileSession) {
		// TODO Auto-generated method stub
		return null;
	}

}
