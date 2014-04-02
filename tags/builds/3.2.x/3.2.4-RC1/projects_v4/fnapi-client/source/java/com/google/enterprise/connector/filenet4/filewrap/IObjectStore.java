package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.PropertyFilter;

public interface IObjectStore {

  public IBaseObject getObject(String type, String id)
      throws RepositoryDocumentException;

  public IBaseObject fetchObject(String type, String id,
      PropertyFilter filter) throws RepositoryDocumentException;

  // TODO(tdnguyen): Rename method getName to get_Name
  public String getName() throws RepositoryException;

  // TODO(tdnguyen): Remove the getObjectStore method.
  public ObjectStore getObjectStore() throws RepositoryException;

  public DatabaseType get_DatabaseType() throws RepositoryException;

  public void refreshSUserContext() throws RepositoryLoginException;

  public String getSUserLogin();

  public String getSUserPassword();
}
