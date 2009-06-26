package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.BaseRuntimeException;
import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.EntireNetwork;
import com.filenet.wcm.api.InsufficientPermissionException;
import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Permissions;
import com.filenet.wcm.api.Properties;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.PropertyNotFoundException;
import com.filenet.wcm.api.Session;
import com.filenet.wcm.api.Value;
import com.filenet.wcm.api.Values;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

public class FnDocument implements IDocument {

	Document doc;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public FnDocument(Document refDoc) {
		this.doc = refDoc;
	}

	public InputStream getContent() throws RepositoryDocumentException{
		logger.log(Level.FINE, "Entering into getContent()");
		try {
			return doc.getContent();
		} catch (InsufficientPermissionException e) {
			logger.log(Level.WARNING, "User does not have sufficient permission to retrive the content of document for "
					+ this.doc.getId() + " " + e.getLocalizedMessage());
//			throw new RepositoryDocumentException();
			return null;
		} catch (BaseRuntimeException e) {
			logger.log(Level.WARNING, "Unable to retrieve the content of file for "
					+ this.doc.getId() + " " + e.getLocalizedMessage());
			return null;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unable to retrieve the content of file for "
					+ this.doc.getId() + " " + e.getLocalizedMessage());
			return null;
		}
	}

	public String getPropertyStringValue(String name)
	throws RepositoryDocumentException {
		String[] valuesName = { name };
		try {
			Properties props = doc.getProperties(valuesName);
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				//Value of 7 i.e. TYPE_OBJECT; object data type; also returned if the value is a Values  
				//collection (that is, if the property in question is a multi-valued property).
				if (prop.getType() == 7) {

					Values values = prop.getValuesValue();
					if (values != null && values.size() > 0) {
						Iterator it2 = values.iterator();
						String val = "";
						while (it2.hasNext()) {
							Value v = (Value) it2.next();
							val += v.getStringValue();
							val += ", ";
						}
						if (val.length() > 0) {
							val = (String) val.subSequence(0, val.length() - 2);
						}
						return val;
					}

				} else {
					return this.doc.getPropertyStringValue(name);
				}
			}
		} catch (PropertyNotFoundException e1) {
			if (name.equals(Property.MIME_TYPE)) {

				return "application/octet-stream";
			} else if (name.equals(Property.PERMISSIONS)) {

				return "security";
			} else if(name.equals(Property.TITLE)){
				return doc.getFilename();
			}else {
				String errorMsg = " Error while trying to get the property " + name
				+ " of the file " + this.doc.getId() + " "
				+ e1.getLocalizedMessage();
				logger.log(Level.SEVERE, errorMsg);
				RepositoryDocumentException re = new RepositoryDocumentException(errorMsg, e1);
				throw re;
			}

		} catch (ClassCastException e) {
			logger.log(Level.FINE, "ClassCastException found but still continuing for property " + name);
		} catch (Exception e) {
			logger.log(Level.FINE, "Exception found but still continuing for property " + name);
		}
		return "";

	}

	public IPermissions getPermissions(Session session) throws RepositoryDocumentException {
		try {
			EntireNetwork en = ObjectFactory.getEntireNetwork(session);
			Permissions perms = this.doc.getPermissions();
			return new FnPermissions(en, perms);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException("Document is null", e);
			throw re;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unable to retrieve permissions for document "+this.doc.getFilename());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public long getPropertyLongValue(String name) throws RepositoryDocumentException {

		try {
			return this.doc.getPropertyIntValue(name);
		} catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "Error while trying to get the property "
					+ name + " of the file " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public double getPropertyDoubleValue(String name)
	throws RepositoryDocumentException {
		try {
			return this.doc.getPropertyDoubleValue(name);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "error while trying to get the property "
					+ name + " of the file " + this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public Date getPropertyDateValue(String name) throws RepositoryDocumentException {
		try {
			return this.doc.getPropertyDateValue(name);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "error while trying to get the property "
					+ name + " of the file " + this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public boolean getPropertyBooleanValue(String name)
	throws RepositoryDocumentException {
		try {
			return this.doc.getPropertyBooleanValue(name);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "error while trying to get the property "
					+ name + " of the file " + this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public byte[] getPropertyBinaryValue(String name)
	throws RepositoryDocumentException {
		try {
			return this.doc.getPropertyBinaryValue(name);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "error while trying to get the property "
					+ name + " of the file " + this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public IProperties getProperties() throws RepositoryDocumentException {
		try {
			return new FnProperties(this.doc.getProperties());
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "error while trying to get the properties of the file " 
					+ this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public IVersionSeries getVersionSeries() throws RepositoryDocumentException {
		try {
			return new FnVersionSeries(doc.getVersionSeries());
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Error while trying to get the properties of the file "
					+ this.doc.getId() + " " + e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public String getId() throws RepositoryDocumentException {
		try {
			return doc.getId();
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Error while trying to get the properties of the file "
					+ this.doc.getId() + " " + e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

	public String getPropertyValue(String name) throws PropertyNotFoundException {

		String[] names = { name };
		Properties props = this.doc.getProperties(names);
		Iterator it = props.iterator();
		Property prop = null;
		while (it.hasNext()) {
			prop = (Property) it.next();
		}

		FnProperty fnprop = new FnProperty(prop);
		return fnprop.getValueType();
	}

	public IProperties getProperties(String[] names) throws RepositoryDocumentException {

		try {
			return new FnProperties(this.doc.getProperties(names));
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Document is null");
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "Error while trying to get the properties of the file " 
					+ this.doc.getFilename()+ " with docId " + this.doc.getId() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
	}

}
