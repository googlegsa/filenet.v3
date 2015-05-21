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

import com.google.enterprise.connector.filenet4.api.IUser;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuthorizationManager implements AuthorizationManager {
  private static final Logger logger =
      Logger.getLogger(FileAuthorizationManager.class.getName());
  private static final int MAX_RESPONSE_MINS = 1;
  private static final int AVG_DOCS_PER_THREAD = 16;
  private static final int AVAILABLE_PROCESSORS =
      Runtime.getRuntime().availableProcessors();

  private final AuthorizationHandler handler;

  public FileAuthorizationManager(AuthorizationHandler handler) {
    this.handler = handler;
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
    handler.pushSubject();

    IUser user = handler.getUser(identity);
    if (user == null) {
      handler.popSubject();
      return null;
    }

    boolean checkMarkings = handler.hasMarkings();
    handler.popSubject();

    // Compute thread pool size
    int poolSize = docids.size() / AVG_DOCS_PER_THREAD;
    if (poolSize > AVAILABLE_PROCESSORS) {
      poolSize = AVAILABLE_PROCESSORS;
    } else if (poolSize == 0) {
      poolSize = 1;
    }
    ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);

    // Use a concurrent map to collect responses from multiple threads without
    // synchronization.
    Map<String, AuthorizationResponse> responses =
        new ConcurrentHashMap<String, AuthorizationResponse>(docids.size());

    // Iterate through the DocId list and authorize the search user. Add the
    // authorization result to a map of responses.
    Iterator<String> iterator = docids.iterator();
    for (int i = 0; i < poolSize; i++) {
      AuthorizationTask task = new AuthorizationTask(handler,
          checkMarkings, iterator, user, responses);
      threadPool.execute(task);
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

    logger.log(Level.FINEST, "Authorization: {0} documents, {1} threads, {2}ms",
        new Object[] {docids.size(), poolSize,
        (System.currentTimeMillis() - timeStart)});
    return responses.values();
  }

  private static class AuthorizationTask implements Runnable {
    private final AuthorizationHandler handler;
    private final boolean checkMarkings;
    // Iterator instance is shared among worker threads.
    private final Iterator<String> iterator;
    private final IUser user;
    private final Map<String, AuthorizationResponse> responses;

    public AuthorizationTask(AuthorizationHandler handler,
        boolean checkMarkings, Iterator<String> docidsIterator, IUser user,
        Map<String, AuthorizationResponse> responses) {
      this.handler = handler;
      this.checkMarkings = checkMarkings;
      this.iterator = docidsIterator;
      this.user = user;
      this.responses = responses;
    }

    private AuthorizationResponse getResponse(String docId)
        throws RepositoryException {
      return handler.authorizeDocid(docId, user, checkMarkings);
    }

    private String pollIterator() {
      synchronized (iterator) {
        return iterator.hasNext() ? iterator.next() : null;
      }
    }

    @Override
    public void run() {
      handler.pushSubject();
      try {
        String docId;
        while ((docId = pollIterator()) != null) {
          try {
            responses.put(docId, getResponse(docId));
          } catch (RepositoryException e) {
            logger.log(Level.WARNING, "Failed to authorize docid " + docId
                + " for user " + user.get_Name(), e);
            responses.put(docId, new AuthorizationResponse(false, docId));
          }
        }
      } finally {
        handler.popSubject();
      }
    }
  }
}
