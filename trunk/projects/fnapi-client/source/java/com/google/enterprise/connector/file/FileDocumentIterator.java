package com.google.enterprise.connector.file;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.google.enterprise.connector.file.filewrap.IObjectStore;

public class FileDocumentIterator implements Iterator {

	private Document resultDoc = null;

	private IObjectStore store = null;

	private int index = -1;

	private NodeList data = null;

	private String isPublic;

	private String displayUrl;

	public FileDocumentIterator(Document document, IObjectStore store,String isPublic, String displayUrl) {
		this.resultDoc = document;
		this.store = store;
		this.data = resultDoc.getElementsByTagName("rs:data").item(0).getChildNodes();
		this.index = 1;
		this.isPublic = isPublic;
		this.displayUrl = displayUrl;
	}

	public void remove() {
	}

	public boolean hasNext() {
		if (index > -1 && index < data.getLength()) {
			return true;
		}
		return false;
	}

	public Object next() {
		NamedNodeMap nodeMap = data.item(index).getAttributes();
		if (nodeMap != null) {
			for (int j = 0; j < nodeMap.getLength(); j++) {
				if (nodeMap.item(j).getNodeName().equals("Id")) {
					index++;
					if (data.item(index).getNodeType() == 3) {
						index++;
					}
					return new FileDocumentPropertyMap((String) nodeMap.item(j)
							.getNodeValue(), this.store, this.isPublic, this.displayUrl);
				}
			}
		}
		index++;
		return null;
	}

}
