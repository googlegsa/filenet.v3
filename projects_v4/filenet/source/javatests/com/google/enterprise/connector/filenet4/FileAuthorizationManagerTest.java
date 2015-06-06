// Copyright 2007-2008 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.filenet4.api.UserMock;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileAuthorizationManagerTest {

  @Test
  public void testAuthorizeDocids() throws RepositoryLoginException, RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    Session fs = connec.login();
    AuthorizationManager fam = fs.getAuthorizationManager();

    Map<String, Boolean> expectedResults = new HashMap<String, Boolean>();
    expectedResults.put(TestConnection.docVsId1, Boolean.FALSE);
    expectedResults.put(TestConnection.docVsId2, Boolean.FALSE);
    expectedResults.put(TestConnection.docVsId3, Boolean.TRUE);
    expectedResults.put(TestConnection.docVsId4, Boolean.TRUE);

    testAuthorization(fam, expectedResults, TestConnection.username);
  }

  /**
   * A mock that authorizes any user for all documents. The threads
   * that are used to authorize the documents are tracked by name.
   */
  private static class MockAuthorizationHandler
      implements AuthorizationHandler {
    public final Set<String> threads = Sets.newHashSet();

    @Override public void pushSubject() {}

    @Override public void popSubject() {}

    @Override public boolean hasMarkings() { return false; }

    @Override public User getUser(AuthenticationIdentity identity) {
      String username = identity.getUsername();
      return new UserMock(username, username, username, username,
          ImmutableList.<Group>of());
    }

    @Override
    public AuthorizationResponse authorizeDocid(String docid, User user,
        boolean checkMarkings) throws RepositoryException {
      threads.add(Thread.currentThread().getName());
      return new AuthorizationResponse(true, docid);
    }
  }

  /**
   * Tests that multiple threads are used for the authorization, and
   * that every docid is authorized.
   */
  @Test
  public void testMultipleThreads() throws RepositoryException {
    MockAuthorizationHandler handler = new MockAuthorizationHandler();
    AuthorizationManager fam = new FileAuthorizationManager(handler);

    Map<String, Boolean> expectedResults = new HashMap<String, Boolean>();
    for (int i = 0; i < 100; i++) {
      expectedResults.put(String.valueOf(i), Boolean.TRUE);
    }

    testAuthorization(fam, expectedResults, TestConnection.username);
    assertTrue("Expected multiple threads but got " + handler.threads.size()
        + " with " + Runtime.getRuntime().availableProcessors()
        + " processors.", handler.threads.size() > 1);
  }

  private void testAuthorization(AuthorizationManager fam,
      Map<String, Boolean> expectedResults, String username)
      throws RepositoryException {
    List<String> docids = new LinkedList<String>(expectedResults.keySet());

    Collection<AuthorizationResponse> resultSet = fam.authorizeDocids(docids,
        new SimpleAuthenticationIdentity(username, null));

    for (AuthorizationResponse ar : resultSet) {
      String uuid = ar.getDocid();

      Boolean expected = expectedResults.get(uuid);
      assertEquals(username + " access to " + uuid, expected.booleanValue(),
          ar.isValid());
    }
  }
}
