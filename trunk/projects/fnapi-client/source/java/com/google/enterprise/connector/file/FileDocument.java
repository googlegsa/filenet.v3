package com.google.enterprise.connector.file;

import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.DoubleValue;
import com.google.enterprise.connector.spiimpl.LongValue;
import com.google.enterprise.connector.spiimpl.StringValue;

public class FileDocument implements Document {

	private String docId;

	private IObjectStore objectStore;

	private IDocument document = null;

	private boolean isPublic = false;

	private String displayUrl;

	private HashSet included_meta = null;

	private HashSet excluded_meta = null;

	private String vsDocId;

	public FileDocument(String docId, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta) {
		this.docId = docId;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
	}

	private void fetch() {
		if (document != null) {
			return;
		}
		document = (IDocument) objectStore.getObject(IBaseObject.TYPE_DOCUMENT,
				docId);

		this.vsDocId = ((IVersionSeries) objectStore.getObject(
				IBaseObject.TYPE_VERSIONSERIES, docId)).getId();
	}

	private Calendar getDate(String type, IDocument document)
			throws IllegalArgumentException, RepositoryException {

		Date date = this.document.getPropertyDateValue(type);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public Property findProperty(String name) throws RepositoryException {
		fetch();
		HashSet set = new HashSet();
		if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
			set.add(new BinaryValue(document.getContent()));
			return new FileDocumentProperty(name, set);
		} else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
			set.add(new StringValue(this.displayUrl + vsDocId));
			return new FileDocumentProperty(name, set);
		} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
			set
					.add(BooleanValue.makeBooleanValue(this.isPublic ? true
							: false));
			return new FileDocumentProperty(name, set);
		} else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
			set.add(new DateValue(getDate("DateLastModified", document)));
			return new FileDocumentProperty(name, set);
		} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
			set
					.add(new StringValue(document
							.getPropertyStringValue("MimeType")));
			return new FileDocumentProperty(name, set);
		} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
			return null;
		} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
			set.add(new StringValue(vsDocId));
			return new FileDocumentProperty(name, set);
		}

		String prop = null;
		String[] names = { name };
		IProperties props = document.getProperties(names);
		IProperty property = null;

		int ps = props.size();
		for (int i = 0; i < ps; i++) {
			property = props.get(i);
			prop = property.getValueType();
		}

		if (prop == "Binary") {
			set.add(new BinaryValue(document.getPropertyBinaryValue(name)));
		} else if (prop == "Boolean") {
			set.add(BooleanValue.makeBooleanValue(document
					.getPropertyBooleanValue(name)));
		} else if (prop == "Date") {
			set.add(new DateValue(getDate(name, document)));
		} else if (prop == "Double") {
			set.add(new DoubleValue(document.getPropertyDoubleValue(name)));
		} else if (prop == "String") {
			set.add(new StringValue(document.getPropertyStringValue(name)));
		} else if (prop == "Long") {
			set.add(new LongValue(document.getPropertyLongValue(name)));
		}
		FileDocumentProperty fileDocumentProperty = new FileDocumentProperty(
				name, set);
		return fileDocumentProperty;
	}

	public Set getPropertyNames() throws RepositoryException {
		fetch();
		HashSet properties = new HashSet();
		IProperties documentProperties = this.document.getProperties();
		IProperty property;
		for (int i = 0; i < documentProperties.size(); i++) {
			property = (IProperty) documentProperties.get(i);
			if ((!excluded_meta.contains(property.getName()) || included_meta
					.contains(property.getName()))
					&& (property.getValue() != null)) {
				properties.add(property.getName());
			}
		}
		return properties;
	}
}
