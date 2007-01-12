package com.google.enterprise.connector.file.filewrap;

import org.w3c.dom.Document;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public interface ISearch {

	ResultSet executeXml(String query, IObjectStore objectStore) throws RepositoryException;
	public Document stringToDom(String xmlSource) throws RepositoryException;

}
