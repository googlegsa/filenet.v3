package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class FileDocumentList implements DocumentList {

	private static final long serialVersionUID = 1L;

	private Document resultDoc = null;

	private FileDocument fileDocument = null;

	private String docId = null;

	private IObjectStore objectStore = null;

	private String displayUrl;

	private boolean isPublic;

	private HashSet included_meta;

	private HashSet excluded_meta;

	private int index = -1;

	private NodeList data = null;

	private static Logger logger = Logger.getLogger(FileDocumentList.class
			.getName());

	public FileDocumentList(Document document, IObjectStore objectStore,
			boolean isPublic, String displayUrl, HashSet included_meta,
			HashSet excluded_meta) {
		this.resultDoc = document;
		this.objectStore = objectStore;
		this.displayUrl = displayUrl;
		this.index = 1;
		this.data = resultDoc.getElementsByTagName("rs:data").item(0)
				.getChildNodes();
		logger.info(resultDoc.getElementsByTagName("z:row").getLength() 
				+ " new documents find");
		this.isPublic = isPublic;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
	}

	public com.google.enterprise.connector.spi.Document nextDocument()
			throws RepositoryException {
		if (index > -1 && index < data.getLength()) {
			NamedNodeMap nodeMap = data.item(index).getAttributes();
			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals("Id")) {
					index++;
					if (data.item(index).getNodeType() == 3) {
						index++;
					}
					docId = (String) nodeMap.item(j).getNodeValue();
					fileDocument = new FileDocument((String) nodeMap.item(j)
							.getNodeValue(), this.objectStore, this.isPublic,
							this.displayUrl, this.included_meta,
							this.excluded_meta);
					return fileDocument;
				}
			}
		}
		index++;
		return null;
	}

	public String checkpoint() throws RepositoryException {
		Property val = fetchAndVerifyValueForCheckpoint(fileDocument,
				SpiConstants.PROPNAME_LASTMODIFIED);
		Calendar date = null;
		try {
			date = FileDateValue.iso8601ToCalendar(val.nextValue().toString());
		} catch (ParseException e1) {
			throw new RepositoryException("Unexpected JSON problem", e1);
		} catch (RepositoryException e1) {
			throw new RepositoryException("Unexpected JSON problem", e1);
		}
		date.setTimeInMillis(date.getTimeInMillis()); //- 7200000
		FileDateValue nativeFormatDateCalendar = new FileDateValue(date);
		String nativeFormatDate = nativeFormatDateCalendar.toIso8601();
		String dateString = nativeFormatDate;

		String result = null;
		try {
			JSONObject jo = new JSONObject();
			jo.put("uuid", docId);
			jo.put("lastModified", dateString);
			result = jo.toString();
		} catch (JSONException e) {
			throw new RepositoryException("Unexpected JSON problem", e);
		}
		logger.info("checkpoint: "+result);
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
