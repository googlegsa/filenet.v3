package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.logging.Level;
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
	private HashSet includedMeta;
	private HashSet excludedMeta;
	private static Logger logger;
	static {
		logger = Logger.getLogger(FileSession.class.getName());
	}

	public FileSession(String iObjectFactory, String userName,
			String userPassword, String objectStoreName,
			String refPathToWcmApiConfig, String refDisplayUrl, boolean refIsPublic,
			String refAdditionalWhereClause, HashSet refIncludedMeta,
			HashSet refExcludedMeta) throws RepositoryException {
		
		setFileObjectFactory(iObjectFactory);

		logger.info("Getting session for user "+userName);
		fileSession = fileObjectFactory.getSession("gsa-file-connector", null, userName, userPassword);
		
		logger.info("WCMApiConfig.properties path is set to: "+refPathToWcmApiConfig);
		this.pathToWcmApiConfig = refPathToWcmApiConfig;
		try {

			URL is = this.getClass().getClassLoader().getResource(this.pathToWcmApiConfig);

			if (is == null) {
				logger.log(Level.SEVERE,"WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ");
			}else{
				String sFile = URLDecoder.decode(is.getFile(), "UTF-8");
				FileInputStream fis = new FileInputStream(sFile);
				fileSession.setConfiguration(fis);
			}
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING,"Cannot Decode. Encoding format not supported.");
			throw new RepositoryException("Cannot Decode. Encoding format not supported.",e);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE,"WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ");
			throw new RepositoryException("WCMApiConfig.properties file not found. Either path is invalid or file is corrupted. ",e);
		} catch (Exception exp) {			
			throw new RepositoryLoginException(exp);
		}
		objectStore = fileObjectFactory.getObjectStore(objectStoreName, fileSession);
		this.displayUrl = refDisplayUrl + "?objectStoreName=" + objectStoreName
				+ "&objectType=document&versionStatus=1&vsId=";

		this.isPublic = refIsPublic;
		fileSession.verify();

		this.additionalWhereClause = refAdditionalWhereClause;
		this.includedMeta = refIncludedMeta;
		this.excludedMeta = refExcludedMeta;
	}

	private void setFileObjectFactory(String objectFactory)
			throws RepositoryException {

		try {
			fileObjectFactory = (IObjectFactory) Class.forName(objectFactory).newInstance();
		} catch (InstantiationException e) {
			logger.log(Level.WARNING,"Unable to instantiate the class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ");
			throw new RepositoryException("Unable to instantiate the class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ",e);
		} catch (IllegalAccessException e) {
			logger.log(Level.WARNING,"Access denied to class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ");
			throw new RepositoryException("Access denied to class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ",e);
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING,"The class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory not found");
			throw new RepositoryException("The class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory not found",e);
		}

	}

	public TraversalManager getTraversalManager() throws RepositoryException {
		FileTraversalManager fileQTM = new FileTraversalManager(
				fileObjectFactory, objectStore, fileSession, this.isPublic,
				this.displayUrl, this.additionalWhereClause,
				this.includedMeta, this.excludedMeta);
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

	public void setObjectStore(IObjectStore refObjectStore) {
		this.objectStore = refObjectStore;
	}

}
