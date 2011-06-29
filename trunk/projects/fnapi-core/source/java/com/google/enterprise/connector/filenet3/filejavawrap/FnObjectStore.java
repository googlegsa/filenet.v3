package com.google.enterprise.connector.filenet3.filejavawrap;

import com.google.enterprise.connector.filenet3.filewrap.IGettableObject;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.wcm.api.BaseObject;
import com.filenet.wcm.api.Document;
import com.filenet.wcm.api.GettableObject;
import com.filenet.wcm.api.InsufficientPermissionException;
import com.filenet.wcm.api.ObjectStore;
import com.filenet.wcm.api.RemoteServerException;
import com.filenet.wcm.api.VersionSeries;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FnObjectStore implements IObjectStore {
	private ObjectStore objectStore;
	private static Logger LOGGER = null;

	public FnObjectStore(ObjectStore objectStore) {
		this.objectStore = objectStore;
	}

	public IGettableObject getObject(int type, String guidOrPath) {

		GettableObject obj = objectStore.getObject(type, guidOrPath);
		if (type == BaseObject.TYPE_VERSIONSERIES) {
			VersionSeries vs = (VersionSeries) objectStore.getObject(type, guidOrPath);
			vs.refresh();
			return new FnVersionSeries(vs);
		}
		if (type == BaseObject.TYPE_DOCUMENT) {
			return new FnDocument(
					(Document) objectStore.getObject(type, guidOrPath));
		}
		return new FnGettableObject(obj);

	}

	public String getName() throws RepositoryException {

		String objectStoreName = null;
		try {
			objectStoreName = this.objectStore.getName();
		} catch (RemoteServerException e) {
			LOGGER.log(Level.WARNING, "Unable to connect to the remote server");
			throw new RepositoryException(e);
		} catch (InsufficientPermissionException e) {
			LOGGER.log(Level.WARNING, "User does not have sufficient permissions to access the Object Store");
			throw new RepositoryException(e);
		}
		return objectStoreName;
	}

}
