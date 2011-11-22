package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

public interface ISearch {

	public IObjectSet execute(String query) throws RepositoryException;

}
