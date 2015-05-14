// Copyright 2007-2010 Google Inc.
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

import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.query.SearchScope;
import com.filenet.api.security.SecurityPolicy;
import com.filenet.api.util.UserContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

/**
 * Class to set FileNet objectFactory and perform operations with objectFactory
 * like getConnection associated with objectFactory, get specific objectStore
 * object.
 */
public class FnObjectFactory implements IObjectFactory {
  private static final Logger logger =
      Logger.getLogger(FnObjectFactory.class.getName());

  public FnObjectFactory() {
  }

  @Override
  public IConnection getConnection(String contentEngineUri, String userName,
          String userPassword)
          throws RepositoryException {
    return new FnConnection(contentEngineUri, userName, userPassword);
  }

  @Override
  public IObjectStore getObjectStore(String objectStoreName,
          IConnection conn, String userName, String userPassword)
          throws RepositoryException, RepositoryLoginException {
    ObjectStore os = null;
    try {
      os = getRawObjectStore(userName, userPassword,
          ((FnConnection) conn).getConnection(), objectStoreName);
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Unable to connect to the Object Store with user: "
              + userName, e);
      String shortName = FileUtil.getShortName(userName);
      logger.log(Level.INFO, "Trying to connect Object Store with user: "
              + shortName + " in short name format.");
      try {
        os = getRawObjectStore(shortName, userPassword,
            ((FnConnection) conn).getConnection(), objectStoreName);
      } catch (Throwable th) {
        logger.log(Level.SEVERE, "Problems while connecting to FileNet object store. Got Exception: ", th);
        throw new RepositoryLoginException(e);
      }
    }
    return new FnObjectStore(os, conn, userName, userPassword);
  }

  @Override
  public ISearch getSearch(IObjectStore objectStore)
          throws RepositoryException {
    SearchScope search =
        new SearchScope(((FnObjectStore) objectStore).getObjectStore());

    return new FnSearch(search, getFactory(null));
  }

  private ObjectStore getRawObjectStore(String userName, String userPassword, 
          Connection conn, String objectStoreName) throws RepositoryException {
    logger.info("Creating the subject for user: " + userName);
    Subject s = UserContext.createSubject(conn, userName, userPassword, 
        "FileNetP8");
    UserContext.get().pushSubject(s);
    Domain domain = Factory.Domain.fetchInstance(conn, null, null);
    logger.log(Level.INFO, "Fetch domain: {0}", domain);
    logger.info("Creating the FileNet object store instance.");
    ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectStoreName,
        null);
    os.refresh();
    logger.config("Connection to FileNet ObjectStore is successful...");
    return os;
  }

  @Override
  public IBaseObjectFactory getFactory(String type) {
    // TODO (tdnguyen): Refactor old codes to use new factories to create
    // objects.
    if (ClassNames.DOCUMENT.equals(type)) {
      return new IBaseObjectFactory() {
        @Override public IBaseObject createObject(Object object)
            throws RepositoryException {
          return new FnDocument((Document) object);
        }
      };
    } else if (ClassNames.FOLDER.equals(type)) {
      return new IBaseObjectFactory() {
        @Override public IBaseObject createObject(Object object)
            throws RepositoryException {
          return new FnFolder((Folder) object);
        }
      };
    } else if (ClassNames.SECURITY_POLICY.equals(type)) {
      return new IBaseObjectFactory() {
        @Override public IBaseObject createObject(Object object)
            throws RepositoryException {
          return new FnSecurityPolicy((SecurityPolicy) object);
        }
      };
    } else if (ClassNames.VERSION_SERIES.equals(type)) {
      return new IBaseObjectFactory() {
        @Override public IBaseObject createObject(Object object)
            throws RepositoryException {
          return new FnVersionSeries((VersionSeries) object);
        }
      };
    } else {
      return new IBaseObjectFactory() {
        @Override public IBaseObject createObject(Object object)
            throws RepositoryException {
          return new FnBaseObject((IndependentObject) object);
        }
      };
    }
  }
}
