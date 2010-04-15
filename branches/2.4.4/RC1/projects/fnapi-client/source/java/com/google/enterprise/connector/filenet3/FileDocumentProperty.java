package com.google.enterprise.connector.filenet3;

import java.util.HashSet;
import java.util.Iterator;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

public class FileDocumentProperty implements Property {

//	private static Logger logger = Logger.getLogger(FileDocumentList.class.getName());
	private String name;

	private Iterator iter;

	public FileDocumentProperty(String refName) {
		this.name = refName;
	}

	public FileDocumentProperty(String refName, HashSet refValue) {
		this.name = refName;
		this.iter = refValue.iterator();
	}

	public Value nextValue() throws RepositoryException {
		Value value = null;

		if (this.iter.hasNext()) {
			value = (Value) this.iter.next();
		}		
		return value;
	}

	public String getName() throws RepositoryException {

		return name;
	}

}
