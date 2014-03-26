// Copyright 2008 Google Inc. All Rights Reserved.
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

  private final ObjectStore objectStore;
  private final IConnection connection;
  private final String login;
  private final String password;

  public FnObjectStore(ObjectStore objectStore, IConnection connection,
      String login, String password) {
    this.objectStore = objectStore;
    this.connection = connection;
    this.login = login;
    this.password = password;
  }

  @Override
  public void refreshSUserContext() throws RepositoryLoginException {
    connection.getUserContext().authenticate(login, password);
  }

  @Override
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

  @Override
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

  @Override
  public String getName() throws RepositoryException {
    try {
      return this.objectStore.get_Name();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unable to get Object Store name");
      throw new RepositoryException(e);
    }
  }

  @Override
  public ObjectStore getObjectStore() {
    return objectStore;
  }

  @Override
  public String getSUserLogin() {
    return login;
  }

  @Override
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
