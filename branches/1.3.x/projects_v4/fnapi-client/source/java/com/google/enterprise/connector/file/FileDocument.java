package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.DoubleValue;
import com.google.enterprise.connector.spiimpl.LongValue;
import com.google.enterprise.connector.spiimpl.StringValue;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.property.Properties;

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

	private void fetch()  throws RepositoryDocumentException{
		if (document != null) {
			return;
		}
		document = (IDocument) objectStore
				.getObject(ClassNames.DOCUMENT, docId);

		document.fetch(included_meta);

		logger.fine("fetch doc " + docId);

		this.vsDocId = document.getVersionSeries().getId(action);
		logger.fine("fetch doc VSID: " + this.vsDocId);
	}

	private Calendar getDate(String type) throws IllegalArgumentException, RepositoryDocumentException {
		Date date = this.document.getPropertyDateValue(type);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public Property findProperty(String name) throws RepositoryDocumentException{
		HashSet set = new HashSet();

		if (SpiConstants.ActionType.ADD.equals(action)) {
			fetch();
			if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
				try{
					set.add(new BinaryValue(document.getContent()));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
				set.add(new StringValue(this.displayUrl + vsDocId));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
				set.add(BooleanValue.makeBooleanValue(this.isPublic ? true
						: false));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				DateValue tmpDtVal=new DateValue(getDate("DateLastModified"));
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
				try{
					set.add(new StringValue(document.getPropertyStringValue("MimeType")));
					logger.fine("Property " + name + " : "
							+ document.getPropertyStringValue("MimeType"));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}
				
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
				return null;
			} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				set.add(new StringValue(vsDocId));
				logger.fine("Property " + name + " : " + vsDocId);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				set.add(new StringValue(action.toString()));
				logger.fine("Property " + name + " : " + action.toString());
				return new FileDocumentProperty(name, set);
			}
			
			String type = null;
			try{
				type = document.getPropertyType(name);
			}catch(RepositoryDocumentException e1){
				type = null;
			}
			
			if (type == null) // unknows property name
				return null;

			if (type.equalsIgnoreCase("Binary")) {
				try{
					set.add(new BinaryValue(document.getPropertyBinaryValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			} else if (type.equalsIgnoreCase("Boolean")) {
				try{
					set.add(BooleanValue.makeBooleanValue(document
						.getPropertyBooleanValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			} else if (type.equalsIgnoreCase("Date")) {
				try{
					set.add(new DateValue(getDate(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			} else if (type.equalsIgnoreCase("Double")) {
				try{
					set.add(new DoubleValue(document.getPropertyDoubleValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			} else if (type.equalsIgnoreCase("String")) {
				try{
					set.add(new StringValue(document.getPropertyStringValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			} else if (type.equalsIgnoreCase("guid")) {
				try{
					set.add(new StringValue(document.getPropertyGuidValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}
			} else if (type.equalsIgnoreCase("Long")) {
				try{
					set.add(new LongValue(document.getPropertyLongValue(name)));
				}catch(RepositoryDocumentException e1){
					set.add(null);
				}	
			}
		} else {
			if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				Calendar tmpCal = Calendar.getInstance();
				
				long timeDateMod = timeStamp.getTime();
				timeStamp.setTime(timeDateMod + 1000);		
				tmpCal.setTime(timeStamp);
				
				DateValue tmpDtVal=new DateValue(tmpCal);
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				set.add(new StringValue(action.toString()));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				set.add(new StringValue(versionId));
				logger.fine("versionId : " + versionId);
				return new FileDocumentProperty(name, set);
			}
		}
		return new FileDocumentProperty(name, set);
	}

	public Set getPropertyNames() throws RepositoryDocumentException {
		return this.document.getPropertyName();
	}
}
