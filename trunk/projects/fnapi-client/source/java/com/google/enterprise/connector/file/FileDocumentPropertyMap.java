package com.google.enterprise.connector.file;

import java.util.HashSet;
import java.util.Iterator;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

public class FileDocumentPropertyMap implements PropertyMap {

	private String docId;

	private IObjectStore objectStore;

	private IDocument document = null;

	private String isPublic = "false";

	private String displayUrl;

	private static HashSet set = null;
	static {
		set = new HashSet();
		set.add("ClassificationStatus");
		set.add("ContentRetentionDate");
		set.add("ContentSize");
		set.add("CurrentState");
		set.add("CurrentVersion");
		set.add("DateContentLastAccessed");
		set.add("DateCreated");
		set.add("DateLastModified");
		set.add("DocumentTitle");
		set.add("Id");
		set.add("IsCurrentVersion");
		set.add("IsFrozenVersion");
		set.add("IsReserved");
		set.add("LastModifier");
		set.add("LockTimeout");
		set.add("LockToken");
		set.add("MajorVersionNumber");
		set.add("MimeType");
		set.add("MinorVersionNumber");
		set.add("Name");
		set.add("ObjectStore");
		set.add("Owner");
		set.add("StorageLocation");
		set.add("VersionStatus");

	}
	private static HashSet sysmeta = null;
	static {
		sysmeta = new HashSet();
		sysmeta.add("AccessMask");
		sysmeta.add("ActiveMarkings");
		sysmeta.add("Annotations");	
		sysmeta.add("AuditedEvents");	
		sysmeta.add("ClassDescription");	
		sysmeta.add("ContentElements");	
		sysmeta.add("ContentElementsPresent");	
		sysmeta.add("CreatePending");	
		sysmeta.add("DateContentLastAccessed");	
		sysmeta.add("DeletePending");	
		sysmeta.add("DestinationDocuments");	
		sysmeta.add("DocumentLifecyclePolicy");	
		sysmeta.add("EntryTemplateId");	
		sysmeta.add("EntryTemplateLaunchedWorkflowNumber");	
		sysmeta.add("EntryTemplateObjectStoreName");	
		sysmeta.add("FoldersFiledIn");	
		sysmeta.add("IsInExceptionState");	
		sysmeta.add("IsVersioningEnabled");	
		sysmeta.add("LockOwner");	
		sysmeta.add("ObjectType");	
		sysmeta.add("OIID");	
		sysmeta.add("OwnerDocument");	
		sysmeta.add("PendingOperation");
		sysmeta.add("Properties");	
		sysmeta.add("PublicationInfo");	
		sysmeta.add("ReleasedVersion");	
		sysmeta.add("Reservation");	
		sysmeta.add("ReservationType");	
		sysmeta.add("SecurityParent");	
		sysmeta.add("SecurityPolicy");	
		sysmeta.add("SourceDocument");	
		sysmeta.add("StoragePolicy");
		sysmeta.add("UpdatePending");	
		sysmeta.add("VersionSeries");	
		sysmeta.add("WorkflowSubscriptions");
	}


	public FileDocumentPropertyMap(String docId, IObjectStore objectStore, String isPublic, String displayUrl) {
		this.docId = docId;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
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
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.STRING, this.displayUrl + docId));
		} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.BOOLEAN, this.isPublic));
		} else if (SpiConstants.PROPNAME_LASTMODIFY.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.DATE, "DateLastModified", document));
		} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.STRING, "MimeType", document));
		} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
			return null;
		} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
			return new FileDocumentProperty(name, new FileDocumentValue(
					ValueType.STRING, docId));
		}

		return new FileDocumentProperty(name, new FileDocumentValue(
				ValueType.STRING, name, document));
	}


	public Iterator getProperties() throws RepositoryException {
		fetch();
		HashSet properties = new HashSet();
		IProperties documentProperties = this.document.getProperties();
		IProperty property;
		for (int i = 0; i < documentProperties.size(); i++) {
			property = (IProperty) documentProperties.get(i);
			if (!sysmeta.contains(property.getName()) || set.contains(property.getName())) {
				properties.add(new FileDocumentProperty(property.getName(),
						new FileDocumentValue(property.getValueType(), property
								.getName(), document)));
			}
		}

		return properties.iterator();
	}

}
