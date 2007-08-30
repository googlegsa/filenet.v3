package com.google.enterprise.connector.file.filewrap;

import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IReadableMetadataObject extends IBaseObject {

	public IProperties getProperties() throws RepositoryException;

	public String getPropertyStringValue(String name)
			throws RepositoryException;

	public long getPropertyLongValue(String name) throws RepositoryException;

	public double getPropertyDoubleValue(String name)
			throws RepositoryException;

	public Date getPropertyDateValue(String name) throws RepositoryException;

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException;

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException;

}
