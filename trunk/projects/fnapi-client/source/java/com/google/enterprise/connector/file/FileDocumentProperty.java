package com.google.enterprise.connector.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

public class FileDocumentProperty implements Property {

	private String name;

	private FileDocumentValue fileDocumentValue;

	public FileDocumentProperty(String name, FileDocumentValue fileDocumentValue) {
		this.name = name;
		this.fileDocumentValue = fileDocumentValue;
	}

	public FileDocumentProperty(String name) {
		this.name = name;
	}

	public String getName() throws RepositoryException {

		return name;
	}

	public Value getValue() throws RepositoryException {
		return fileDocumentValue;
	}

	public Iterator getValues() throws RepositoryException {
		List l = new ArrayList(1);
		l.add(fileDocumentValue);
		return l.iterator();
	}

}
