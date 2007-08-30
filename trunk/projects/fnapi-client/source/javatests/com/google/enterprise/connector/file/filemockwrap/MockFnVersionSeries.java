package com.google.enterprise.connector.file.filemockwrap;

import java.util.Date;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnVersionSeries implements IVersionSeries {

	MockRepositoryDocument versionSeries;

	public MockFnVersionSeries(MockRepositoryDocument doc) {
		// TODO Auto-generated constructor stub
		this.versionSeries = doc;
	}

	public IProperties getProperties() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IDocument getCurrentVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public IDocument getReleasedVersion() {
		return new MockFnDocument(this.versionSeries);
	}

	public String getId() {
		return versionSeries.getDocID();
	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public long getPropertyLongValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
