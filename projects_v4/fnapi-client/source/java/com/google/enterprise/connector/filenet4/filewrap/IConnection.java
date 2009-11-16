package com.google.enterprise.connector.filenet4.filewrap;

import com.filenet.api.core.Connection;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IConnection {

	public Connection getConnection() throws RepositoryException;

	public IUserContext getUserContext();

}