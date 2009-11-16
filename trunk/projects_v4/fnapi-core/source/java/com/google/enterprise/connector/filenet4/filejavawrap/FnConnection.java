package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.api.core.Connection;
import com.filenet.api.core.Factory;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnConnection implements IConnection {

	Connection conn;

	public FnConnection(String contentEngineUri) throws RepositoryException {
		super();
		
		conn = Factory.Connection.getConnection(contentEngineUri);

	}

	public Connection getConnection() {
		return conn;
	}

	public IUserContext getUserContext() {
		return new FnUserContext(this);
	}

}
