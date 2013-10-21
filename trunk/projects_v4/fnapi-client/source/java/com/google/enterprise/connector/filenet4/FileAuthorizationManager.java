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
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.admin.DocumentClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.core.Factory;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.util.UserContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

public class FileAuthorizationManager implements AuthorizationManager {
  private static final Logger logger =
      Logger.getLogger(FileAuthorizationManager.class.getName());
  private static final int MAX_RESPONSE_MINS = 1;

  private final IConnection conn;
  private final IObjectStore objectStore;
  private boolean checkMarkings;

  public FileAuthorizationManager(IConnection conn, IObjectStore objectStore,
          boolean checkMarkings) {
    this.conn = conn;
    this.objectStore = objectStore;
    this.checkMarkings = checkMarkings;
  }

  private IUser getUser(AuthenticationIdentity id) {
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

  /**
   * To authorize a given username against the grantee-names, present in all
   * the Access Control Entries for all the permission of the target document.
   *
   * @param (java.util.Collection<String> List of Document IDs to authorize,
   *        com.google.enterprise.connector.spi.AuthenticationIdentity
   *        Search_user_identity)
   * @return Collection of AuthorizationResponse objects(True or
   *         False,DocumentID), depending on the success or failure of
   *         authorization.
   * @see com.google.enterprise.connector.spi.AuthorizationManager#authorizeDocids(java.util.Collection,
   *      com.google.enterprise.connector.spi.AuthenticationIdentity)
   */
  public Collection<AuthorizationResponse> authorizeDocids(
      Collection<String> docids, AuthenticationIdentity identity)
      throws RepositoryException {
    long timeStart = System.currentTimeMillis();
    if (null == docids) {
      logger.severe("Got null docids for authZ .. returning null");
      return null;
    }

    logger.info("Authorizing docids for user: " + identity.getUsername());

    // In some cases current FileNet connection loses UserContext
    // object associated with it; hence need to fetch userContext for
    // each and every AuthZ request.
    // TODO(tdnguyen) Refactor this method to properly handle pushSubject and
    // popSubject
    Subject subject = conn.getSubject();
    UserContext.get().pushSubject(subject);

    IUser user = getUser(identity);
    if (user == null) {
      UserContext.get().popSubject();
      return null;
    }

    // check for the marking sets applied over the document class
    try {
      DocumentClassDefinition documentClassDefinition = Factory.DocumentClassDefinition.fetchInstance(this.objectStore.getObjectStore(), GuidConstants.Class_Document, null);
      PropertyDefinitionList propertyDefinitionList = documentClassDefinition.get_PropertyDefinitions();
      @SuppressWarnings("unchecked")
          Iterator<PropertyDefinition> propertyDefinitionIterator =
          propertyDefinitionList.iterator();
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
    UserContext.get().popSubject();

    ExecutorService threadPool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    // Use a concurrent map to collect responses from multiple threads without
    // synchronization.
    Map<String, AuthorizationResponse> responses =
        new ConcurrentHashMap<String, AuthorizationResponse>(docids.size());

    // Iterate through the DocId list and authorize the search user. Add the
    // authorization result to a map of responses.
    for (String docId : docids) {
      AuthorizationHandler handler = new AuthorizationHandler(conn, objectStore,
          checkMarkings, docId, user, responses);
      threadPool.execute(handler);
    }
    threadPool.shutdown();
    try {
      if (threadPool.awaitTermination(MAX_RESPONSE_MINS, TimeUnit.MINUTES)) {
        threadPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.log(Level.FINEST,
          "Authorization exceeds response threadshold, terminate thread pool",
          e);
    }

    logger.log(Level.FINEST, "Authorization time: {0}ms",
        (System.currentTimeMillis() - timeStart));
    return responses.values();
  }

  private static class AuthorizationHandler implements Runnable {
    private final IConnection conn;
    private final IObjectStore objectStore;
    private final boolean checkMarkings;
    private final String docId;
    private final IUser user;
    private final Map<String, AuthorizationResponse> responses;

    public AuthorizationHandler(IConnection conn, IObjectStore os,
        boolean checkMarkings, String docId, IUser user, Map<String,
        AuthorizationResponse> responses) {
      this.conn = conn;
      this.objectStore = os;
      this.checkMarkings = checkMarkings;
      this.docId = docId;
      this.user = user;
      this.responses = responses;
    }

    private AuthorizationResponse getResponse() throws RepositoryException {
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
                + user.getName());
        // Check whether the search user is authorized to view document
        // contents or
        // not.
        IDocument releasedVersion = versionSeries.getReleasedVersion();
        if (releasedVersion.getPermissions().authorize(user)) {
          logger.log(Level.INFO, "As per the ACLS User "
                  + user.getName()
                  + " is authorized for document DocID " + docId);
          authorizationResponse = new AuthorizationResponse(true,
                  docId);

          if (this.checkMarkings) {
            logger.log(Level.INFO, "Authorizing DocID: " + docId
                    + " for user: " + user.getName()
                    + " for Marking sets ");

            // check whether current document has property values
            // set for properties associated with marking sets or
            // not //
            if (releasedVersion.getActiveMarkings() != null) {
              logger.log(Level.INFO, "Document has property associated with Markings set");

              // check whether USER is authorized to view the
              // document as per the Marking set security applied
              // over it.

              if (releasedVersion.getActiveMarkings().authorize(user)) {
                logger.log(Level.INFO, "As per the Marking Sets User "
                        + user.getName()
                        + " is authorized for document DocID "
                        + docId);
                authorizationResponse = new AuthorizationResponse(
                        true, docId);
              } else {
                logger.log(Level.INFO, "As per the Marking Sets User "
                        + user.getName()
                        + " is NOT authorized for document DocID "
                        + docId);
                authorizationResponse = new AuthorizationResponse(
                        false, docId);
              }

            } else {
              logger.log(Level.INFO, "Document does not have property associated with Marking Sets "
                      + docId);
              logger.log(Level.INFO, "User "
                      + user.getName()
                      + " is authorized for document DocID "
                      + docId);
              authorizationResponse = new AuthorizationResponse(
                      true, docId);
            }
          } else {
            logger.log(Level.INFO, "Either Document class does not have property associated with Marking Sets or Connector is not configured to check Marking Sets ");
            logger.log(Level.INFO, "User " + user.getName()
                    + " is authorized for document DocID " + docId);
            authorizationResponse = new AuthorizationResponse(true,
                    docId);
          }
        } else {
          authorizationResponse = new AuthorizationResponse(false,
                  docId);
          logger.log(Level.INFO, "As per the ACLS User "
                  + user.getName()
                  + " is NOT authorized for document DocID " + docId);
        }
      } else {
        authorizationResponse = new AuthorizationResponse(false, docId);
        logger.log(Level.INFO, "User " + user.getName()
                + " is NOT authorized for document DocID " + docId
                + "version series null");
      }
      return authorizationResponse;
    }

    @Override
    public void run() {
      UserContext.get().pushSubject(conn.getSubject());
      try {
        responses.put(docId, getResponse());
      } catch (RepositoryException e) {
        logger.log(Level.WARNING, "Failed to authorize docid " + docId
            + " for user " + user.getName(), e);
        responses.put(docId, new AuthorizationResponse(false, docId));
      } finally {
        UserContext.get().popSubject();
      }
    }
  }
}
