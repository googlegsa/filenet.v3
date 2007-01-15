package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.PropertyNotFoundException;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class IFileDocument implements IDocument{
	Document doc;
	
	public IFileDocument(Document doc){
		
		this.doc = doc;		
		this.doc.getPermissionsXML();
	}

	public InputStream getContent() {
		return doc.getContent();
		
	}

	public String getPropertyStringValue(String name) throws RepositoryException {
		try {
			return this.doc.getPropertyStringValue(name);
		} catch (PropertyNotFoundException de) {
			RepositoryException re = new LoginException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}
		
	}

	public double getContentSize() throws RepositoryException{
		try {
			String size = this.doc.getPropertyStringValue(Property.CONTENT_SIZE);
			if(size != null){
				return Double.parseDouble(size);
			}else{
				return 0;
			}
			
		} catch (NumberFormatException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		} catch (PropertyNotFoundException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}
	}

	public Date getPropertyDateValue(String date_last_modified) throws RepositoryException {
		
		try {
			return this.doc.getPropertyDateValue(date_last_modified);
		} catch (PropertyNotFoundException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}
	}

	public String getPermissionsXML() {
		return this.doc.getPermissionsXML();
		
	}

	public IPermissions getPermissions() {
		return new IFilePermissions(doc.getPermissions());
	}

	
}
