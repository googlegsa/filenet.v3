package com.google.enterprise.connector.file.filewrap;

public interface IObjectFactory {

	
	public ISession getSession(String appId, String credTag, String userId, String password);

	public IObjectStore getObjectStore(String string, ISession fileSession);

	public ISearch getSearch(ISession fileSession);
	
	
}
