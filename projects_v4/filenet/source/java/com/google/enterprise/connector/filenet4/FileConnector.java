// Copyright 2007 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class FileConnector implements Connector {
  private static final Logger LOGGER =
      Logger.getLogger(FileConnector.class.getName());

  private String object_factory;
  private String username;
  private String password;
  private String object_store;
  private String workplace_display_url;
  private String content_engine_url;
  private String is_public = "false";
  private String check_marking = "off";
  private String useIDForChangeDetection = "false";
  private boolean pushAcls = true;
  private String additional_where_clause;
  private String delete_additional_where_clause = "";
  private Set<String> included_meta;
  private Set<String> excluded_meta;
  private String globalNamespace;
  private String displayUrl;

  // The db_timezone property is deprecated; however, its setter remains here
  // for backward compatibility.

  public void setDb_timezone(String dbTimezone) {
    LOGGER.warning("Deprecated db_timezone property, set to " + dbTimezone +
        ", will be ignored");
  }

  @Override
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

    HostnameVerifier aa = new HostnameVerifier() {
        @Override public boolean verify(String arg0, SSLSession arg1) {
          return true; } };
    HttpsURLConnection.setDefaultHostnameVerifier(aa);

    if (!(object_factory == null || username == null || password == null
            || object_store == null || workplace_display_url == null || content_engine_url == null)) {
      this.displayUrl = getDisplayUrl(workplace_display_url);
      LOGGER.info("Creating fileSession object...");
      return new FileSession(this);
    }
    return null;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
    LOGGER.config("Set Password");
  }

  public String getObjectFactory() {
    return object_factory;
  }

  public void setObject_factory(String objectFactory) {
    this.object_factory = objectFactory;
    LOGGER.config("Set Object Factory to " + this.object_factory);
  }

  public String getObjectStore() {
    return object_store;
  }

  public void setObject_store(String objectStoreName) {
    this.object_store = objectStoreName;
    LOGGER.config("Set Object Store to " + this.object_store);
  }

  public void setWorkplace_display_url(String displayUrl) {
    this.workplace_display_url = displayUrl;
    LOGGER.config("Set Workplace Display URL to " + workplace_display_url);
  }

  private String getDisplayUrl(String url) {
    String workplaceUrl = url;
    if (workplaceUrl.endsWith("/getContent/")) {
      workplaceUrl = workplaceUrl.substring(0, workplaceUrl.length() - 1);
    }
    if (workplaceUrl.contains("/getContent")
            && workplaceUrl.endsWith("/getContent")) {
      return workplaceUrl + "?objectStoreName=" + object_store
              + "&objectType=document&versionStatus=1&vsId=";
    } else {
      return workplaceUrl + "/getContent?objectStoreName="
              + object_store
              + "&objectType=document&versionStatus=1&vsId=";
    }
  }

  public String getWorkplaceDisplayUrl() {
    return displayUrl;
  }

  public void setIs_public(String isPublic) {
    this.is_public = isPublic;
    LOGGER.config("Set IsPublic to " + this.is_public);
  }

  public boolean isPublic() {
    return "true".equalsIgnoreCase(is_public);
  }

  public void setCheck_marking(String checkMarking) {
    this.check_marking = checkMarking;
    LOGGER.config("Set CheckMarking to " + this.check_marking);
  }

  public boolean checkMarking() {
    return "on".equalsIgnoreCase(check_marking);
  }

  public String getAdditionalWhereClause() {
    return additional_where_clause;
  }

  public void setAdditional_where_clause(String additionalWhereClause) {
    this.additional_where_clause = additionalWhereClause;
    LOGGER.config("Set Additional Where Clause to "
            + this.additional_where_clause);
  }

  public String getDeleteAdditionalWhereClause() {
    return delete_additional_where_clause;
  }

  public void setDelete_additional_where_clause(
          String deleteadditionalWhereClause) {
    this.delete_additional_where_clause = deleteadditionalWhereClause;
    LOGGER.config("Set Additional DELETE Clause to "
            + this.delete_additional_where_clause);
  }

  public String getGoogleGlobalNamespace() {
    return globalNamespace;
  }

  public void setGoogleGlobalNamespace(String globalNamespace) {
    this.globalNamespace = globalNamespace;
  }

  public Set<String> getExcludedMeta() {
    if (excluded_meta == null) {
      return ImmutableSet.of();
    } else {
      return excluded_meta;
    }
  }

  public void setExcluded_meta(Set<String> excluded_meta) {
    this.excluded_meta = excluded_meta;
    LOGGER.config("Setting excluded_meta to " + excluded_meta);
  }

  public Set<String> getIncludedMeta() {
    if (included_meta == null) {
      return ImmutableSet.of();
    } else {
      return included_meta;
    }
  }

  public void setIncluded_meta(Set<String> included_meta) {
    this.included_meta = included_meta;
    LOGGER.config("Setting included_meta to " + included_meta);
  }

  public String getContentEngineUrl() {
    return content_engine_url;
  }

  public void setContent_engine_url(String content_engine_url) {
    this.content_engine_url = content_engine_url;
    LOGGER.config("Set Content Engine URL to " + this.content_engine_url);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
    LOGGER.config("Set UserName to " + this.username);
  }

  public void setUseIDForChangeDetection(String useIDForChangeDetection) {
    this.useIDForChangeDetection = useIDForChangeDetection;
    LOGGER.config("Set useIDForChangeDetection to "
            + this.useIDForChangeDetection);
  }

  public boolean useIDForChangeDetection() {
    return "true".equalsIgnoreCase(useIDForChangeDetection);
  }

  public void setPushAcls(boolean pushAcls) {
    LOGGER.config("Set pushAcls to " + pushAcls);
    this.pushAcls = pushAcls;
  }

  public boolean pushAcls() {
    return pushAcls;
  }
}
