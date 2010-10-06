package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IObjectFactory {

	public IConnection getConnection(String contentEngineUri)
			throws RepositoryException;

	public IObjectStore getObjectStore(String objectStoreName,
			IConnection connection, String userId, String password)
			throws RepositoryException, RepositoryLoginException;

	public ISearch getSearch(IObjectStore objectStore)
			throws RepositoryException;

}
