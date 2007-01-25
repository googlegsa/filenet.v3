package com.google.enterprise.connector.file;

import java.util.HashSet;
import java.util.Iterator;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

public class FileDocumentPropertyMap implements PropertyMap {

	private String docId;

	private IObjectStore objectStore;

	private IDocument document = null;

	public FileDocumentPropertyMap(String docId, IObjectStore objectStore) {
		this.docId = docId;
		this.objectStore = objectStore;
	}

	private void fetch() {
		if (document != null) {
			return;
		}
		document = objectStore.getObject(docId);
	}

	public Property getProperty(String name) throws RepositoryException {
		fetch();
		if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.BINARY, name, document));
		} else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
			return new SimpleProperty(name, new FileDocumentValue(
					ValueType.STRING, objectStore.getDisplayUrl() + docId));
		} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
			return new SimpleProperty(name, new FileDocumentValue(
					ValueType.BOOLEAN, "false"));
		} else if (SpiConstants.PROPNAME_LASTMODIFY.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.DATE, "DateLastModified", document));
		} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.STRING, "MimeType", document));
		} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
			return null;
		} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
			return new SimpleProperty(name, new FileDocumentValue(
					ValueType.STRING, docId));
		}

		return new FileDocumentProperty(name, new FileDocumentValue(
				ValueType.STRING, name, document));
	}

	public Iterator getProperties() throws RepositoryException {
		HashSet propNames = new HashSet();

		propNames.add(new FileDocumentProperty(
				SpiConstants.PROPNAME_LASTMODIFY, new FileDocumentValue(
						ValueType.DATE, "DateLastModified", document)));
		propNames
				.add(new FileDocumentProperty(SpiConstants.PROPNAME_DISPLAYURL,
						new FileDocumentValue(ValueType.STRING, objectStore
								.getDisplayUrl()
								+ docId)));
		propNames.add(new FileDocumentProperty(SpiConstants.PROPNAME_ISPUBLIC,
				new FileDocumentValue(ValueType.BOOLEAN, "false")));
		propNames.add(new FileDocumentProperty(SpiConstants.PROPNAME_MIMETYPE,
				new FileDocumentValue(ValueType.STRING, "MimeType", document)));
		propNames.add(new SimpleProperty(SpiConstants.PROPNAME_DOCID,
				new FileDocumentValue(ValueType.STRING, docId)));
		propNames.add(new FileDocumentProperty("DocumentTitle",
				new FileDocumentValue(ValueType.STRING, "DocumentTitle",
						document)));
		propNames.add(new FileDocumentProperty("Creator",
				new FileDocumentValue(ValueType.STRING, "Creator", document)));
		return propNames.iterator();
	}

}
