package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.PropertyNotFoundException;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnDocument implements IDocument{
	Document doc;
	
	public FnDocument(Document doc){
		
		this.doc = doc;	
		
		
		
	}

	public InputStream getContent() {
		return doc.getContent();
		
	}

	public String getPropertyStringValue(String name) throws RepositoryException {
		try {
//			System.out.println("nouveau");
//			
//			Properties props = doc.getProperties();
//			Iterator iter = props.iterator();
//			String [] test = {Property.MIME_TYPE,Property.PERMISSIONS};
//			System.out.println(	doc.getPropertiesXML(test));
//			while(iter.hasNext()){
//				
//				Property prop = (Property)iter.next();
//System.out.println(	prop.getName() + " "+prop.getStringValue() + " " +doc.getPropertiesXML(test));
//			}
			
				return this.doc.getPropertyStringValue(name);
			
		} catch (PropertyNotFoundException de) {
			System.out.println(" PropertyNotFoundException docnmae plantage? "+doc.getName() + " " + doc.getSession().toString());
			if(name.equals(Property.MIME_TYPE)){
				
				return "application/octet-stream";	
			}else if(name.equals(Property.PERMISSIONS)){
				return "security";
			}else{
			RepositoryException re = new RepositoryException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
			}
		}
		catch (Exception de) {
			System.out.println(" Exception docnmae plantage? "+doc.getName() + " " + doc.getSession().toString() + " " + name);
			
			RepositoryException re = new RepositoryException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
			
		}
		
	}

	public double getContentSize() throws RepositoryException{
		try {
			String size = this.doc.getPropertyStringValue(Property.CONTENT_SIZE);
			if(size != null){
				return Double.parseDouble(size);
			}else{
				return 0;
			}
			
		} catch (NumberFormatException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		} catch (PropertyNotFoundException de) {
			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}
	}

//	public Date getPropertyDateValue(String date_last_modified) throws RepositoryException {
//		
//		try {
//			return this.doc.getPropertyDateValue(date_last_modified);
//		} catch (PropertyNotFoundException de) {
//			RepositoryException re = new RepositoryException(de.getMessage(),de.getCause());
//			re.setStackTrace(de.getStackTrace());
//			throw re;
//		}
//	}
//
//	public String getPermissionsXML() {
//		return this.doc.getPermissionsXML();
//		
//	}
//
	public IPermissions getPermissions() {
		return new FnPermissions(doc.getPermissions());
	}
//
//	
//	public String getPropertiesXML(String [] tab){
//		return doc.getPropertiesXML(tab);
//		
//	}
//
//	public String getAccessMask() {
//		return this.doc.getAccessMask()+"";
//	}
//	
//	public ISession getSession(){
//		return new IFileSession(this.doc.getSession());
//	}
	
	
}
