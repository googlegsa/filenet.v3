package com.google.enterprise.connector.filenet3.filejavawrap;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.PropertyNotFoundException;
import com.filenet.wcm.api.VersionSeries;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnVersionSeries implements IVersionSeries {

	private VersionSeries versionSeries;
	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public FnVersionSeries(VersionSeries refVersionSeries) {
		this.versionSeries = refVersionSeries;
	}

	public String getId() {
		return versionSeries.getId();

	}

	public IProperties getProperties() throws RepositoryDocumentException {
		try {
			return new FnProperties(this.versionSeries.getProperties());
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "versionSeries is null");
			throw new RepositoryDocumentException(e);
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "Error while trying to get the properties of the file "
					+ e.getLocalizedMessage());
			throw new RepositoryDocumentException(e);
		}
	}

	public IDocument getCurrentVersion() {
		return new FnDocument(this.versionSeries.getCurrentVersion());
	}

	public IDocument getReleasedVersion() {
		return new FnDocument(this.versionSeries.getReleasedVersion());
	}

	public String getPropertyStringValue(String name)
			throws RepositoryDocumentException {
		try {
			return this.versionSeries.getPropertyStringValue(name);
		}  catch (NullPointerException e) {
			logger.log(Level.WARNING, "versionSeries is null");
			throw new RepositoryDocumentException(e);
		}catch (PropertyNotFoundException e) {
			logger.log(Level.WARNING, "Error while trying to get the properties of the file "
					+ e.getLocalizedMessage());
			throw new RepositoryDocumentException(e);
		}
	}

	public long getPropertyLongValue(String name) throws RepositoryDocumentException {
		return 0;
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryDocumentException {
		return 0;
	}

	public Date getPropertyDateValue(String name) throws RepositoryDocumentException {
		return null;
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryDocumentException {
		return false;
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryDocumentException {
		return null;
	}


}
