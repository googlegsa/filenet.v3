package com.google.enterprise.connector.file.filejavawrap;

import java.util.Date;

import com.filenet.wcm.api.PropertyNotFoundException;
import com.filenet.wcm.api.VersionSeries;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnVersionSeries implements IVersionSeries {

	private VersionSeries versionSeries;

	public FnVersionSeries(VersionSeries versionSeries) {
		this.versionSeries = versionSeries;
	}

	public String getId() {
		return versionSeries.getId();

	}

	public IProperties getProperties() throws RepositoryException {
		try {
			return new FnProperties(this.versionSeries.getProperties());
		} catch (PropertyNotFoundException e) {

			throw new RepositoryException(e);
		}
	}

	public IDocument getCurrentVersion() {
		return new FnDocument(this.versionSeries.getCurrentVersion());
	}

	public IDocument getReleasedVersion() {
		return new FnDocument(this.versionSeries.getReleasedVersion());
	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		try {
			return this.versionSeries.getPropertyStringValue(name);
		} catch (PropertyNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

	public long getPropertyLongValue(String name) throws RepositoryException {
		return 0;
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryException {
		return 0;
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		return null;
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		return false;
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException {
		return null;
	}

}
