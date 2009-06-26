package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileSession implements Session {

	private ISession fileSession;

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private String pathToWcmApiConfig;

	private String displayUrl;

	private boolean isPublic;

	private String additionalWhereClause;

	private HashSet included_meta;

	private HashSet excluded_meta;

	private static Logger logger;

	static {
		logger = Logger.getLogger(FileSession.class.getName());
	}

	public FileSession(String iObjectFactory, String userName,
			String userPassword, String objectStoreName,
			String pathToWcmApiConfig, String displayUrl, boolean isPublic,
			String additionalWhereClause, HashSet included_meta,
			HashSet excluded_meta) throws RepositoryException,
			RepositoryLoginException {
		setFileObjectFactory(iObjectFactory);

		fileSession = fileObjectFactory.getSession("gsa-file-connector", null,
				userName, userPassword);
		this.pathToWcmApiConfig = pathToWcmApiConfig;
		try {

			URL is = this.getClass().getClassLoader().getResource(
					this.pathToWcmApiConfig);

			if (is == null) {
				URL f = this.getClass().getClassLoader().getResource("");
				logger.info(f.toString());
				logger.info(this.pathToWcmApiConfig + " is not valid.");
			}
			String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
			FileInputStream fis = new FileInputStream(sFile);

			fileSession.setConfiguration(fis);
		} catch (Exception exp) {
			exp.printStackTrace();
			throw new RepositoryLoginException(exp);
		}
		objectStore = fileObjectFactory.getObjectStore(objectStoreName,
				fileSession);
		this.displayUrl = displayUrl + "?objectStoreName=" + objectStoreName
				+ "&objectType=document&versionStatus=1&vsId=";

		this.isPublic = isPublic;
		fileSession.verify();

		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
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

	public TraversalManager getTraversalManager() throws RepositoryException {
		FileTraversalManager fileQTM = new FileTraversalManager(
				fileObjectFactory, objectStore, fileSession, this.isPublic,
				this.displayUrl, this.additionalWhereClause,
				this.included_meta, this.excluded_meta);
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
				fileObjectFactory, pathToWcmApiConfig, objectStore, fileSession);
		return fileAzm;
	}

	public IObjectStore getObjectStore() {
		return objectStore;
	}

	public void setObjectStore(IObjectStore objectStore) {
		this.objectStore = objectStore;
	}

}
