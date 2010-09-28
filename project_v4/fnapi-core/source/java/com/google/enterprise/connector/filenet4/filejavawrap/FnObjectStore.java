package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.filenet.wcm.api.InsufficientPermissionException;
import com.filenet.wcm.api.RemoteServerException;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public class FnObjectStore implements IObjectStore {

	private ObjectStore objectStore;
	private IConnection connection;
	private String login;
	private String password;
	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnDocument.class.getName());
	}
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
			throws RepositoryDocumentException {
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
			logger.log(Level.WARNING, "Unable to get Object got VersionSeries or Document",e);
			throw new RepositoryDocumentException(e);
		}
		return new FnBaseObject(obj);

	}

	public String getName() throws RepositoryException {

		String objectStoreName = null;
		try {
			objectStoreName = this.objectStore.get_Name();
		} catch (RemoteServerException e) {
			logger.log(Level.WARNING, "Unable to connect to the remote server");
			throw new RepositoryException(e);
		}catch(InsufficientPermissionException e){
			logger.log(Level.WARNING, "User does not have sufficient permissions to access the Object Store");
			throw new RepositoryException(e);
		}catch (Exception e) {
			logger.log(Level.WARNING, "Unable to get Name");
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
