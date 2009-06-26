package com.google.enterprise.connector.file.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.GettableObject;
import com.filenet.wcm.api.InsufficientPermissionException;
import com.filenet.wcm.api.ObjectStore;
import com.filenet.wcm.api.RemoteServerException;
import com.filenet.wcm.api.VersionSeries;
import com.google.enterprise.connector.file.filewrap.IGettableObject;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;
import com.filenet.wcm.api.BaseObject;

public class FnObjectStore implements IObjectStore {
	private ObjectStore objectStore;
	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}
	public FnObjectStore(ObjectStore objectStore) {
		this.objectStore = objectStore;

	}

	public IGettableObject getObject(int type, String guidOrPath) {

		GettableObject obj = objectStore.getObject(type, guidOrPath);
		if (type == BaseObject.TYPE_VERSIONSERIES) {
			VersionSeries vs = (VersionSeries) objectStore.getObject(type,
					guidOrPath);
			vs.refresh();
			return new FnVersionSeries(vs);
		}
		if (type == BaseObject.TYPE_DOCUMENT) {
			return new FnDocument((Document) objectStore.getObject(type,
					guidOrPath));
		}
		return new FnGettableObject(obj);

	}

	public String getName() throws RepositoryException {

		String objectStoreName = null;
		try {
			objectStoreName = this.objectStore.getName();
		} catch (RemoteServerException e) {
			logger.log(Level.WARNING, "Unable to connect to the remote server");
			throw new RepositoryException(e);
		}catch(InsufficientPermissionException e){
			logger.log(Level.WARNING, "User does not have sufficient permissions to access the Object Store");
			throw new RepositoryException(e);
		}
		return objectStoreName;
	}

}
