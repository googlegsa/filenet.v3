package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;
import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument {

	public InputStream getContent();

	public double getContentSize() throws RepositoryException;

	public IPermissions getPermissions();

	public String getPropertyStringValue(String name)
			throws RepositoryException;

	public long getPropertyLongValue(String name) throws RepositoryException;

	public double getPropertyDoubleValue(String name)
			throws RepositoryException;

	public Date getPropertyDateValue(String name) throws RepositoryException;

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException;

	public IProperties getProperties() throws RepositoryException;

}
