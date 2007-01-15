package com.google.enterprise.connector.file.filewrap;

import java.io.InputStream;
import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IDocument{

	
	public InputStream getContent();
	public String getPropertyStringValue(String name)    throws RepositoryException;
	public double getContentSize() throws RepositoryException;
	public Date getPropertyDateValue(String date_last_modified) throws RepositoryException;
	public String getPermissionsXML();
	public IPermissions getPermissions();
}
