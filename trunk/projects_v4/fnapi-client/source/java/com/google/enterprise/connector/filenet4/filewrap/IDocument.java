package com.google.enterprise.connector.filenet4.filewrap;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument extends IBaseObject {
	
	public void fetch(Set includedMeta)throws RepositoryDocumentException;

	public IPermissions getPermissions() throws RepositoryException;

	public InputStream getContent() throws RepositoryDocumentException;

	public IVersionSeries getVersionSeries() throws RepositoryDocumentException;;

	public Set getPropertyName() throws RepositoryDocumentException;

	public String getPropertyType(String name) throws RepositoryDocumentException;

	public String getPropertyStringValue(String name)
			throws RepositoryDocumentException;

	public String getPropertyGuidValue(String name) throws RepositoryDocumentException;

	public long getPropertyLongValue(String name) throws RepositoryDocumentException;

	public double getPropertyDoubleValue(String name)
			throws RepositoryDocumentException;

	public Date getPropertyDateValue(String name) throws RepositoryDocumentException;

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryDocumentException;

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryDocumentException;

}
