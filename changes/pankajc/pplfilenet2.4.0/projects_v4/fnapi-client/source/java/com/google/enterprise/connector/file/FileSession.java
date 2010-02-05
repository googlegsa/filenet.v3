package com.google.enterprise.connector.file;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileSession implements Session {

	private IObjectFactory fileObjectFactory;
	private IObjectStore objectStore;
	private IConnection connection;
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
			String userPassword, String objectStoreName, String displayUrl,
			String contentEngineUri, boolean isPublic,
			String additionalWhereClause, HashSet included_meta,
			HashSet excluded_meta) throws RepositoryException,
			RepositoryLoginException {

		setFileObjectFactory(iObjectFactory);

		logger.info("getting connection for content engine: "+contentEngineUri);
		connection = fileObjectFactory.getConnection(contentEngineUri);

		logger.info("trying to access object store: "+objectStoreName+" for user: "+userName);
		objectStore = fileObjectFactory.getObjectStore(objectStoreName, connection, userName, userPassword);

		logger.info("objectStore ok user:"+userName);

		if(displayUrl.contains("/getContent") && displayUrl.endsWith("/getContent")){
			this.displayUrl = displayUrl + "?objectStoreName=" + objectStoreName + "&objectType=document&versionStatus=1&vsId=";
		}else{
			this.displayUrl = displayUrl + "/getContent?objectStoreName=" + objectStoreName + "&objectType=document&versionStatus=1&vsId=";
		}
		this.isPublic = isPublic;
		this.additionalWhereClause = additionalWhereClause;
		this.included_meta = included_meta;
		this.excluded_meta = excluded_meta;
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
//		logger.info("getTraversalManager");
		FileTraversalManager fileQTM = new FileTraversalManager(
				fileObjectFactory, objectStore, this.isPublic, this.displayUrl,
				this.additionalWhereClause, this.included_meta,
				this.excluded_meta);
		return fileQTM;
	}

	public AuthenticationManager getAuthenticationManager()
			throws RepositoryException {
		FileAuthenticationManager fileAm = new FileAuthenticationManager(connection);
		return fileAm;
	}

	public AuthorizationManager getAuthorizationManager()
			throws RepositoryException {

		FileAuthorizationManager fileAzm = new FileAuthorizationManager(connection, objectStore);
		return fileAzm;
	}

}
