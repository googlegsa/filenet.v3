package com.google.enterprise.connector.file.filewrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface ISession {

	public IUser verify() throws LoginException, RepositoryException;

	public void setConfiguration(FileInputStream stream);

	public void setObjectStore(IObjectStore objectStore);

}
