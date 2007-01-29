package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;

import com.filenet.wcm.api.BaseRuntimeException;
import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.PropertyNotFoundException;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnDocument implements IDocument {
	Document doc;

	public FnDocument(Document doc) {

		this.doc = doc;

	}

	public InputStream getContent() {
		try {
			return doc.getContent();
		} catch (BaseRuntimeException e) {

			System.out.println("how often? getContent BaseRuntimeException");
			return doc.getContent();
		} catch (Error er) {
			System.out.println("to check " + er.getMessage());
			return doc.getContent();
		}

	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyStringValue(name);

		} catch (PropertyNotFoundException de) {
			System.out.println(" PropertyNotFoundException docnmae plantage? "
					+ doc.getName() + " " + doc.getSession().toString());
			if (name.equals(Property.MIME_TYPE)) {
				System.out.println("how often? mimetype propertynotfound");
				return "application/octet-stream";
			} else if (name.equals(Property.PERMISSIONS)) {
				System.out.println("how often? permissions propertynotfound");
				return "security";
			} else {
				RepositoryException re = new RepositoryException(de);
				throw re;
			}
			// }catch (BaseRuntimeException e){
			// if (name.equals(Property.MIME_TYPE)) {
			// System.out.println("how often? mimetype BaseRuntimeException");
			// return "application/octet-stream";
			// } else if (name.equals(Property.PERMISSIONS)) {
			// System.out.println("how often? permissions
			// BaseRuntimeException");
			// return "security";
			// } else {
			// RepositoryException re = new RepositoryException(e);
			// throw re;
			// }
		} catch (Exception de) {
			System.out.println(" FnDocument Exception" + doc.getName() + " "
					+ doc.getSession().toString() + " " + name);
			RepositoryException re = new RepositoryException(de);
			throw re;

		} catch (Error er) {
			System.out.println("to check " + er.getMessage());
			return "";
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
			RepositoryException re = new RepositoryException(de.getMessage(),
					de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		} catch (PropertyNotFoundException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),
					de.getCause());
			re.setStackTrace(de.getStackTrace());
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

			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyDoubleValue(name);
		} catch (PropertyNotFoundException e) {

			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		try {
			return this.doc.getPropertyDateValue(name);
		} catch (PropertyNotFoundException e) {

			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyBooleanValue(name);
		} catch (PropertyNotFoundException e) {

			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public IProperties getProperties() throws RepositoryException {
		try {
			return new FnProperties(this.doc.getProperties());
		} catch (PropertyNotFoundException e) {
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

}
