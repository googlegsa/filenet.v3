// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileConnector implements Connector {

	private String object_factory;

	private String username;

	private String password;

	private String object_store;

	private String path_to_WcmApiConfig;

	private String workplace_display_url;

	private String is_public = "false";

	private String useIDForChangeDetection = "false";

	private String authentication_type;

	private String additional_where_clause;

	private String additional_delete_where_clause;

	private HashSet included_meta;

	private HashSet excluded_meta;

	private static Logger LOGGER = Logger.getLogger(FileConnectorType.class.getName());

	public String getUseIDForChangeDetection() {
		return useIDForChangeDetection;
	}

	public void setUseIDForChangeDetection(String useIDForChangeDetection) {
		this.useIDForChangeDetection = useIDForChangeDetection;
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

	public Session login() throws RepositoryException {
		Session sess = null;
		if (!(object_factory == null || username == null || password == null
				|| object_store == null || workplace_display_url == null)) {

			LOGGER.info("creating FileNet session");
			sess = new FileSession(object_factory, username, password,
					object_store, path_to_WcmApiConfig, workplace_display_url,
					is_public.equals("true"),
					useIDForChangeDetection.equals("true"),
					additional_where_clause, additional_delete_where_clause,
					included_meta, excluded_meta);
			LOGGER.info("FileNet Seesion creation succeeded");
		}
		return sess;

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String refUsername) {
		this.username = refUsername;
		LOGGER.log(Level.CONFIG, "Set login to " + refUsername);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String refPassword) {
		this.password = refPassword;
		LOGGER.log(Level.CONFIG, "Set password");
	}

	public String getObject_factory() {
		return object_factory;
	}

	public void setObject_factory(String objectFactory) {
		this.object_factory = objectFactory;
		LOGGER.log(Level.CONFIG, "Set ObjectFactory to " + objectFactory);
	}

	public String getObject_store() {
		return object_store;
	}

	public void setObject_store(String objectStoreName) {
		this.object_store = objectStoreName;
		LOGGER.log(Level.CONFIG, "Set Object Store to " + object_store);
	}

	public String getPath_to_WcmApiConfig() {
		return path_to_WcmApiConfig;
	}

	public void setPath_to_WcmApiConfig(String pathToWcmApiConfig) {
		this.path_to_WcmApiConfig = pathToWcmApiConfig;
		LOGGER.log(Level.CONFIG, "Set path_to_WcmApiConfig to "
				+ path_to_WcmApiConfig);
	}

	public String getWorkplace_display_url() {
		return workplace_display_url;
	}

	public void setWorkplace_display_url(String displayUrl) {
		this.workplace_display_url = displayUrl;
		LOGGER.log(Level.CONFIG, "Set workplace_display_url to "
				+ workplace_display_url);
	}

	public String getIs_public() {
		return is_public;
	}

	public void setIs_public(String isPublic) {
		this.is_public = isPublic;
		LOGGER.log(Level.CONFIG, "Set is_public to " + is_public);
	}

	public String getAdditional_where_clause() {
		return additional_where_clause;
	}

	public void setAdditional_where_clause(String additionalWhereClause) {
		this.additional_where_clause = additionalWhereClause;
		LOGGER.log(Level.CONFIG, "Set additional_where_clause to "
				+ additional_where_clause);
	}

	public String getAdditional_delete_where_clause() {
		return additional_delete_where_clause;
	}

	public void setAdditional_delete_where_clause(
			String additionalDeleteWhereClause) {
		additional_delete_where_clause = additionalDeleteWhereClause;
	}

	public String getAuthentication_type() {
		return authentication_type;
	}

	public void setAuthentication_type(String authenticationType) {
		this.authentication_type = authenticationType;
		LOGGER.log(Level.CONFIG, "Set authentication_type to "
				+ authentication_type);
	}

	public HashSet getExcluded_meta() {
		return excluded_meta;
	}

	public void setExcluded_meta(HashSet excluded_meta) {
		this.excluded_meta = excluded_meta;
		LOGGER.log(Level.CONFIG, "Set excluded_meta to " + excluded_meta);
	}

	public HashSet getIncluded_meta() {
		return included_meta;
	}

	public void setIncluded_meta(HashSet included_meta) {
		this.included_meta = included_meta;
		LOGGER.log(Level.CONFIG, "Set included_meta to " + included_meta);
	}

}
