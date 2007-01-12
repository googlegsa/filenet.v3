package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.ObjectStore;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;

public class IFileObjectStore implements IObjectStore {
	ObjectStore objectStore;
	String displayUrl;

	public IFileObjectStore(ObjectStore objectStore) {
		this.objectStore = objectStore;
		
	}

	public IDocument getObject(int objectType, String guidOrPath) {
		return new IFileDocument((Document)objectStore.getObject(objectType, guidOrPath));
		
	}

	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
		
	}

	public String getDisplayUrl() {
		return displayUrl;
	}
	
	public String getName(){
		return this.objectStore.getName();
	}

}
