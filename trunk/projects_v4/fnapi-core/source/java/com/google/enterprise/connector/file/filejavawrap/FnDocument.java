package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.collection.PermissionList;
import com.filenet.api.collection.PropertyDescriptionList;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.meta.PropertyDescription;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.Permission;
import com.filenet.api.util.Id;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
public class FnDocument implements IDocument {

	Document doc;

	Map metas;

	Map metaTypes;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public FnDocument(Document doc) {
		this.doc = doc;
	}

	public void fetch(Set includedMeta) throws RepositoryDocumentException{
		
		
		PropertyFilter pf = new PropertyFilter();
		try {
			pf.addIncludeProperty(new FilterElement(null, null, null,
							"Id ClassDescription ContentElements DateLastModified MimeType VersionSeries"));
			
			if (includedMeta != null){
				Iterator it = includedMeta.iterator();
				while (it.hasNext()){
					pf.addIncludeProperty(new FilterElement(null, null, null,(String)it.next()));
				}
			}
			
			pf.setMaxRecursion(1);
			doc.fetchProperties(pf);
			
			setMetaTypes();
			setMeta();
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
	}

	private void setMetaTypes() throws RepositoryDocumentException{
		metaTypes = new HashMap();
		String propertyName, propertyType;
		try{
			PropertyDescriptionList propDescList = doc.get_ClassDescription()
				.get_PropertyDescriptions();
			Iterator it8 = propDescList.iterator();
			while (it8.hasNext()) {
				PropertyDescription propDesc = (PropertyDescription) it8.next();
				propertyName = propDesc.get_Name().replaceAll(" ", "").toUpperCase();
				propertyType = propDesc.get_DataType().toString();
				metaTypes.put(propertyName, propertyType);
			}
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}

		
	}

	private void setMeta() throws RepositoryDocumentException{
		metas = new HashMap();
		Properties props;
		String propertyName;
		Object value;
		try{
			props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				propertyName = prop.getPropertyName();
				value = prop.getObjectValue();
				if (value != null)
					metas.put(propertyName, value);
			}
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}	
	}

	public Set getPropertyName() {
		return metas.keySet();
	}

	public String getPropertyType(String name) throws RepositoryDocumentException {
		return (String) metaTypes.get(name.toUpperCase());
	}

	public IVersionSeries getVersionSeries() throws RepositoryDocumentException{
		try {
			return new FnVersionSeries(doc.get_VersionSeries());
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}	
	}

	public String getId(ActionType action) {
		logger.info("getId, FnDocument");
		return doc.get_Id().toString();
	}
	
	public Date getModifyDate(ActionType action) throws RepositoryDocumentException{
		//String ModifyDate;
		Date ModifyDate = new Date();
		try {
			if(SpiConstants.ActionType.DELETE.equals(action)){
				ModifyDate = ((com.filenet.apiimpl.core.DeletionEventImpl) doc).get_DateLastModified();
			}else{//if action==SpiConstants.ActionType.ADD
				ModifyDate = doc.get_DateLastModified();
			}	
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
		return ModifyDate;
	}
		
	
	public String getClassNameEvent() throws RepositoryDocumentException {
		try {
			return doc.getClassName();
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
	}
	
	
	public String getVersionSeriesId() throws RepositoryDocumentException {
		Id id;
		String strId;
		try {
			id = ((com.filenet.apiimpl.core.DeletionEventImpl) doc).get_VersionSeriesId();
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
		logger.info("versionId : ID : "+id);
		strId=id.toString();
		logger.info("versionId : tostring : "+strId);
		strId = strId.substring(1,strId.length()-1);
		logger.info("versionId : cut start/end : "+strId);
		return strId;
	}
	
	public IPermissions getPermissions() {
		return new FnPermissions(doc.get_Permissions());
	}

	public InputStream getContent() throws RepositoryDocumentException{
		InputStream ip = null;
		try {
			List contentList = (List) doc.get_ContentElements();
			ContentTransfer content = (ContentTransfer) contentList.get(0);
			ip = content.accessContentStream();
			return ip;
		} catch (Error er) {
			
			er.printStackTrace();
			logger.log(Level.SEVERE,"error while trying to get the content of file "+ this.doc.get_Id() + " " + er.getMessage());
			throw new RepositoryDocumentException();
		}
	}

	public String getPropertyStringValue(String name)
			throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					Object o = prop.getStringValue();
					if (o == null)
						return null;
					return o.toString();
				}
			}
		} catch (ClassCastException e) {
			logger
					.info("ClassCastException found but still continuing for property "
							+ name);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
		return null;
	}
	
	public String getPropertyGuidValue(String name) throws RepositoryDocumentException {
		try {
			String id = null;
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					Object o = prop.getIdValue();
					if (o == null)
						return null;
					id = o.toString();	
					return id.substring(1,id.length()-1);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			e.printStackTrace();
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return null;
	}

	public long getPropertyLongValue(String name) throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					return prop.getInteger32Value().longValue();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return -1;
	}

	public double getPropertyDoubleValue(String name)
			throws RepositoryDocumentException {
		try {

			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					return prop.getFloat64Value().doubleValue();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return -1;
	}
	
	public Date getPropertyDateValueDelete(String name) throws RepositoryDocumentException {
		return new Date();
	}
	
	public Date getPropertyDateValue(String name) throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					return prop.getDateTimeValue();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return null;
	}

	public boolean getPropertyBooleanValue(String name)
			throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					return prop.getBooleanValue().booleanValue();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return false;
	}

	public byte[] getPropertyBinaryValue(String name)
			throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					return prop.getBinaryValue();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return null;
	}

}
