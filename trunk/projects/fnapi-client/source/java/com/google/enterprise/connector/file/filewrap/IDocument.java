package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument extends IBaseObject, IGettableObject,
		IReadableMetadataObject, IReadableSecurityObject {

	public InputStream getContent() throws RepositoryDocumentException;

	public IVersionSeries getVersionSeries() throws RepositoryException;

	public IProperties getProperties(String[] names) throws RepositoryException;

}
