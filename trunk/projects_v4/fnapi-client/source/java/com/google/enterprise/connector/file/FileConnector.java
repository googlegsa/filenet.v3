package com.google.enterprise.connector.file;

import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileConnector implements Connector {

	private String object_factory;
	private String username;
	private String password;
	private String object_store;
	private String workplace_display_url;
	private String content_engine_url;
	private String is_public = "false";
	private String additional_where_clause;
	private HashSet included_meta;
	private HashSet excluded_meta;
	private static Logger logger = null;
	{
		logger = Logger.getLogger(FileConnector.class.getName());
	}
	
	public Session login() throws RepositoryLoginException, RepositoryException {

		
		URL conf = FileConnector.class.getResource("/jaas.conf");
		if (conf!=null){
			logger.info("setting sytem property java.security.auth.login.config to "+conf.getPath());
//			System.setProperty("java.security.auth.login.config", conf.getPath());
		}else{
			logger.warning("Unable to find URL of file jaas.conf");
//			System.setProperty("java.security.auth.login.config", "F:\\Program Files\\GoogleConnectors\\FileNET2\\Tomcat\\webapps\\connector-manager\\WEB-INF\\classes\\jaas.conf");
		}
		
		HostnameVerifier aa = new FileHNV();
		HttpsURLConnection.setDefaultHostnameVerifier(aa);
		
		Session sess = null;
		if (!(object_factory == null || username == null || password == null
				|| object_store == null || workplace_display_url == null || content_engine_url == null)) {

			logger.info("Creating fileSession object...");
			sess = new FileSession(object_factory, username, password,
					object_store, workplace_display_url, content_engine_url,
					is_public.equals("on"), additional_where_clause,
					included_meta, excluded_meta);
		}
		return sess;

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		logger.config("Set Password");
	}

	public String getObject_factory() {
		return object_factory;
	}

	public void setObject_factory(String objectFactory) {
		this.object_factory = objectFactory;
		logger.config("Set Object Factory to "+this.object_factory);
	}

	public String getObject_store() {
		return object_store;
	}

	public void setObject_store(String objectStoreName) {
		this.object_store = objectStoreName;
		logger.config("Set Object Store to "+this.object_store);
	}

	public String getWorkplace_display_url() {
		return workplace_display_url;
	}

	public void setWorkplace_display_url(String displayUrl) {
		this.workplace_display_url = displayUrl;
		logger.config("Set Workplace Display URL to "+this.workplace_display_url);
	}

	public String getIs_public() {
		return is_public;
	}

	public void setIs_public(String isPublic) {
		this.is_public = isPublic;
		logger.config("Set IsPublic to "+this.is_public);
	}

	public String getAdditional_where_clause() {
		return additional_where_clause;
	}

	public void setAdditional_where_clause(String additionalWhereClause) {
		this.additional_where_clause = additionalWhereClause;
		logger.config("Set Additional Where Clause to "+this.additional_where_clause);
	}

	public HashSet getExcluded_meta() {
		return excluded_meta;
	}

	public void setExcluded_meta(HashSet excluded_meta) {
		this.excluded_meta = excluded_meta;
		logger.config("Setting excluded_meta to " + excluded_meta );
	}

	public HashSet getIncluded_meta() {
		return included_meta;
	}

	public void setIncluded_meta(HashSet included_meta) {
		this.included_meta = included_meta;
		logger.config("Setting included_meta to " + included_meta );
	}

	public String getContent_engine_url() {
		return content_engine_url;
	}

	public void setContent_engine_url(String content_engine_url) {
		this.content_engine_url = content_engine_url;
		logger.config("Set Content Engine URL to "+this.content_engine_url);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		logger.config("Set UserName to "+this.username);
	}
	
}
