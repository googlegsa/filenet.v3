package com.google.enterprise.connector.filenet3.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IObjectStore {

	public IGettableObject getObject(int type, String guidOrPath);

	public String getName() throws RepositoryException;

}
