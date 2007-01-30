package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.spi.RepositoryException;

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

	public double getContentSize() throws RepositoryException {
		return this.document.getContent().length();
	}

	public IPermissions getPermissions() {
		MockRepositoryPropertyList mrPL = this.document.getProplist();
		// "acl":{type:string, value:[fred,mark,bill]}
		// "google:ispublic":"false"
		String[] users = mrPL.getProperty("acl").getValues();
		String pub = mrPL.lookupStringValue("google:ispublic");
		return new MockFnPermissions(users, pub);
	}

	/**
	 * TOTEST carrefully
	 */
	public String getPropertyStringValue(String name)
			throws RepositoryException {
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.STRING
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			return curProp.getValue();
		}
		throw new RepositoryException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != String whereas MockFnDocument.getPropertyStringValue("
						+ name + ") was called");
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
		MockRepositoryProperty curProp = this.document.getProplist()
				.getProperty(name);
		if (curProp.getType() == MockRepositoryProperty.PropertyType.DATE
				|| curProp.getType() == MockRepositoryProperty.PropertyType.UNDEFINED) {
			DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
			try {
				return df.parse(curProp.getValue());
			} catch (ParseException e) {
				throw new RepositoryException(e);
			}
		}
		throw new RepositoryException(
				"MockRepositoryDocument.getProplist().getProperty("
						+ name
						+ ").getType() != Int or Long or double.. whereas MockFnDocument.getPropertyLongValue("
						+ name + ") was called");
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryException {
		// TODO Wait to see whether Connector-Manager only gets strings.
		return false;
	}

	public IProperties getProperties() throws RepositoryException {
		return new MockFnProperties(this.document.getProplist());
	}

}
