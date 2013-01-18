package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

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
    IndependentObject obj = null;
    try {
      obj = objectStore.getObject(type, id);
      if (type.equals(ClassNames.VERSION_SERIES)) {
        VersionSeries vs = (VersionSeries) objectStore.getObject(type, id);
        vs.refresh();
        return new FnVersionSeries(vs);
      } else if (type.equals(ClassNames.DOCUMENT)) {
        return new FnDocument((Document) objectStore.getObject(type, id));
      } else {
        return new FnBaseObject(obj);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING,
          "Unable to get VersionSeries or Document object", e);
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
}
