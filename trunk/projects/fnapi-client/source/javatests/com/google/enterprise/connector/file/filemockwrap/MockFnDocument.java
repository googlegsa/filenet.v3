package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class MockFnDocument implements IDocument {

	MockRepositoryDocument document;

	protected MockFnDocument(MockRepositoryDocument doc) {
		this.document = doc;
	}

	public InputStream getContent() throws RepositoryException {
		try {
			return this.document.getContentStream();
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

	public IPermissions getPermissions() {
		MockRepositoryPropertyList mrPL = this.document.getProplist();
		String[] users = mrPL.getProperty("acl").getValues();
		return new MockFnPermissions(users);
	}

	public String getPropertyStringValue(String name)
			throws RepositoryException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		return curProp.getValue();
	}

	public long getPropertyLongValue(String name) throws RepositoryException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.INTEGER
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			return Integer.parseInt(curProp.getValue());
		}
		throw new RepositoryException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != Int or Long or double.. whereas MockFnDocument.getPropertyLongValue("
						+ name + ") was called");
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.INTEGER
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			return Integer.parseInt(curProp.getValue());
		}
		throw new RepositoryException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != Int or Long or double.. whereas MockFnDocument.getPropertyDoubleValue("
						+ name + ") was called");
	}

	public Date getPropertyDateValue(String name) throws RepositoryException {
		if (name.equals("DateLastModified")
				|| name.equals(SpiConstants.PROPNAME_LASTMODIFIED)) {
			MockRepositoryDateTime curProp = this.document.getTimeStamp();
			return new Date(curProp.getTicks());
		}
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		return new Date(Long.parseLong(curProp.getValue()));

	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		// TODO Wait to see whether Connector-Manager only gets strings.
		return false;
	}

	public IProperties getProperties() throws RepositoryException {
		return new MockFnProperties(this.document.getProplist());
	}

	public IVersionSeries getVersionSeries() {
		return null;
	}

	public String getId() {
		return document.getDocID();
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IProperties getProperties(String[] names) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IPermissions getPermissions(Session s) {
		// TODO Auto-generated method stub
		return null;
	}

}
