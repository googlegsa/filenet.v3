package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IObjectFactory {

	public ISession getSession(String appId,String credTag, String userId,
			String password);

	public IObjectStore getObjectStore(String string, ISession fileSession)
			throws RepositoryException, LoginException;

	public ISearch getSearch(ISession fileSession);

}
