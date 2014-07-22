// Copyright 2007-2008 Google Inc. All Rights Reserved.
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
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IUser;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.util.UserContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuthorizationHandler implements AuthorizationHandler {
  private static final Logger logger =
      Logger.getLogger(FileAuthorizationHandler.class.getName());

  private final IConnection conn;
  private final IObjectStore objectStore;
  private boolean checkMarkings;

  public FileAuthorizationHandler(IConnection conn, IObjectStore objectStore,
          boolean checkMarkings) {
    this.conn = conn;
    this.objectStore = objectStore;
    this.checkMarkings = checkMarkings;
  }

  @Override
  public void pushSubject() {
    UserContext.get().pushSubject(conn.getSubject());
  }

  @Override
  public void popSubject() {
    UserContext.get().popSubject();
  }

  @Override
  public IUser getUser(AuthenticationIdentity id) {
    // Lookup FileNet user and user's groups
    IUserContext uc = conn.getUserContext();
    String username = FileUtil.getUserName(id);
    try {
      return uc.lookupUser(username);
    } catch (RepositoryException e) {
      logger.log(Level.WARNING, "Failed to lookup user [" + username
          + "] in FileNet", e);
      return null;
    }
  }

  @Override
  public boolean hasMarkings() {
    // check for the marking sets applied over the document class
    try {
      Iterator<PropertyDefinition> propertyDefinitionIterator =
          objectStore.getPropertyDefinitions(GuidConstants.Class_Document,
              null);
      boolean hasMarkings = false;

      while (propertyDefinitionIterator.hasNext()) {
        PropertyDefinition propertyDefinition = propertyDefinitionIterator.next();

        if (propertyDefinition instanceof PropertyDefinitionString) {
          MarkingSet markingSet = ((PropertyDefinitionString) propertyDefinition).get_MarkingSet();
          if (markingSet != null) {
            logger.log(Level.INFO, "Document class has property associated with Markings set");
            hasMarkings = true;
            break;
          }
        }
      }
      if (hasMarkings == true) {
        if (this.checkMarkings == true) {
          logger.log(Level.INFO, "Connector is configured to perform marking set's check for authorization");
        } else {
          logger.log(Level.INFO, "Connector is not configured to perform marking set's check for authorization");
        }
      } else {
        logger.log(Level.INFO, "Document class does not have properties associated with Markings set hence; Marking set's check is  not required for authorization");
        this.checkMarkings = false;
      }
    } catch (Exception ecp) {
      logger.log(Level.SEVERE, ecp.getStackTrace().toString());
    }
    return this.checkMarkings;
  }

  @Override
  public AuthorizationResponse authorizeDocid(String docId, IUser user,
      boolean checkMarkings) throws RepositoryException {
    AuthorizationResponse authorizationResponse = null;
    IVersionSeries versionSeries = null;
    try {
      logger.config("Getting version series for document DocID: "
          + docId);
      versionSeries = (IVersionSeries) objectStore.getObject(ClassNames.VERSION_SERIES, URLDecoder.decode(docId, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.WARNING, "Unable to Decode: Encoding is not supported for the document with DocID: "
          + docId);
      versionSeries = null;
    } catch (RepositoryException e) {
      logger.log(Level.WARNING, "Error : document Version Series Id "
          + docId + " may no longer exist. Message: "
          + e.getLocalizedMessage());
      versionSeries = null;
    }

    if (versionSeries != null) {
      logger.config("Authorizing DocID: " + docId + " for user: "
          + user.get_Name());
      // Check whether the search user is authorized to view document
      // contents or
      // not.
      IDocument releasedVersion = versionSeries.get_ReleasedVersion();
      Permissions permissions = new Permissions(
          releasedVersion.get_Permissions(), releasedVersion.get_Owner());
      if (permissions.authorize(user)) {
        logger.log(Level.INFO, "As per the ACLS User "
            + user.get_Name()
            + " is authorized for document DocID " + docId);
        authorizationResponse = new AuthorizationResponse(true,
            docId);

        if (checkMarkings) {
          logger.log(Level.INFO, "Authorizing DocID: " + docId
              + " for user: " + user.get_Name()
              + " for Marking sets ");

          // check whether current document has property values
          // set for properties associated with marking sets or
          // not //
          if (releasedVersion.get_ActiveMarkings() != null) {
            logger.log(Level.INFO, "Document has property associated with Markings set");

            // check whether USER is authorized to view the
            // document as per the Marking set security applied
            // over it.
            MarkingPermissions markingPermissions =
                new MarkingPermissions(releasedVersion.get_ActiveMarkings());
            if (markingPermissions.authorize(user)) {
              logger.log(Level.INFO, "As per the Marking Sets User "
                  + user.get_Name()
                  + " is authorized for document DocID "
                  + docId);
              authorizationResponse = new AuthorizationResponse(
                  true, docId);
            } else {
              logger.log(Level.INFO, "As per the Marking Sets User "
                  + user.get_Name()
                  + " is NOT authorized for document DocID "
                  + docId);
              authorizationResponse = new AuthorizationResponse(
                  false, docId);
            }

          } else {
            logger.log(Level.INFO, "Document does not have property associated with Marking Sets "
                + docId);
            logger.log(Level.INFO, "User "
                + user.get_Name()
                + " is authorized for document DocID "
                + docId);
            authorizationResponse = new AuthorizationResponse(
                true, docId);
          }
        } else {
          logger.log(Level.INFO, "Either Document class does not have property associated with Marking Sets or Connector is not configured to check Marking Sets ");
          logger.log(Level.INFO, "User " + user.get_Name()
              + " is authorized for document DocID " + docId);
          authorizationResponse = new AuthorizationResponse(true,
              docId);
        }
      } else {
        authorizationResponse = new AuthorizationResponse(false,
            docId);
        logger.log(Level.INFO, "As per the ACLS User "
            + user.get_Name()
            + " is NOT authorized for document DocID " + docId);
      }
    } else {
      authorizationResponse = new AuthorizationResponse(false, docId);
      logger.log(Level.INFO, "User " + user.get_Name()
          + " is NOT authorized for document DocID " + docId
          + "version series null");
    }
    return authorizationResponse;
  }
}
