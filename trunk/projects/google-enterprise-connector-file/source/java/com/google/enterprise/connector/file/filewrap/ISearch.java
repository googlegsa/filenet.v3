package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public interface ISearch {

	ResultSet executeXml(String query, IObjectStore objectStore)
			throws RepositoryException;

}
