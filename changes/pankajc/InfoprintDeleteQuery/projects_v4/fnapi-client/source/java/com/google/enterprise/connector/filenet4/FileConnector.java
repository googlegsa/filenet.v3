/* Copyright 2009 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class FileConnector implements Connector {

	private String objectFactory;
	private String username;
	private String password;
	private String objectStore;
	private String workplaceDisplayUrl;
	private String contentEngineUrl;
	private String isPublic = "false";
	private String additionalWhereClause;
	private String additionalDeleteWhereClause;
	private HashSet includedMeta;
	private HashSet excludedMeta;
	private String dbTimezone;
	private static Logger LOGGER = Logger.getLogger(FileConnector.class.getName());

	public String getDbTimezone() {
		return dbTimezone;
	}

	public String getAdditionalDeleteWhereClause() {
		return additionalDeleteWhereClause;
	}

	public void setAdditionalDeleteWhereClause(
			String additionalDeleteWhereClause) {
		this.additionalDeleteWhereClause = additionalDeleteWhereClause;
	}

	public void setDbTimezone(String dbTimezone) {
		dbTimezone = dbTimezone;
		LOGGER.config("Set Database Server's TimeZone to " + this.dbTimezone);
	}

	/**
	 * login method is to create a new instance of a connector by instantiating
	 * the Connector interface. It starts access to managers for authentication,
	 * authorization, and traversal and returns a Session object that passes
	 * data and objects between the connector manager and a connector.
	 * 
	 * @param Username which needs to be authorized.
	 * @return com.google.enterprise.connector.spi.Session;
	 */

	public Session login() throws RepositoryLoginException, RepositoryException {

		URL conf = FileConnector.class.getResource("/jaas.conf");
		if (conf != null) {
			LOGGER.info("setting sytem property java.security.auth.login.config to "
					+ conf.getPath());
			// System.setProperty("java.security.auth.login.config",
			// conf.getPath());
		} else {
			LOGGER.warning("Unable to find URL of file jaas.conf");
			// System.setProperty("java.security.auth.login.config",
			// "F:\\Program Files\\GoogleConnectors\\FileNET2\\Tomcat\\webapps\\connector-manager\\WEB-INF\\classes\\jaas.conf");
		}

		HostnameVerifier aa = new FileHNV();
		HttpsURLConnection.setDefaultHostnameVerifier(aa);

		Session sess = null;
		if (!(objectFactory == null || username == null || password == null
				|| objectStore == null || workplaceDisplayUrl == null || contentEngineUrl == null)) {

			LOGGER.info("Creating fileSession object...");
			sess = new FileSession(objectFactory, username, password,
					objectStore, workplaceDisplayUrl, contentEngineUrl,
					isPublic.equals("on"), additionalWhereClause,
					additionalDeleteWhereClause, includedMeta, excludedMeta,
					dbTimezone);
		}
		return sess;

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		LOGGER.config("Set Password");
	}

	public String getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(String objectFactory) {
		this.objectFactory = objectFactory;
		LOGGER.config("Set Object Factory to " + this.objectFactory);
	}

	public String getObjectStore() {
		return objectStore;
	}

	public void setObjectStore(String objectStoreName) {
		this.objectStore = objectStoreName;
		LOGGER.config("Set Object Store to " + this.objectStore);
	}

	public String getWorkplaceDisplayUrl() {
		return workplaceDisplayUrl;
	}

	public void setWorkplaceDisplayUrl(String displayUrl) {
		this.workplaceDisplayUrl = displayUrl;
		LOGGER.config("Set Workplace Display URL to "
				+ this.workplaceDisplayUrl);
	}

	public String getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
		LOGGER.config("Set IsPublic to " + this.isPublic);
	}

	public String getAdditionalWhereClause() {
		return additionalWhereClause;
	}

	public void setAdditionalWhereClause(String additionalWhereClause) {
		this.additionalWhereClause = additionalWhereClause;
		LOGGER.config("Set Additional Where Clause to "
				+ this.additionalWhereClause);
	}

	public HashSet getExcludedMeta() {
		return excludedMeta;
	}

	public void setExcludedMeta(HashSet excludedMeta) {
		this.excludedMeta = excludedMeta;
		LOGGER.config("Setting excludedMeta to " + excludedMeta);
	}

	public HashSet getIncludedMeta() {
		return includedMeta;
	}

	public void setIncludedMeta(HashSet includedMeta) {
		this.includedMeta = includedMeta;
		LOGGER.config("Setting includedMeta to " + includedMeta);
	}

	public String getContentEngineUrl() {
		return contentEngineUrl;
	}

	public void setContentEngineUrl(String contentEngineUrl) {
		this.contentEngineUrl = contentEngineUrl;
		LOGGER.config("Set Content Engine URL to " + this.contentEngineUrl);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		LOGGER.config("Set UserName to " + this.username);
	}

}
