package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument extends IBaseObject, IGettableObject,
		IReadableMetadataObject, IReadableSecurityObject {

	public InputStream getContent() throws RepositoryException;

	public IVersionSeries getVersionSeries();

	public IProperties getProperties(String[] names) throws RepositoryException;

}
