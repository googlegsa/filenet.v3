package com.google.enterprise.connector.filenet3.filemockwrap;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import com.filenet.wcm.api.PropertyNotFoundException;
import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IPermissions;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IVersionSeries;
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class MockFnDocument implements IDocument {

	MockRepositoryDocument document;

	protected MockFnDocument(MockRepositoryDocument doc) {
		this.document = doc;
	}

	public InputStream getContent() throws RepositoryDocumentException {
		try {
			return this.document.getContentStream();
		} catch (FileNotFoundException e) {
			throw new RepositoryDocumentException(e);
		}
	}

	public IPermissions getPermissions() {
		MockRepositoryPropertyList mrPL = this.document.getProplist();
		String[] users = mrPL.getProperty("acl").getValues();
		return new MockFnPermissions(users);
	}

	public String getPropertyStringValue(String name)
			throws RepositoryDocumentException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		return curProp.getValue();
	}

	public long getPropertyLongValue(String name) throws RepositoryDocumentException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.INTEGER
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			return Integer.parseInt(curProp.getValue());
		}
		throw new RepositoryDocumentException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != Int or Long or double.. whereas MockFnDocument.getPropertyLongValue("
						+ name + ") was called");
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryDocumentException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.INTEGER
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			return Integer.parseInt(curProp.getValue());
		}
		throw new RepositoryDocumentException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != Int or Long or double.. whereas MockFnDocument.getPropertyDoubleValue("
						+ name + ") was called");
	}

	public Date getPropertyDateValue(String name) throws RepositoryDocumentException {
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
			throws RepositoryDocumentException {
		// TODO Wait to see whether Connector-Manager only gets strings.
		return false;
	}

	public IProperties getProperties() throws RepositoryDocumentException {
		return new MockFnProperties(this.document.getProplist());
	}

	public IVersionSeries getVersionSeries() {
		return null;
	}

	public String getId() {
		return document.getDocID();
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryDocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public IProperties getProperties(String[] names) throws RepositoryDocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public IPermissions getPermissions(Session s) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPropertyValue(String name) throws PropertyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
