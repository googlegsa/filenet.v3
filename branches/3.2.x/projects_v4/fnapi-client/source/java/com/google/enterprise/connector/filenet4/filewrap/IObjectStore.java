package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;

import java.util.Iterator;

public interface IObjectStore {

  public IBaseObject getObject(String type, String id)
      throws RepositoryDocumentException;

  public IBaseObject getObject(String type, IId id)
      throws RepositoryDocumentException;

  public IBaseObject fetchObject(String type, IId id,
      PropertyFilter filter) throws RepositoryDocumentException;

  String get_Name() throws RepositoryException;

  Iterator<PropertyDefinition> getPropertyDefinitions(Id objectId,
      PropertyFilter filter) throws RepositoryException;

  public DatabaseType get_DatabaseType() throws RepositoryException;

  public void refreshSUserContext() throws RepositoryLoginException;

  public String getSUserLogin();

  public String getSUserPassword();
}
