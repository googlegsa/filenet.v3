package com.google.enterprise.connector.filenet4.filejavawrap;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.property.PropertyFilter;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FnObjectStore implements IObjectStore {
  private static final Logger logger =
      Logger.getLogger(FnDocument.class.getName());

  private ObjectStore objectStore;
  private IConnection connection;
  private String login;
  private String password;

  public FnObjectStore(ObjectStore objectStore, IConnection connection,
      String login, String password) {
    this.objectStore = objectStore;
    this.connection = connection;
    this.login = login;
    this.password = password;
  }

  public void refreshSUserContext() throws RepositoryLoginException {
    connection.getUserContext().authenticate(login, password);
  }

  public IBaseObject getObject(String type, String id)
      throws RepositoryDocumentException {
    try {
      IndependentObject obj = objectStore.getObject(type, id);
      if (type.equals(ClassNames.VERSION_SERIES)) {
        VersionSeries vs = (VersionSeries) obj;
        vs.refresh();
        return new FnVersionSeries(vs);
      } else if (type.equals(ClassNames.DOCUMENT)) {
        return new FnDocument((Document) obj);
      } else {
        return new FnBaseObject(obj);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING,
          "Unable to get VersionSeries or Document object", e);
      throw new RepositoryDocumentException(e);
    }
  }

  public IBaseObject fetchObject(String type, String id, PropertyFilter filter)
          throws RepositoryDocumentException {
    IndependentObject obj = null;
    try {
      obj = objectStore.fetchObject(type, id, filter);
      if (type.equals(ClassNames.VERSION_SERIES)) {
        return new FnVersionSeries((VersionSeries) obj);
      } else if (type.equals(ClassNames.DOCUMENT)) {
        return new FnDocument((Document) obj);
      } else {
        return new FnBaseObject(obj);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING,
          "Unable to fetch VersionSeries or Document object", e);
      throw new RepositoryDocumentException(e);
    }
  }

  public String getName() throws RepositoryException {
    try {
      return this.objectStore.get_Name();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unable to get Object Store name");
      throw new RepositoryException(e);
    }
  }

  public ObjectStore getObjectStore() {
    return objectStore;
  }

  public String getSUserLogin() {
    return login;
  }

  public String getSUserPassword() {
    return password;
  }

  @Override
  public DatabaseType get_DatabaseType() throws RepositoryException {
    try {
      return objectStore.get_DatabaseType();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unable to get database type", e);
      throw new RepositoryDocumentException(e);
    }
  }
}
