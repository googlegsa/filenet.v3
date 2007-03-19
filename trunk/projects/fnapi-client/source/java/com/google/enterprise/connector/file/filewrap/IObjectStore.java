package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IObjectStore {

	public IDocument getObject(String guidOrPath);

	public String getName() throws RepositoryException;


}
