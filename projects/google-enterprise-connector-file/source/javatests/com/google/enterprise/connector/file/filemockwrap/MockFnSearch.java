package com.google.enterprise.connector.file.filemockwrap;

import org.w3c.dom.Document;

import com.google.enterprise.connector.file.Field;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class MockFnSearch implements ISearch {

	public ResultSet executeXml(String query, IObjectStore objectStore,
			Field[] fields) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public Document stringToDom(String xmlSource) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
