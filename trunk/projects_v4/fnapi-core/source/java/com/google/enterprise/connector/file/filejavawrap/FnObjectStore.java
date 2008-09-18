package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public class FnObjectStore implements IObjectStore {
	
	private ObjectStore objectStore;

	private IConnection connection;

	private String login;

	private String password;
	
	public FnObjectStore(ObjectStore objectStore, IConnection connection,
			String login, String password) {
		this.objectStore = objectStore;
		this.connection = connection;
		this.login = login;
		this.password = password;
	}
	
	public void refreshSUserContext() throws RepositoryLoginException{
		connection.getUserContext().authenticate(login, password);
	}

	public IBaseObject getObject(String type, String id)
			throws RepositoryException {
		IndependentObject obj = null;
		try {
			obj = objectStore.getObject(type, id);
			if (type.equals(ClassNames.VERSION_SERIES)) {
				VersionSeries vs = (VersionSeries) objectStore.getObject(type, id);
				vs.refresh();
				return new FnVersionSeries(vs);
			}
			if (type.equals(ClassNames.DOCUMENT)) {
				return new FnDocument((Document) objectStore.getObject(type, id));
			}
		}catch (Exception e){
			throw new RepositoryException(e);
		}
		return new FnBaseObject(obj);

	}

	public String getName() throws RepositoryException {

		String objectStoreName = null;
		try {
			objectStoreName = this.objectStore.get_Name();
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return objectStoreName;
	}

	public ObjectStore getObjectStore() {
		return objectStore;
	}

	public String getSUserLogin(){
		return login;
	}
	
	public String getSUserPassword(){
		return password;
	}
}
