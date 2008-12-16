package com.google.enterprise.connector.file.filewrap;

import com.filenet.api.core.ObjectStore;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public interface IObjectStore {

	public IBaseObject getObject(String type, String id)
			throws RepositoryDocumentException;

	public String getName() throws RepositoryException;

	public ObjectStore getObjectStore() throws RepositoryException;
	
	public void refreshSUserContext() throws RepositoryLoginException;
	
	public String getSUserLogin();
	
	public String getSUserPassword();
}
