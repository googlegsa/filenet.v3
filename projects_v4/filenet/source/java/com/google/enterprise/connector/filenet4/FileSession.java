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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

public class FileSession implements Session {
  private static final Logger LOGGER =
      Logger.getLogger(FileSession.class.getName());

  private final FileConnector connector;
  private final IObjectFactory fileObjectFactory;
  private final IObjectStore objectStore;
  private final IConnection connection;

  public FileSession(FileConnector fileConnector)
      throws RepositoryLoginException, RepositoryException {
    this.connector = fileConnector;

    LOGGER.info("Initializing new object factory: "
        + connector.getObjectFactory());
    this.fileObjectFactory =
        getFileObjectFactory(connector.getObjectFactory());

    LOGGER.info("Getting connection for content engine: "
        + connector.getContentEngineUrl());
    this.connection =
        fileObjectFactory.getConnection(connector.getContentEngineUrl(),
            connector.getUsername(), connector.getPassword());

    LOGGER.info("Connecting to object store " + connector.getObjectStore()
        + " using account: " + connector.getUsername());
    this.objectStore =
        fileObjectFactory.getObjectStore(connector.getObjectStore(),
            connection, connector.getUsername(), connector.getPassword());
  }

  /**
   * Gets FileNet objectFactory.
   */
  private IObjectFactory getFileObjectFactory(String objectFactoryName)
      throws RepositoryException {
    try {
      return (IObjectFactory) Class.forName(objectFactoryName).newInstance();
    } catch (InstantiationException e) {
      throw new RepositoryException("Unable to instantiate object factory: "
          + objectFactoryName, e);
    } catch (IllegalAccessException e) {
      throw new RepositoryException("Access denied to object factory class: "
          + objectFactoryName, e);
    } catch (ClassNotFoundException e) {
      throw new RepositoryException("Class not found: " + objectFactoryName, e);
    }
  }

  @VisibleForTesting
  Traverser getFileDocumentTraverser() {
    return new FileDocumentTraverser(fileObjectFactory, objectStore, connector);
  }

  @VisibleForTesting
  Traverser getSecurityPolicyTraverser() {
    return new SecurityPolicyTraverser(fileObjectFactory, objectStore,
            connector);
  }

  @VisibleForTesting
  Traverser getSecurityFolderTraverser() {
    return new SecurityFolderTraverser(fileObjectFactory, objectStore,
        connector);
  }

  /**
   * To return the TraversalManager class object
   */
  @Override
  public TraversalManager getTraversalManager() throws RepositoryException {
    return new FileTraversalManager(getFileDocumentTraverser(),
        getSecurityPolicyTraverser(), getSecurityFolderTraverser());
  }

  /**
   * To return the AuthenticationManager class object.
   */
  @Override
  public AuthenticationManager getAuthenticationManager()
          throws RepositoryException {
    return new FileAuthenticationManager(connection,
        connector.getGoogleGlobalNamespace());
  }

  /**
   * To returns the AuthorizationManager class object.
   */
  @Override
  public AuthorizationManager getAuthorizationManager()
          throws RepositoryException {
    return new FileAuthorizationManager(
        new FileAuthorizationHandler(connection, fileObjectFactory, objectStore,
            connector.checkMarking()));
  }

  /**
   * To return the Search object for searching.
   *
   * @return
   * @throws RepositoryException
   */
  public ISearch getSearch() throws RepositoryException {
    return fileObjectFactory.getSearch(objectStore);
  }
}
