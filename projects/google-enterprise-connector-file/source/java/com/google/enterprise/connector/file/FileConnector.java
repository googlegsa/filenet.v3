package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileConnector implements Connector {
	private IObjectFactory objectFactory;
	private String login;
	private String password;
	private String appId;
	private String credTag;
	private String objectStoreName;
	private String pathToWcmApiConfig;
	private String displayUrl;

	
	public Session login() throws LoginException, RepositoryException {
		Session sess = null;
		if (!(objectFactory == null||login == null||password == null||appId == null||objectStoreName == null||displayUrl == null)){
			
			sess = new FileSession(objectFactory,login,password,appId,credTag,objectStoreName,pathToWcmApiConfig, displayUrl);
		} else {
			sess = new FileSession();
		}
		return sess;
		
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getCredTag() {
		return credTag;
	}

	public void setCredTag(String credTag) {
		this.credTag = credTag;
	}

	public IObjectFactory getIObjectFactory() {
		return objectFactory;
	}

	public void setIObjectFactory(IObjectFactory objectFactory) {
		objectFactory = objectFactory;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public IObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(IObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public String getObjectStoreName() {
		return objectStoreName;
	}

	public void setObjectStoreName(String objectStoreName) {
		this.objectStoreName = objectStoreName;
	}

	public String getPathToWcmApiConfig() {
		return pathToWcmApiConfig;
	}

	public void setPathToWcmApiConfig(String pathToWcmApiConfig) {
		this.pathToWcmApiConfig = pathToWcmApiConfig;
	}

	

}
