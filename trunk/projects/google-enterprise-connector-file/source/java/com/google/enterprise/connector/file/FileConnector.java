package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileConnector implements Connector {
	private String objectFactory;

	private String login;

	private String password;

	private String appId;

	private String credTag;

	private String objectStoreName;

	private String pathToWcmApiConfig;

	private String displayUrl;

	private String isPublic;

	public Session login() throws LoginException, RepositoryException {
		Session sess = null;
		if (!(objectFactory == null || login == null || password == null
				|| appId == null || objectStoreName == null || displayUrl == null)) {

			sess = new FileSession(objectFactory, login, password, appId,
					credTag, objectStoreName, pathToWcmApiConfig, displayUrl,
					isPublic);
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

	public String getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(String objectFactory) {
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

	public String getDisplayUrl() {
		return displayUrl;
	}

	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}

	public String getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
	}

}
