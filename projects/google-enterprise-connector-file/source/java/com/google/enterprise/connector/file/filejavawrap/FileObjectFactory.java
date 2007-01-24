package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.ObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.file.filewrap.ISession;

public class FileObjectFactory implements IObjectFactory {
	
	public FileObjectFactory(){
		super();
	}

	
	public ISession getSession(String appId, String credTag, String userId, String password) {
		return new FileSession(ObjectFactory.getSession(appId, credTag, userId, password));
	
	}


	public IObjectStore getObjectStore(String objectStoreName, ISession fileSession) {
		return new FileObjectStore(ObjectFactory.getObjectStore(objectStoreName, fileSession.getSession()));
		
	}


	public ISearch getSearch(ISession fileSession) {
		return new FileSearch(ObjectFactory.getSearch(fileSession.getSession()));
		
	}

	
}
