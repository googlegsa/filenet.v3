package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IObjectFactory {

	public ISession getSession(String appId, String credTag, String userId,
			String password) throws RepositoryException;

	public IObjectStore getObjectStore(String string, ISession fileSession)
			throws RepositoryException, RepositoryLoginException;

	public ISearch getSearch(ISession fileSession);

}
