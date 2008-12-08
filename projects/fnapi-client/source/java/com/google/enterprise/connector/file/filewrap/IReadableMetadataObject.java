package com.google.enterprise.connector.file.filewrap;

import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IReadableMetadataObject extends IBaseObject {

	public IProperties getProperties() throws RepositoryDocumentException;

	public String getPropertyStringValue(String name)
			throws RepositoryDocumentException;

	public long getPropertyLongValue(String name) throws RepositoryDocumentException;

	public double getPropertyDoubleValue(String name)
			throws RepositoryDocumentException;

	public Date getPropertyDateValue(String name) throws RepositoryDocumentException;

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryDocumentException;

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryDocumentException;

}
