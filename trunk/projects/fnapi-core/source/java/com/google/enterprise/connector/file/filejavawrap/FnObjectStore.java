package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.ObjectStore;
import com.filenet.wcm.api.RemoteServerException;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;
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
	public String getName() throws RepositoryException {
		String objectStoreName = null;
		try{
			objectStoreName = this.objectStore.getName();
		}catch(RemoteServerException e){
			throw new RepositoryException(e);
		}
		return objectStoreName;
	}

}
