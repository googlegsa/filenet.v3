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

import com.google.enterprise.connector.filenet4.FileAuthenticationIdentity;
import com.google.enterprise.connector.filenet4.FileAuthorizationManager;
import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FileAuthorizationManagerTest extends FileNetTestCase {

  public void testAuthorizeDocids() throws RepositoryLoginException, RepositoryException {

    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    FileSession fs = (FileSession)connec.login();
    FileAuthorizationManager fam = (FileAuthorizationManager) fs.getAuthorizationManager();
//    FileAuthenticationManager fatm = (FileAuthenticationManager) fs.getAuthenticationManager();
//    FileAuthenticationIdentity fai = new FileAuthenticationIdentity(TestConnection.username, TestConnection.password);
//    AuthenticationResponse ar = fatm.authenticate(fai);

    Map<String, Boolean> expectedResults = new HashMap<String, Boolean>();
    expectedResults.put(TestConnection.docVsId1, Boolean.FALSE);
    expectedResults.put(TestConnection.docVsId2, Boolean.FALSE);
    expectedResults.put(TestConnection.docVsId3, Boolean.TRUE);
    expectedResults.put(TestConnection.docVsId4, Boolean.TRUE);

    testAuthorization(fam, expectedResults, TestConnection.username, TestConnection.password);

  }

  private void testAuthorization(FileAuthorizationManager fam,
          Map<String, Boolean> expectedResults, 
          String username, String password) throws RepositoryException {
    List<String> docids = new LinkedList<String>(expectedResults.keySet());

    Collection<AuthorizationResponse> resultSet = fam.authorizeDocids(docids,
        new FileAuthenticationIdentity(username, null));

    for (AuthorizationResponse ar : resultSet) {
      String uuid = ar.getDocid();

      Boolean expected = expectedResults.get(uuid);
      assertEquals(username + " access to " + uuid, expected.booleanValue(),
          ar.isValid());
    }
  }
}
