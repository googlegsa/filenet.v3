package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.ObjectStore;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.filenet.wcm.api.BaseObject;

public class FnObjectStore implements IObjectStore {
	private ObjectStore objectStore;

	
	public FnObjectStore(ObjectStore objectStore) {
		this.objectStore = objectStore;

	}

	public IDocument getObject(String guidOrPath) {
		return new FnDocument((Document) objectStore.getObject(
				BaseObject.TYPE_DOCUMENT, guidOrPath));

	}
	public String getName() {
		return this.objectStore.getName();
	}

}
