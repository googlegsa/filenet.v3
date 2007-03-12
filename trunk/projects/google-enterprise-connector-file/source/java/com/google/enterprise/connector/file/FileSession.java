package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileSession implements Session {

	private ISession fileSession;

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private String pathToWcmApiConfig;

	public FileSession(String iObjectFactory, String userName,
			String userPassword, String appId, String credTag,
			String objectStoreName, String pathToWcmApiConfig,
			String displayUrl, String isPublic) throws RepositoryException,
			LoginException {
		try {
			setFileObjectFactory(iObjectFactory);
			if (credTag.equals("")) {
				credTag = null;
			}
			fileSession = fileObjectFactory.getSession(appId, credTag,
					userName, userPassword);
			this.pathToWcmApiConfig = pathToWcmApiConfig;
			fileSession.setConfiguration(new FileInputStream(
					this.pathToWcmApiConfig));
			// fileSession.verify();
			objectStore = fileObjectFactory.getObjectStore(objectStoreName,
					fileSession);
			objectStore.setDisplayUrl(displayUrl
					+ "getContent?objectType=document&objectStoreName="
					+ objectStoreName + "&id=");

			objectStore.setIsPublic(isPublic);
		} catch (FileNotFoundException de) {
			RepositoryException re = new RepositoryException(de);
			throw re;
		}
	}

	private void setFileObjectFactory(String objectFactory)
			throws RepositoryException {

		try {
			fileObjectFactory = (IObjectFactory) Class.forName(objectFactory)
					.newInstance();
		} catch (InstantiationException e) {
			throw new RepositoryException(e);
		} catch (IllegalAccessException e) {
			throw new RepositoryException(e);
		} catch (ClassNotFoundException e) {
			throw new RepositoryException(e);
		}

	}

	public QueryTraversalManager getQueryTraversalManager()
			throws RepositoryException {
		FileQueryTraversalManager fileQTM = new FileQueryTraversalManager(
				fileObjectFactory, objectStore, fileSession);
		return fileQTM;
	}

	public AuthenticationManager getAuthenticationManager()
			throws RepositoryException {
		FileAuthenticationManager fileAm = new FileAuthenticationManager(
				fileObjectFactory, pathToWcmApiConfig);
		return fileAm;
	}

	public AuthorizationManager getAuthorizationManager()
			throws RepositoryException {
		
		FileAuthorizationManager fileAzm = new FileAuthorizationManager(
				fileObjectFactory, pathToWcmApiConfig, objectStore);
		return fileAzm;
	}

	public IObjectStore getObjectStore() {
		return objectStore;
	}

	public void setObjectStore(IObjectStore objectStore) {
		this.objectStore = objectStore;
	}

}
