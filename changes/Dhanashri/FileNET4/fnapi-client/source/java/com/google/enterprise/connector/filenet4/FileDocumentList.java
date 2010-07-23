package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private HashSet included_meta;
	private HashSet excluded_meta;
	private int index = -1;
	// private NodeList data = null;
	// private NodeList dataToDelete = null;
	private static Logger logger = Logger.getLogger(FileDocumentList.class.getName());

	public FileDocumentList(IObjectSet objectSet, IObjectSet objectSetToDelete,
			IObjectStore objectStore, String displayUrl, HashSet included_meta,
			HashSet excluded_meta, String dateFirstPush, String checkPoint) {
		this.objectSet = objectSet;
		this.objectSetToDelete = objectSetToDelete;
		this.objectStore = objectStore;
		this.displayUrl = displayUrl;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
		this.dateFirstPush = dateFirstPush;
		this.lastCheckPoint = checkPoint;

		this.index = 0;
		this.ObjectIt = objectSet.getIterator();
		this.ObjectItToDelete = objectSetToDelete.getIterator();

		// Docs to Add
		logger.log(Level.INFO, "Number of new documents discovered: "
				+ this.objectSet.getSize());

		// Docs to Delete
		logger.log(Level.INFO, "Number of new documents to be removed: "
				+ this.objectSetToDelete.getSize());
	}

	public com.google.enterprise.connector.spi.Document nextDocument()
			throws RepositoryDocumentException {
		logger.entering("FileDocumentList", "nextDocument()");

		int dataLen = this.objectSet.getSize();
		int dataLenToDelete = this.objectSetToDelete.getSize();

		logger.log(Level.FINE, "Number of documents to be retrieved: "
				+ dataLen);
		logger.log(Level.FINE, "Number of indexes to be deleted from appliance: "
				+ dataLenToDelete);
		logger.log(Level.FINE, "Index of the document in list " + index);

		if (index > -1 && index < dataLen) {
			logger.info("ADD...");
			if (this.ObjectIt.hasNext()) {
				IBaseObject doc = (IBaseObject) this.ObjectIt.next();

				docId = doc.getId(SpiConstants.ActionType.ADD);
				Date dateLastModified = doc.getModifyDate(SpiConstants.ActionType.ADD);

				fileDocument = new FileDocument(docId, dateLastModified,
						this.objectStore, this.displayUrl, this.included_meta,
						this.excluded_meta, SpiConstants.ActionType.ADD);
				index++;
				return fileDocument;
			}
		} else if ((index >= dataLen) && index < (dataLenToDelete + dataLen)) {
			logger.info("DEL...");
			if (this.ObjectItToDelete.hasNext()) {
				IBaseObject doc = (IBaseObject) this.ObjectItToDelete.next();
				if (doc.getClassNameEvent().contains("DeletionEvent")) {
					docId = doc.getId(SpiConstants.ActionType.DELETE);
					Date dateLastModified = doc.getModifyDate(SpiConstants.ActionType.DELETE);
					String commonVersionId = doc.getVersionSeriesId();

					// Since the record url sent to GSA contains "{" and "}" pre
					// and post of the commonVersionId
					// we need to append braces so that indexed document can get
					// deleted from the appliance.
					docId = "{" + docId + "}";
					commonVersionId = "{" + commonVersionId + "}";
					fileDocumentToDelete = new FileDocument(docId,
							commonVersionId, dateLastModified,
							this.objectStore, this.displayUrl,
							this.included_meta, this.excluded_meta,
							SpiConstants.ActionType.DELETE);
					index++;
					return fileDocumentToDelete;
				}
			}
		}
		// logger.info("return Null");
		index++;
		return null;
	}

	public String checkpoint() throws RepositoryException {
		logger.log(Level.FINE, "Creation of the Checkpoint");
		String dateString = "";
		String dateStringDocumentToDelete = "";
		logger.info("fileDocument : " + fileDocument);

		if (fileDocument != null) {
			Property val = fetchAndVerifyValueForCheckpoint(fileDocument, SpiConstants.PROPNAME_LASTMODIFIED);

			try {
				String dateStr = val.nextValue().toString();
				dateString = FileDateValue.calendarToIso8601(dateStr);
			} catch (ParseException e1) {
				logger.log(Level.WARNING, "Unable to parse the date string for add. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				logger.log(Level.WARNING, "Unable to parse the date string for add. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
			logger.log(Level.FINE, "dateString of the checkpoint of added document is "
					+ dateString);

		} else {
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				docId = jo.getString("uuid");
				dateString = jo.getString("lastModified");
			} catch (JSONException e) {
				logger.log(Level.WARNING, "JSON exception, while getting last checkpoint.", e);
				throw new RepositoryException(
						"JSON exception, while getting last checkpoint.", e);
			}
		}

		logger.info("fileDocumentToDelete : " + fileDocumentToDelete);
		logger.info("lastCheckPoint : " + lastCheckPoint);

		if (fileDocumentToDelete != null) {
			Property valToDelete = fetchAndVerifyValueForCheckpoint(fileDocumentToDelete, SpiConstants.PROPNAME_LASTMODIFIED);
			Calendar date = null;
			try {
				String dateStr = valToDelete.nextValue().toString();
				dateStringDocumentToDelete = FileDateValue.calendarToIso8601(dateStr);
			} catch (ParseException e1) {
				logger.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			} catch (Exception e1) {
				logger.log(Level.WARNING, "Unable to parse the date string for delete. Date string format may be incorrect.", e1);
				throw new RepositoryException("Unexpected JSON problem", e1);
			}
			logger.log(Level.FINE, "dateString of the checkpoint of deleted document is "
					+ dateStringDocumentToDelete);
		} else if (lastCheckPoint != null) {
			logger.fine("Get the last modified date from the last checkpoint ");
			JSONObject jo;
			try {
				jo = new JSONObject(lastCheckPoint);
				dateStringDocumentToDelete = jo.getString("lastRemoveDate");
				docIdToDelete = jo.getString("uuidToDelete");
			} catch (JSONException e) {
				logger.log(Level.WARNING, "JSON exception, while getting last removed date.", e);
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
			logger.log(Level.WARNING, "Unable to create String out of JSON Object");
			throw new RepositoryException("Unexpected JSON problem", e);
		}
		logger.info("checkpoint: " + result);
		return result;
	}

	protected Property fetchAndVerifyValueForCheckpoint(FileDocument pm,
			String pName) throws RepositoryException {
		Property property = pm.findProperty(pName);
		if (property == null) {
			logger.log(Level.WARNING, "Checkpoint must have a " + pName
					+ " property");
			throw new RepositoryException("Checkpoint must have a " + pName
					+ " property");
		}
		return property;
	}

}
