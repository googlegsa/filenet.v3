package com.google.enterprise.connector.file.filewrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface ISession {

	public IUser verify() throws RepositoryLoginException, RepositoryException;

	public void setConfiguration(FileInputStream stream);

	public Session getSession();
}
