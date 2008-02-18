package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.BaseRuntimeException;
import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.EntireNetwork;
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
import com.google.enterprise.connector.spi.RepositoryException;

public class FnDocument implements IDocument {

	Document doc;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public FnDocument(Document doc) {
		this.doc = doc;
	}

	public InputStream getContent() {
		try {
			return doc.getContent();
		} catch (BaseRuntimeException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,
					"error while trying to get the content of file "
							+ this.doc.getId() + " " + e.getMessage());
			return doc.getContent();
		} catch (Error er) {
			er.printStackTrace();
			logger.log(Level.SEVERE,
					"error while trying to get the content of file "
							+ this.doc.getId() + " " + er.getMessage());
			return doc.getContent();
		}
	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		String[] valuesName = { name };
		try {
			Properties props = doc.getProperties(valuesName);
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
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
			} else {
				logger.log(Level.SEVERE,
						"error while trying to get the property " + name
								+ " of the file " + this.doc.getId() + " "
								+ e1.getMessage());
				RepositoryException re = new RepositoryException(e1);
				throw re;
			}

		} catch (ClassCastException e) {
			logger
					.info("ClassCastException found but still continuing for property "
							+ name);
		} catch (Exception e) {
			logger
					.info("ClassCastException found but still continuing for property "
							+ name);
		}
		return "";

	}

	public IPermissions getPermissions(Session session) {
		EntireNetwork en = ObjectFactory.getEntireNetwork(session);
		try {
			logger.info("getPermissions11:" + this.doc.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Permissions perms = this.doc.getPermissions();
		return new FnPermissions(en, perms);
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

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException {
		try {
			return this.doc.getPropertyBinaryValue(name);
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
							+ this.doc.getId() + " " + e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public IVersionSeries getVersionSeries() throws RepositoryException {
		try {
			return new FnVersionSeries(doc.getVersionSeries());
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"error while trying to get the properties of the file "
							+ this.doc.getId() + " " + e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}
	}

	public String getId() throws RepositoryException {
		try {
			return doc.getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"error while trying to get the properties of the file "
							+ this.doc.getId() + " " + e.getMessage());
			RepositoryException re = new RepositoryException(e);
			throw re;
		}

	}

	public String getPropertyValue(String name)
			throws PropertyNotFoundException {

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

	public IProperties getProperties(String[] names) throws RepositoryException {

		try {
			return new FnProperties(this.doc.getProperties(names));
		} catch (PropertyNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

}
