package com.google.enterprise.connector.file.filemockwrap;

import java.io.InputStream;
import java.util.Date;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnDocument implements IDocument {

	public InputStream getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getContentSize() throws RepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IPermissions getPermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPropertyStringValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public long getPropertyLongValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getPropertyDoubleValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getPropertyBooleanValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	public IProperties getProperties() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}
}
