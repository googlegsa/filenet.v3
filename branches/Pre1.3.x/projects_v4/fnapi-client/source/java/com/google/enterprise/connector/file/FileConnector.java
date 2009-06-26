package com.google.enterprise.connector.file;

import java.net.URL;
import java.util.HashSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileConnector implements Connector {

	private String object_factory;

	private String login;

	private String password;

	private String object_store;

	private String workplace_display_url;

	private String content_engine_uri;

	private String is_public = "false";

	private String additional_where_clause;

	private HashSet included_meta;

	private HashSet excluded_meta;
	
	public Session login() throws RepositoryLoginException, RepositoryException {

		
		URL conf = FileConnector.class.getResource("/jaas.conf");
		if (conf!=null)
			System.setProperty("java.security.auth.login.config", conf.getPath());	
		
		HostnameVerifier aa = new FileHNV();
		
		HttpsURLConnection.setDefaultHostnameVerifier(aa);
		
			
		
		Session sess = null;
		if (!(object_factory == null || login == null || password == null
				|| object_store == null || workplace_display_url == null || content_engine_uri == null)) {

			sess = new FileSession(object_factory, login, password,
					object_store, workplace_display_url, content_engine_uri,
					is_public.equals("on"), additional_where_clause,
					included_meta, excluded_meta);
		}
		return sess;

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

	public String getObject_factory() {
		return object_factory;
	}

	public void setObject_factory(String objectFactory) {
		this.object_factory = objectFactory;
	}

	public String getObject_store() {
		return object_store;
	}

	public void setObject_store(String objectStoreName) {
		this.object_store = objectStoreName;
	}

	public String getWorkplace_display_url() {
		return workplace_display_url;
	}

	public void setWorkplace_display_url(String displayUrl) {
		this.workplace_display_url = displayUrl;
	}

	public String getIs_public() {
		return is_public;
	}

	public void setIs_public(String isPublic) {
		this.is_public = isPublic;
	}

	public String getAdditional_where_clause() {
		return additional_where_clause;
	}

	public void setAdditional_where_clause(String additionalWhereClause) {
		this.additional_where_clause = additionalWhereClause;
	}

	public HashSet getExcluded_meta() {
		return excluded_meta;
	}

	public void setExcluded_meta(HashSet excluded_meta) {
		this.excluded_meta = excluded_meta;
	}

	public HashSet getIncluded_meta() {
		return included_meta;
	}

	public void setIncluded_meta(HashSet included_meta) {
		this.included_meta = included_meta;
	}

	public String getContent_engine_uri() {
		return content_engine_uri;
	}

	public void setContent_engine_uri(String content_engine_uri) {
		this.content_engine_uri = content_engine_uri;
	}
	
}
