package com.google.enterprise.connector.filenet4.filewrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public interface ISession {

	public IUser verify() throws RepositoryLoginException, RepositoryException;

	public void setConfiguration(FileInputStream stream);

	public Session getSession();
}
