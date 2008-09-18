package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument extends IBaseObject {
	
	public void fetch(Set includedMeta)throws RepositoryException;

	public IPermissions getPermissions() throws RepositoryException;

	public InputStream getContent() throws RepositoryException;

	public IVersionSeries getVersionSeries() throws RepositoryException;;

	public Set getPropertyName() throws RepositoryException;

	public String getPropertyType(String name) throws RepositoryException;

	public String getPropertyStringValue(String name)
			throws RepositoryException;

	public String getPropertyGuidValue(String name) throws RepositoryException;

	public long getPropertyLongValue(String name) throws RepositoryException;

	public double getPropertyDoubleValue(String name)
			throws RepositoryException;

	public Date getPropertyDateValue(String name) throws RepositoryException;

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException;

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException;

}
