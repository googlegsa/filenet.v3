// Copyright 2009 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSession implements Session {
  private static Logger LOGGER = Logger.getLogger(FileSession.class.getName());

  private IObjectFactory fileObjectFactory;
  private IObjectStore objectStore;
  private IConnection connection;
  private String displayUrl;
  private boolean isPublic;
  private boolean checkMarking;
  private boolean useIDForChangeDetection;
  private String additionalWhereClause;
  private String deleteadditionalWhereClause;
  private Set<String> included_meta;
  private Set<String> excluded_meta;
  private final String globalNamespace;

  public FileSession(String iObjectFactory, String userName,
      String userPassword, String objectStoreName, String displayUrl,
      String contentEngineUri, boolean isPublic, boolean checkMarking,
      boolean useIDForChangeDetection, String additionalWhereClause,
      String deleteadditionalWhereClause, Set<String> included_meta,
      Set<String> excluded_meta, String globalNamespace)
      throws RepositoryException, RepositoryLoginException {

    setFileObjectFactory(iObjectFactory);

    LOGGER.info("Getting connection for content engine: "
            + contentEngineUri);
    connection = fileObjectFactory.getConnection(contentEngineUri, userName, userPassword);

    LOGGER.info("Trying to access object store: " + objectStoreName
            + " for user: " + userName);
    objectStore = fileObjectFactory.getObjectStore(objectStoreName, connection, userName, userPassword);

    this.displayUrl = getDisplayURL(displayUrl, objectStoreName);
    this.isPublic = isPublic;
    this.useIDForChangeDetection = useIDForChangeDetection;
    this.checkMarking = checkMarking;
    this.additionalWhereClause = additionalWhereClause;
    this.deleteadditionalWhereClause = deleteadditionalWhereClause;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.globalNamespace = globalNamespace;
  }

  /**
   * To return display url associated with the specific document
   *
   * @param displayUrl
   * @param objectStoreName
   */
  private String getDisplayURL(String displayUrl, String objectStoreName) {
    if (displayUrl.endsWith("/getContent/")) {
      displayUrl = displayUrl.substring(0, displayUrl.length() - 1);
    }
    if (displayUrl.contains("/getContent")
            && displayUrl.endsWith("/getContent")) {
      return displayUrl + "?objectStoreName=" + objectStoreName
              + "&objectType=document&versionStatus=1&vsId=";
    } else {
      return displayUrl + "/getContent?objectStoreName="
              + objectStoreName
              + "&objectType=document&versionStatus=1&vsId=";
    }
  }

  /**
   * To set FileNet objectFactory
   *
   * @param objectFactory
   * @throws RepositoryException
   */
  private void setFileObjectFactory(String objectFactory)
          throws RepositoryException {

    try {
      fileObjectFactory = (IObjectFactory) Class.forName(objectFactory).newInstance();
    } catch (InstantiationException e) {
      LOGGER.log(Level.WARNING, "Unable to instantiate the class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ");
      throw new RepositoryException(
              "Unable to instantiate the class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ",
              e);
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.WARNING, "Access denied to class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ");
      throw new RepositoryException(
              "Access denied to class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory ",
              e);
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.WARNING, "The class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory not found");
      throw new RepositoryException(
              "The class com.google.enterprise.connector.file.filejavawrap.FnObjectFactory not found",
              e);
    }

  }

  /**
   * To return the TraversalManager class object
   */
  public TraversalManager getTraversalManager() throws RepositoryException {
    // logger.info("getTraversalManager");
    FileTraversalManager fileQTM = new FileTraversalManager(
            fileObjectFactory, objectStore, this.isPublic,
            this.useIDForChangeDetection, this.displayUrl,
            this.additionalWhereClause, this.deleteadditionalWhereClause,
            this.included_meta, this.excluded_meta, globalNamespace);
    return fileQTM;
  }

  /**
   * To return the AuthenticationManager class object.
   */
  public AuthenticationManager getAuthenticationManager()
          throws RepositoryException {
    FileAuthenticationManager fileAm = new FileAuthenticationManager(
        connection, globalNamespace);
    return fileAm;
  }

  /**
   * To returns the AuthorizationManager class object.
   */
  public AuthorizationManager getAuthorizationManager()
          throws RepositoryException {

    FileAuthorizationManager fileAzm = new FileAuthorizationManager(
            connection, objectStore, checkMarking);
    return fileAzm;
  }

  /**
   * To return the Search object for searching.
   *
   * @return
   * @throws RepositoryException
   */

  public ISearch getSearch() throws RepositoryException {
    ISearch search = fileObjectFactory.getSearch(objectStore);
    return search;
  }

}
