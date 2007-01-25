package com.google.enterprise.connector.file;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

public class FileDocumentValue implements Value {

	private IDocument fileDocument = null;

	private String propname_content;

	private ValueType type;

	private String stringValue;

	public FileDocumentValue(ValueType t, String propname_content,
			IDocument document) {
		type = t;
		this.propname_content = propname_content;
		fileDocument = document;
		stringValue = null;
	}

	public FileDocumentValue(ValueType t, String v) {
		type = t;
		this.propname_content = null;
		fileDocument = null;
		stringValue = v;
	}

	public String getString() throws IllegalArgumentException,
			RepositoryException {
		if (stringValue != null) {
			return stringValue;
		} else {
			return this.fileDocument
					.getPropertyStringValue(this.propname_content);
		}

	}

	public InputStream getStream() throws IllegalArgumentException,
			IllegalStateException, RepositoryException {

		return this.fileDocument.getContent();
	}

	public long getLong() throws IllegalArgumentException, RepositoryException {

		return this.fileDocument.getPropertyLongValue(this.propname_content);
	}

	public double getDouble() throws IllegalArgumentException,
			RepositoryException {

		return this.fileDocument.getPropertyDoubleValue(this.propname_content);
	}

	public Calendar getDate() throws IllegalArgumentException,
			RepositoryException {
		Date date = this.fileDocument
				.getPropertyDateValue(this.propname_content);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;

	}

	public boolean getBoolean() throws IllegalArgumentException,
			RepositoryException {
		if (stringValue != null) {
			return stringValue.equals("true");
		} else {
			return this.fileDocument
					.getPropertyBooleanValue(this.propname_content);
		}
	}

	public ValueType getType() throws RepositoryException {
		return this.type;
	}

}
