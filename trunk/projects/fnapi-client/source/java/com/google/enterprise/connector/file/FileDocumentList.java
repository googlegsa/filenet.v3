package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class FileDocumentList implements DocumentList {

	private static final long serialVersionUID = 1L;

	private Document resultDoc = null;
	
	private Document resultDocToDelete = null;

	private FileDocument fileDocument = null;
	
	private FileDocument fileDocumentToDelete = null;

	private IObjectStore objectStore = null;

	private String docId = "";
	
	private String displayUrl;
	
	private String lastCheckPoint;
	
	private String dateFirstPush;
	
	private String docIdToDelete = "";

	private boolean isPublic;

	private HashSet included_meta;

	private HashSet excluded_meta;

	private int index = -1;

	private NodeList data = null;
	
	private NodeList dataToDelete = null;

	private static Logger logger = Logger.getLogger(FileDocumentList.class
			.getName());

	public FileDocumentList(Document document,Document documentToDelete, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta,String dateFirstPush,String checkPoint) {
		this.resultDoc = document;
		this.resultDocToDelete = documentToDelete;
		this.objectStore = objectStore;
		this.displayUrl = displayUrl;
		this.index = 1;
		this.data = resultDoc.getElementsByTagName("rs:data").item(0)
				.getChildNodes();
		logger.info(resultDoc.getElementsByTagName("z:row").getLength()
				+ " new documents found");
		logger.info("data : "+data);
		
		this.dataToDelete = resultDocToDelete.getElementsByTagName("rs:data").item(0).getChildNodes();
		logger.info(resultDocToDelete.getElementsByTagName("z:row").getLength()
				+ " new documents to remove found");
		logger.info("resultDocToDelete : "+resultDocToDelete.getChildNodes());
		
		
		this.isPublic = isPublic;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.lastCheckPoint=checkPoint;
		this.dateFirstPush = dateFirstPush;
	}

	public com.google.enterprise.connector.spi.Document nextDocument()
			throws RepositoryException {
		int dataLen = data.getLength();
		
		logger.info("Next Document ");
		logger.info("data.getLength() " + data.getLength());
		logger.info("dataToDelete.getLength() " + dataToDelete.getLength());
		logger.info("index " + index);
		
		if (index > -1 && index < dataLen) {
			logger.info("Next Document : IF");
			NamedNodeMap nodeMap = data.item(index).getAttributes();
			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals("Id")) {
					index++;
					if (data.item(index).getNodeType() == Node.TEXT_NODE) {
						index++;
					}
					try {
						docId = (String) nodeMap.item(j).getNodeValue();
						String dateLastModified = nodeMap.item(j-1).getNodeValue();
						logger.info("dateLastModified " + dateLastModified);
						
						fileDocument = new FileDocument((String) nodeMap.item(j)
								.getNodeValue(), dateLastModified, this.objectStore, this.isPublic,
								this.displayUrl, this.included_meta,
								this.excluded_meta, SpiConstants.ActionType.ADD);
								
						return fileDocument;
					} catch (DOMException e) {
//						 skip this document
						logger.severe("Unable to retrieve docId for item: " + e);
						throw new RepositoryDocumentException(
								"Unable to retrieve docId for item", e);
					}
				}
			}
		} else if ((index >=dataLen) && index<(dataToDelete.getLength()+dataLen-1)) {
			int indexDelete = index - dataLen + 1 ;
			NamedNodeMap nodeMap = dataToDelete.item(indexDelete).getAttributes();
			
			for (int j = 0; j < nodeMap.getLength(); j++) {
				
				if (nodeMap.item(j).getNodeName().equals("Id")) {
					index++;
					logger.info("dataToDelete.item(indexDelete).getNodeType() : "+dataToDelete.item(indexDelete).getNodeType());
					
					if (data.item(index).getNodeType() == Node.TEXT_NODE) {
						index++;
					}
					try {					
						docIdToDelete = (String) nodeMap.item(j).getNodeValue();
						logger.info("docIdToDelete : "+ docIdToDelete);
						
						String commonVersionId = nodeMap.item(j+1).getNodeValue();
						String dateLastModified = nodeMap.item(j-1).getNodeValue();
						logger.info("dateLastModified : "+dateLastModified);
						
						fileDocumentToDelete = new FileDocument(docIdToDelete, commonVersionId, dateLastModified, this.objectStore, this.isPublic,
								this.displayUrl, this.included_meta,
								this.excluded_meta, SpiConstants.ActionType.DELETE);
						index++;
						return fileDocumentToDelete;
					} catch (DOMException e) {
//						 skip this document
						logger.severe("Unable to retrieve docId for item: " + e);
						throw new RepositoryDocumentException(
								"Unable to retrieve docId for item", e);
					}
				}
			}
		}
		index++;
		return null;
	}

	public String checkpoint() throws RepositoryException {
		
		if (((docId == null) && (docIdToDelete == null))||((fileDocument == null)&&(fileDocumentToDelete == null))) {
			throw new RepositoryException("Cannot create checkpoint: No documents found.");
		}
		logger.fine("Creation of the Checkpoint");
		String dateString = "";
		String dateStringDocumentToDelete = "";
		
		if (fileDocument!=null) {
			Property val = fetchAndVerifyValueForCheckpoint(fileDocument,
					SpiConstants.PROPNAME_LASTMODIFIED);
			
			Calendar date = null;
			try {
				String dateStr = val.nextValue().toString();
				logger.info("dateStr : "+ dateStr);
				date = FileDateValue.iso8601ToCalendar(dateStr);
			} catch (ParseException e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);	
			} catch (Exception e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
						
			FileDateValue tempDt = new FileDateValue(date);
			dateString= tempDt.FiletoIso8601();
						
			logger.finest("dateString of the checkpoint of added document is "+dateString);
		}else{
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				docId = jo.getString("uuid");
				dateString = jo.getString("lastModified");
			}catch (JSONException e) {
				logger.severe("JSON exception, while getting last checkpoint.");
			}
		}
		
		logger.info("fileDocumentToDelete : " + fileDocumentToDelete);
		logger.info("lastCheckPoint : " + lastCheckPoint);
		
		if (fileDocumentToDelete!=null) {
			Property valToDelete = fetchAndVerifyValueForCheckpoint(fileDocumentToDelete,
					SpiConstants.PROPNAME_LASTMODIFIED);
			
			Calendar date = null;
			try {
				String dateStr = valToDelete.nextValue().toString();
				date = FileDateValue.iso8601ToCalendar(dateStr);
			} catch (ParseException e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);
			} 
			
			FileDateValue tempDt = new FileDateValue(date);
			dateStringDocumentToDelete= tempDt.FiletoIso8601();
						
			logger.finest("dateString of the checkpoint of deleted document is "+dateStringDocumentToDelete);
		} else if(lastCheckPoint!=null) {
			logger.fine("Get the last modified date from the last checkpoint ");
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				dateStringDocumentToDelete = jo.getString("lastRemoveDate");
				docIdToDelete = jo.getString("uuidToDelete");
			} catch (JSONException e) {
				logger.severe("JSON exception, while getting last removed date.");
			}
		} else {
			logger.fine("date of the first push : " + dateFirstPush);
			
			dateStringDocumentToDelete= dateFirstPush;
			///dateStringDocumentToDelete="2008-06-16T14:20:41";
			
		}
		
		String result = null;
		try {
			JSONObject jo = new JSONObject();
			jo.put("uuid", docId);
			jo.put("lastModified", dateString);
			jo.put("uuidToDelete", docIdToDelete);
			jo.put("lastRemoveDate", dateStringDocumentToDelete);
			result = jo.toString();
		} catch (JSONException e) {
			throw new RepositoryException("Unexpected JSON problem", e);
		}
		logger.info("checkpoint: " + result);
		return result;
	}

	protected Property fetchAndVerifyValueForCheckpoint(FileDocument pm,
			String pName) throws RepositoryException {
		logger.info("In fetchAndVerifyValueForCheckpoint : pm " + pm);
		logger.info("In fetchAndVerifyValueForCheckpoint : pName " + pName);
		
		Property property = pm.findProperty(pName);
		if (property == null) {
			throw new RepositoryException("checkpoint must have a "
					+ pName + " property");
		}
		return property;
	}

}
