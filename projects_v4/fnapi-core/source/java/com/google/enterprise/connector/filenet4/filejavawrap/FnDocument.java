// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4.filejavawrap;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.collection.PropertyDescriptionList;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.meta.PropertyDescription;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;
import com.filenet.wcm.api.BaseRuntimeException;
import com.filenet.wcm.api.InsufficientPermissionException;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.DoubleValue;
import com.google.enterprise.connector.spiimpl.LongValue;
import com.google.enterprise.connector.spiimpl.StringValue;
/**
 * Core document class, which directly interacts with the core FileNet APIs related to Documents.
 * @author pankaj_chouhan
 *
 */
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

	public void fetch(Set includedMeta) throws RepositoryDocumentException {
		PropertyFilter pf = new PropertyFilter();
		if (includedMeta != null){
			if(includedMeta.size() == 0){
				pf.addIncludeProperty(new FilterElement(null, null, null,(String)null)); //Removed the hardcoded metadata values. -Pankaj 06/05/2009
			}else{
				pf.addIncludeProperty(new FilterElement(null, null, null,"Id ClassDescription ContentElements DateLastModified MimeType VersionSeries"));
			}

			for (Object object : includedMeta) {
				pf.addIncludeProperty(new FilterElement(null, null, null,(String)object));
			}
		}

		pf.setMaxRecursion(1);
		try{
		doc.fetchProperties(pf);
		}catch(Exception e){
			e.printStackTrace();
		}

		setMetaTypes();
		setMeta();
	}

	private void setMetaTypes() {
		metaTypes = new HashMap();
		String propertyName, propertyType;
		PropertyDescriptionList propDescList = doc.get_ClassDescription()
				.get_PropertyDescriptions();

		Iterator it8 = propDescList.iterator();
		while (it8.hasNext()) {
			PropertyDescription propDesc = (PropertyDescription) it8.next();
			propertyName = propDesc.get_SymbolicName().toUpperCase();
			propertyType = propDesc.get_DataType().toString();
			metaTypes.put(propertyName, propertyType);
		}
	}

	private void setMeta() {
		metas = new HashMap();
		Properties props;
		Object value;
		props = doc.getProperties();
		Property[] prop = props.toArray();
		for (Property property : prop) {
			value = property.getObjectValue();
			if (value != null){
				metas.put(property.getPropertyName(), value);
			}
		}
	}

	public Set getPropertyName() {
		return metas.keySet();
	}

	public String getPropertyType(String name) throws RepositoryDocumentException {
		return (String) metaTypes.get(name.toUpperCase());
	}

	public IVersionSeries getVersionSeries() {
		return new FnVersionSeries(doc.get_VersionSeries());
	}

	public String getId(ActionType action) {
		return doc.get_Id().toString();
	}

	public Date getModifyDate(ActionType action) throws RepositoryDocumentException {
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
		return doc.getClassName();
	}


	public String getVersionSeriesId() throws RepositoryDocumentException {
		Id id;
		String strId;
		try {
			id = ((com.filenet.apiimpl.core.DeletionEventImpl) doc).get_VersionSeriesId();
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
		strId=id.toString();
		strId = strId.substring(1,strId.length()-1);
		return strId;
	}

	public IPermissions getPermissions() {
		return new FnPermissions(doc.get_Permissions());
	}

	public InputStream getContent() {
		InputStream ip = null;
		try {
			List contentList = (List) doc.get_ContentElements();
			ContentTransfer content = (ContentTransfer) contentList.get(0);
			ip = content.accessContentStream();
			return ip;
		} catch (InsufficientPermissionException e) {
			logger.log(Level.WARNING, "User does not have sufficient permission to retrive the content of document for "
					+ this.doc.get_Id() + " " + e.getLocalizedMessage());
			return ip;
		} catch (BaseRuntimeException e) {
			logger.log(Level.WARNING, "Unable to retrieve the content of file for "
					+ this.doc.get_Id() + " " + e.getLocalizedMessage());
			return ip;
		} catch (Exception er) {
			logger.log(Level.WARNING, "Unable to retrieve the content of file for "
					+ this.doc.get_Id() + " " + er.getLocalizedMessage(), er);
			return ip;
		}
	}

	/**
	 * Fetches the String type metadata from FileNet.
	 */
	public void getPropertyStringValue(String name, List list) throws RepositoryDocumentException{
		Object value = null;
		try {
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			for (Property prop : property) {
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								list.add(new StringValue(object.toString()));
							}
						}else{
							list.add(new StringValue(prop.getStringValue()));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "Encountered ClassCastException while fetching values for property ["+ name +"] Skipping the current value [" + (String)value +"]",e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}

	/**
	 * Fetches the GUID type metadata from FileNet.
	 */
	public void getPropertyGuidValue(String name, List list) throws RepositoryDocumentException {
		try {
			String id = null;
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			for (Property prop : property) {
				String propName = prop.getPropertyName();

				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								id = object.toString();
								if(id != null)
									//Whenever the ID is retrieved from FileNet, it comes with "{"
									// and "}" surrounded and ID is in between these curly braces
									//FileNEt connector needs ID without curly braces. Thus removing
									//the curly braces.
									list.add(new StringValue(id.substring(1,id.length()-1)));
							}
						}else{
							id = prop.getIdValue().toString();
							if(id != null)
								//Whenever the ID is retrieved from FileNet, it comes with "{"
								// and "}" surrounded and ID is in between these curly braces
								//FileNEt connector needs ID without curly braces. Thus removing
								//the curly braces.
								list.add(new StringValue(id.substring(1,id.length()-1)));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}

	/**
	 * Fetches the Integer type metadata from FileNet.
	 */
	public void getPropertyLongValue(String name, List list) throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			for (Property prop : property) {
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								//FileNet only supports Integer type and connector-manager contains only LongValue
								//Thus need to map Integer value of FileNet to LongValue.
								list.add(new LongValue(((Integer)object).longValue()));
							}
						}else{
							list.add(new LongValue(prop.getInteger32Value().longValue()));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}

	/**
	 * Fetches the Double/Float type metadata from FileNet.
	 */
	public void getPropertyDoubleValue(String name, List list)
			throws RepositoryDocumentException {
		try {

			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			for (Property prop : property) {
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								if(object instanceof Double)
									list.add(new DoubleValue(((Double)object).doubleValue()));
							}
						}else{
							list.add(new DoubleValue(prop.getFloat64Value().doubleValue()));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}
	public Date getPropertyDateValueDelete(String name) throws RepositoryDocumentException {
		return new Date();
	}

	/**
	 * Fetches the Date type metadata from FileNet.
	 */
	public void getPropertyDateValue(String name, List list) throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			Iterator it = props.iterator();
			for (Property prop : property) {
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								Calendar c = Calendar.getInstance();
								c.setTime((Date)object);
								list.add(new DateValue(c));
							}
						}else{
							Calendar c = Calendar.getInstance();
							c.setTime(prop.getDateTimeValue());
							list.add(new DateValue(c));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}

	/**
	 * Fetches the Boolean type metadata from FileNet.
	 */
	public void getPropertyBooleanValue(String name, List list)
			throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			for (Property prop : property) {
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							for (Object object : (List)value) {
								list.add(BooleanValue.makeBooleanValue(((Boolean)object).booleanValue()));
							}
						}else{
							list.add(BooleanValue.makeBooleanValue((prop.getBooleanValue().booleanValue())));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}

	public void getPropertyBinaryValue(String name, List list)
			throws RepositoryDocumentException {
		try {
			Properties props = doc.getProperties();
			Property[] property = props.toArray();
			Object value;
			for (Property prop : property) {
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					value = prop.getObjectValue();
					if(value!=null){
						if(value instanceof List){
							logger.log(Level.WARNING, "Binary MultiValued Metadat is currently not supported. Binary MultiValued metadata will not be fed to GSA");
						}else{
							list.add(new BinaryValue(prop.getBinaryValue()));
						}
					}
					return;
				}
			}
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, "ClassCastException found but still continuing for property "+ name,e);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "Error while trying to get the property "
					+ name + " of the file " + this.doc.get_Id() + " "
					+ e1.getMessage(),e1);
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}
}
