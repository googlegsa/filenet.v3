package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.filenet.api.constants.ClassNames;
import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectSet;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

public class FileDocumentList implements DocumentList {

	private static final long serialVersionUID = 1L;

	private FileDocument fileDocumentToDelete = null;
	private IObjectStore objectStore = null;
	private IObjectSet objectSet = null;
	private IObjectSet objectSetToDelete = null;
	private FileDocument fileDocument = null;
	private Iterator ObjectIt = null;
	private Iterator ObjectItToDelete = null;

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

	public FileDocumentList(IObjectSet objectSet, IObjectSet objectSetToDelete,
			IObjectStore objectStore, boolean isPublic, String displayUrl,
			HashSet included_meta, HashSet excluded_meta, String dateFirstPush,
			String checkPoint) {
		this.objectSet = objectSet;
		this.objectSetToDelete = objectSetToDelete;
		this.objectStore = objectStore;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.dateFirstPush = dateFirstPush;
		this.lastCheckPoint = checkPoint;

		this.index = 0;
		this.ObjectIt = objectSet.getIterator();
		this.ObjectItToDelete = objectSetToDelete.getIterator();

		// Docs to Add
		logger.info(this.objectSet.getSize() + " new documents found");

		// Docs to Delete
		logger.info(this.objectSetToDelete.getSize()
				+ " new documents to remove found");
	}

	public com.google.enterprise.connector.spi.Document nextDocument()
			throws RepositoryException {
	
		int dataLen = this.objectSet.getSize();
		int dataLenToDelete = this.objectSetToDelete.getSize();

		if (index > -1 && index < dataLen) {
		 logger.info("ADD...");
		 if (this.ObjectIt.hasNext()) {
		 IBaseObject doc = (IBaseObject) this.ObjectIt.next();
		
		 docId = doc.getId(SpiConstants.ActionType.ADD);
		 Date dateLastModified = doc.getModifyDate(SpiConstants.ActionType.ADD);
		 	 
		 fileDocument = new FileDocument(docId, dateLastModified,
					this.objectStore, this.isPublic, this.displayUrl,
					this.included_meta, this.excluded_meta,
					SpiConstants.ActionType.ADD);
		 index++;
		 return fileDocument;
		 } 
		} else if ((index >= dataLen) && index<(dataLenToDelete+dataLen)){
			logger.info("DEL...");
			if (this.ObjectItToDelete.hasNext()) {
				 IBaseObject doc = (IBaseObject) this.ObjectItToDelete.next();

				 if(doc.getClassNameEvent().contains("DeletionEvent"))
				 {
					 docId = doc.getId(SpiConstants.ActionType.DELETE);
				 
					 Date dateLastModified = doc.getModifyDate(SpiConstants.ActionType.DELETE);
				 
				 	String commonVersionId = doc.getVersionSeriesId();
				 
				 	fileDocumentToDelete = new FileDocument(docId, commonVersionId, dateLastModified,
							this.objectStore, this.isPublic, this.displayUrl,
							this.included_meta, this.excluded_meta,
							SpiConstants.ActionType.DELETE);
				 	index++;
				 	return fileDocumentToDelete;
				 }
			} 
		}
		logger.info("return Null");
		index++;
		return null;
	}

	public String checkpoint() throws RepositoryException {
		logger.fine("Creation of the Checkpoint");
		String dateString = "";
		String dateStringDocumentToDelete = "";
		logger.info("fileDocument : "+fileDocument);

		if (fileDocument != null) {
			Property val = fetchAndVerifyValueForCheckpoint(fileDocument,
					SpiConstants.PROPNAME_LASTMODIFIED);
			Calendar date = null;
			try {
				String dateStr = val.nextValue().toString();
				date = FileDateValue.iso8601ToCalendar(dateStr);
			} catch (Exception e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);
			}

			FileDateValue tempDt = new FileDateValue(date);
			dateString = tempDt.FiletoIso8601();

		} else {
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				docId = jo.getString("uuid");
				dateString = jo.getString("lastModified");
			} catch (JSONException e) {
				logger.severe("JSON exception, while getting last checkpoint.");
			}
		}

		logger.info("fileDocumentToDelete : " + fileDocumentToDelete);
		logger.info("lastCheckPoint : " + lastCheckPoint);

		if (fileDocumentToDelete != null) {
			Property valToDelete = fetchAndVerifyValueForCheckpoint(
					fileDocumentToDelete, SpiConstants.PROPNAME_LASTMODIFIED);
			Calendar date = null;
			try {
				String dateStr = valToDelete.nextValue().toString();
				date = FileDateValue.iso8601ToCalendar(dateStr);
			} catch (Exception e1) {
				throw new RepositoryException("Unexpected JSON problem", e1);
			}

			FileDateValue tempDt = new FileDateValue(date);
			dateStringDocumentToDelete = tempDt.FiletoIso8601();

		} else if (lastCheckPoint != null) {
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
			dateStringDocumentToDelete = dateFirstPush;
		}

		String result = null;
		try {
			logger.info("CheckPoint");
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
		Property property = pm.findProperty(pName);
		if (property == null) {
			throw new IllegalArgumentException("checkpoint must have a "
					+ pName + " property");
		}
		return property;
	}

}
