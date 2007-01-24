package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument{

	
	public InputStream getContent();
	public double getContentSize() throws RepositoryException;
	public IPermissions getPermissions();
}
