package com.google.enterprise.connector.filenet4;

import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.DoubleValue;
import com.google.enterprise.connector.spiimpl.LongValue;
import com.google.enterprise.connector.spiimpl.StringValue;
import com.filenet.api.constants.ClassNames;

public class FileDocument implements Document {

	private String docId;
	private IObjectStore objectStore;
	private IDocument document = null;
	private boolean isPublic = false;
	private String displayUrl;
	private String versionId;
	private Date timeStamp;
	private String vsDocId;
	private HashSet included_meta = null;
	private HashSet excluded_meta = null;
	private static Logger logger = null;
	{
		logger = Logger.getLogger(FileDocument.class.getName());
	}
	private SpiConstants.ActionType action;

	public FileDocument(String docId, Date timeStamp,
			IObjectStore objectStore, boolean isPublic, String displayUrl, 
			HashSet included_meta, HashSet excluded_meta, SpiConstants.ActionType action) {
		this.docId = docId;
		this.timeStamp = timeStamp;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.action = action;
	}

	public FileDocument(String docId, String commonVersionId, Date timeStamp,
			IObjectStore objectStore, boolean isPublic, String displayUrl,
			HashSet included_meta, HashSet excluded_meta,
			SpiConstants.ActionType action) {
		this.docId = docId;
		this.versionId = commonVersionId;
		this.timeStamp = timeStamp;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.action = action;
	}

	private void fetch() throws RepositoryDocumentException {
		if (document != null) {
			return;
		}
		document = (IDocument) objectStore.getObject(ClassNames.DOCUMENT, docId);
		document.fetch(included_meta);
		logger.log(Level.FINE, "Fetch document for docId " + docId);
		this.vsDocId = document.getVersionSeries().getId(action);
		logger.log(Level.FINE, "VersionSeriesID for document is : " + this.vsDocId);
	}

	private Calendar getDate(String type) throws IllegalArgumentException,
	RepositoryDocumentException {
		Date date = this.document.getPropertyDateValue(type);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public Property findProperty(String name) throws RepositoryDocumentException {
		HashSet set = new HashSet();

		if (SpiConstants.ActionType.ADD.equals(action)) {
			fetch();
			if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new BinaryValue(document.getContent()));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new StringValue(this.displayUrl + vsDocId));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(BooleanValue.makeBooleanValue(this.isPublic ? true : false));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				DateValue tmpDtVal=new DateValue(getDate("DateLastModified"));
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
				set.add(new StringValue(document.getPropertyStringValue("MimeType")));
				logger.log(Level.FINEST, "Getting property: "+name);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
				return null;
			} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new StringValue(vsDocId));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				set.add(new StringValue(action.toString()));
				logger.fine("Getting Property " + name + " : " + action.toString());
				return new FileDocumentProperty(name, set);
			}

			String type = document.getPropertyType(name);
			if (type == null) // unknows property name
				return null;

			if (type.equalsIgnoreCase("Binary")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new BinaryValue(document.getPropertyBinaryValue(name)));
			} else if (type.equalsIgnoreCase("Boolean")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(BooleanValue.makeBooleanValue(document.getPropertyBooleanValue(name)));
			} else if (type.equalsIgnoreCase("Date")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new DateValue(getDate(name)));
			} else if (type.equalsIgnoreCase("Double")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new DoubleValue(document.getPropertyDoubleValue(name)));
			} else if (type.equalsIgnoreCase("String")) {
				logger.info("String Type:");
				set.add(new StringValue(document.getPropertyStringValue(name)));
			} else if (type.equalsIgnoreCase("guid")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new StringValue(document.getPropertyGuidValue(name)));
			} else if (type.equalsIgnoreCase("Long")) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new LongValue(document.getPropertyLongValue(name)));
			}
		} else {
			if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				Calendar tmpCal = Calendar.getInstance();
				
				long timeDateMod = timeStamp.getTime();
				timeStamp.setTime(timeDateMod + 1000);		
				tmpCal.setTime(timeStamp);
				
				DateValue tmpDtVal=new DateValue(tmpCal);
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new StringValue(action.toString()));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				logger.log(Level.FINEST, "Getting property: "+name);
				set.add(new StringValue(versionId));
//				logger.fine("versionId : " + versionId);
				return new FileDocumentProperty(name, set);
			}
		}
		return new FileDocumentProperty(name, set);
	}

	public Set getPropertyNames() throws RepositoryDocumentException {
		fetch();
		HashSet properties = new HashSet();
		Set documentProperties = this.document.getPropertyName();
		String property;
		for (Iterator iter = documentProperties.iterator(); iter.hasNext();) {
			property = (String) iter.next();
			if(property != null){
				if(included_meta.size() != 0){
					//includeMeta - exludeMeta
					logger.log(Level.FINE, "Metadata set will be (includeMeta - exludeMeta)");
					if ((!excluded_meta.contains(property) 
							&& included_meta.contains(property))){
						properties.add(property);
					}
				}else {
					//superSet - exludeMeta
					logger.log(Level.FINE, "Metadata set will be (superSet - exludeMeta)");
					if ((!excluded_meta.contains(property) 
							|| included_meta.contains(property))){
						properties.add(property);
					}
				}
			}
		}
		
		return properties;
	}
}
