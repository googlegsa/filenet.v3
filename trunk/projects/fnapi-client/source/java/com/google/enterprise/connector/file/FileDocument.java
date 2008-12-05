package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
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

public class FileDocument implements Document {

	private IObjectStore objectStore;

	private IDocument document = null;

	private boolean isPublic = false;

	private String displayUrl;
    
	private String docId;
	private String versionId;
	private String timeStamp;
	
	private String vsDocId;

	private HashSet included_meta = null;

	private HashSet excluded_meta = null;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FileDocument.class.getName());
	}
	
	private SpiConstants.ActionType action;

	public FileDocument(String docId, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta, SpiConstants.ActionType action) {
		this.docId = docId;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.action=action;
	}
	
	public FileDocument(String docId, String timeStamp, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta, SpiConstants.ActionType action) {
		this.docId = docId;
		this.objectStore = objectStore;
		this.timeStamp = timeStamp;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.action=action;
	}
	
	public FileDocument(String docId, String commonVersionId, String timeStamp, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta, SpiConstants.ActionType action) {
		this.docId = docId;
		this.versionId = commonVersionId;
		this.timeStamp = timeStamp;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.action=action;
	}

	private void fetch(){
		if (document != null) {
			return;
		}
		document = (IDocument) objectStore.getObject(IBaseObject.TYPE_DOCUMENT,
				docId);

		logger.fine("fetch doc " + docId);
        try{
        	this.vsDocId = document.getVersionSeries().getId();
    		logger.fine("fetch doc VSID: " + this.vsDocId);
        }catch(RepositoryException e){
        	logger.severe("Problem on getting the VSID "+e.getStackTrace());
        }
	}

	private Calendar getDate(String type, IDocument document)
			throws IllegalArgumentException, RepositoryException {

		Date date = this.document.getPropertyDateValue(type);

		logger.info("date ADD: "+date.toString());
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		logger.info("calendar c after setTime : "+c);
		return c;
	}

	///public Property findProperty(String name) throws RepositoryException {
	public Property findProperty(String name) throws RepositoryDocumentException{
		HashSet set = new HashSet();
		logger.info("in findProperty");
		if (SpiConstants.ActionType.ADD.equals(action)) {
			fetch();
			if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
					///try {
						if(document.getContent()!= null){
							set.add(new BinaryValue(document.getContent()));
						}else{
							logger.fine("getContent returns null");
							set.add(null);
						}
						
					///} catch (RepositoryDocumentException e) {
						///throw new RepositoryDocumentException();
					///} catch (RepositoryException e) {
						///logger.warning("RepositoryException thrown : "+ e+" on getting property : "+name);
						///logger.warning("RepositoryException thrown message : "+ e.getMessage());
						///set.add(null);
					///}
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
				logger.info("getting property "+name);
				set.add(new StringValue(this.displayUrl + vsDocId));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
				logger.info("getting property "+name);
				set.add(BooleanValue.makeBooleanValue(this.isPublic ? true : false));
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				logger.info("getting property "+name);
			
				Calendar tmpCal = Calendar.getInstance();
				logger.info("tmpCal instance : " + tmpCal);
				try {
					Date tmpDt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeStamp);
					
					long timeDateMod=tmpDt.getTime();
					logger.fine("last modified date before setTime "+tmpDt.getSeconds());
					tmpDt.setTime(timeDateMod+1000);
					logger.fine("last modified date after setTime "+tmpDt.getSeconds());
					tmpCal.setTime(tmpDt);

					logger.fine("Right last modified date : "+tmpDt.toString());
				} catch (ParseException e) {
					logger.fine("Error: wrong last modified date");
					tmpCal.setTime(new Date());	
					
				}
				logger.info("tmpCal after setTime : " + tmpCal);
				FileDateValue tmpDtVal = new FileDateValue(tmpCal);
				logger.fine("Last modify date value : " + tmpDtVal.toString());
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
			
				
			} else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
				logger.info("getting property "+name);
				try {
					set.add(new StringValue(document.getPropertyStringValue("MimeType")));
					logger.fine("Property "+name+" : "+document.getPropertyStringValue("MimeType"));
				} catch (RepositoryException e) {
					logger.warning("RepositoryException thrown : "+ e+" on getting property : "+name);
					logger.warning("RepositoryException thrown message : "+ e.getMessage());
					set.add(null);
				}
				
				return new FileDocumentProperty(name, set);
			} else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
				logger.info("getting property "+name);
				return null;
			} else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				logger.info("getting property "+name);
				set.add(new StringValue(vsDocId));
				logger.fine("Property "+name+" : "+vsDocId);
				return new FileDocumentProperty(name, set);
			}else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				logger.info("getting property "+name);
				set.add(new StringValue(action.toString()));
				logger.fine("Property "+name+" : "+action.toString());
				return new FileDocumentProperty(name, set);
			}

			try{
				String prop = null;
				String[] names = { name };
				IProperties props = document.getProperties(names);
				IProperty property = null;
	
				int ps = props.size();
				for (int i = 0; i < ps; i++) {
					property = props.get(i);
					prop = property.getValueType();
				}
	
				if (prop.equals("Binary")) {
					logger.info("getting property "+name);
					set.add(new BinaryValue(document.getPropertyBinaryValue(name)));
				} else if (prop.equals("Boolean")) {
					logger.info("getting property "+name);
					set.add(BooleanValue.makeBooleanValue(document
							.getPropertyBooleanValue(name)));
					logger.fine("Property "+name+" : "+BooleanValue.makeBooleanValue(document
							.getPropertyBooleanValue(name)));
				} else if (prop.equals("Date")) {
					set.add(new DateValue(getDate(name, document)));
					logger.fine("Property "+name+" : "+getDate(name, document));
				} else if (prop.equals("Double")) {
					set.add(new DoubleValue(document.getPropertyDoubleValue(name)));
				} else if (prop.equals("String")) {
					set.add(new StringValue(document.getPropertyStringValue(name)));
					logger.fine("Property "+name+" : "+document.getPropertyStringValue(name));
				} else if (prop.equals("Long")) {
					logger.info("getting property "+name);
					set.add(new LongValue(document.getPropertyLongValue(name)));
				}
			
			}catch(RepositoryException re){
				logger.warning("RepositoryException thrown : "+ re+" on getting property : "+name);
				logger.warning("RepositoryException thrown message : "+ re.getMessage());
				set.add(null);
			}
		}else{
			if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
				Calendar tmpCal = Calendar.getInstance();
				logger.info("tmpCal del instance : " + tmpCal);
				
				
			
				try {
					Date tmpDt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeStamp);
					
					///
					long timeDateMod=tmpDt.getTime();
					logger.fine("last modified date before setTime "+tmpDt.getSeconds());
					tmpDt.setTime(timeDateMod+1000);
					logger.fine("last modified date after setTime "+tmpDt.getSeconds());
					tmpCal.setTime(tmpDt);
		
					
					logger.fine("Right last modified date : "+tmpDt.toString());
				} catch (ParseException e) {
					
					logger.fine("Error: wrong last modified date");
					tmpCal.setTime(new Date());	
				}
				logger.info("tmpCal after setTime : " + tmpCal);
				FileDateValue tmpDtVal = new FileDateValue(tmpCal);
				logger.fine("Last modify date value : " + tmpDtVal.toString());
				set.add(tmpDtVal);
				return new FileDocumentProperty(name, set);
				
				
			}else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
				set.add(new StringValue(action.toString()));
				return new FileDocumentProperty(name, set);
			}else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
				set.add(new StringValue(versionId));
				logger.fine("versionId : " + versionId);
				return new FileDocumentProperty(name, set);
			}
		}		
		
		return new FileDocumentProperty( name, set);
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
