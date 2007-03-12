package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.BaseRuntimeException;
import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.PropertyNotFoundException;
import com.google.enterprise.connector.dctm.DctmConnector;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnDocument implements IDocument {
	Document doc;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());

		logger.setLevel(Level.ALL);
	}

	public FnDocument(Document doc) {

		this.doc = doc;

	}

	public InputStream getContent() {
		try {
			return doc.getContent();
		} catch (BaseRuntimeException e) {

			logger.log(Level.SEVERE,
					"error while trying to get the content of file "
							+ this.doc.getId() + " " + e.getMessage());
			return doc.getContent();
		} catch (Error er) {
			logger.log(Level.SEVERE,
					"error while trying to get the content of file "
							+ this.doc.getId() + " " + er.getMessage());
			return doc.getContent();
		}

	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyStringValue(name);

		} catch (PropertyNotFoundException de) {

			if (name.equals(Property.MIME_TYPE)) {

				return "application/octet-stream";
			} else if (name.equals(Property.PERMISSIONS)) {

				return "security";
			} else {
				logger.log(Level.SEVERE,
						"error while trying to get the property " + name
								+ " of the file " + this.doc.getId() + " "
								+ de.getMessage());
				RepositoryException re = new RepositoryException(de);
				throw re;
			}

		} catch (Exception de) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ de.getMessage());
			RepositoryException re = new RepositoryException(de);
			throw re;

		} catch (Error er) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ er.getMessage());
			RepositoryException re = new RepositoryException(er);
			throw re;
		}

	}

	public double getContentSize() throws RepositoryException {
		try {
			String size = this.doc
					.getPropertyStringValue(Property.CONTENT_SIZE);
			if (size != null) {
				return Double.parseDouble(size);
			} else {
				return 0;
			}

		} catch (NumberFormatException de) {
			logger.log(Level.SEVERE,
					"error while trying to get the size of the file "
							+ this.doc.getId() + " " + de.getMessage());
			RepositoryException re = new RepositoryException(de.getMessage(),
					de.getCause());
			throw re;
		} catch (PropertyNotFoundException de) {
			logger.log(Level.SEVERE,
					"error while trying to get the size of the file "
							+ this.doc.getId() + " " + de.getMessage());
			RepositoryException re = new RepositoryException(de.getMessage(),
					de.getCause());
			throw re;
		}
	}

	public IPermissions getPermissions() {
		return new FnPermissions(doc.getPermissions());
	}

	public long getPropertyLongValue(String name) throws RepositoryException {

		try {
			return this.doc.getPropertyIntValue(name);
		} catch (PropertyNotFoundException e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyDoubleValue(name);
		} catch (PropertyNotFoundException e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		try {
			return this.doc.getPropertyDateValue(name);
		} catch (PropertyNotFoundException e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyBooleanValue(name);
		} catch (PropertyNotFoundException e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public IProperties getProperties() throws RepositoryException {
		try {
			return new FnProperties(this.doc.getProperties());
		} catch (PropertyNotFoundException e) {
			logger.log(Level.SEVERE,
					"error while trying to get the properties of the file "
							+ this.doc.getId() + " " + er.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

}
