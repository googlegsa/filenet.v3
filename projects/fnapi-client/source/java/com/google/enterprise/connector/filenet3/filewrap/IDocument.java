package com.google.enterprise.connector.filenet3.filewrap;

import java.io.InputStream;

import com.filenet.wcm.api.PropertyNotFoundException;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument extends IBaseObject, IGettableObject,
		IReadableMetadataObject, IReadableSecurityObject {

	public InputStream getContent() throws RepositoryDocumentException;

	public IVersionSeries getVersionSeries() throws RepositoryDocumentException;
	
	public String getPropertyValue(String name) throws PropertyNotFoundException;
	
	public IProperties getProperties(String[] names) throws RepositoryDocumentException;


}
