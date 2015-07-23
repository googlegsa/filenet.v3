// Copyright 2007-2010 Google Inc. All Rights Reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FileAuthenticationManagerTest {

  @Before
  public void checkLiveConnection() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());
  }

  private AuthenticationManager getObjectUnderTest(boolean pushAcls)
      throws RepositoryException {
    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);
    connec.setPushAcls(pushAcls);

    return connec.login().getAuthenticationManager();
  }

  @Test
  public void testAuthenticate() throws RepositoryException {
    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity fai = new SimpleAuthenticationIdentity(
        TestConnection.username, TestConnection.password);
    AuthenticationResponse ar = fatm.authenticate(fai);
    assertEquals(true, ar.isValid());

    @SuppressWarnings("unchecked") List<Principal> groups =
        (List<Principal>) ar.getGroups();
    assertTrue(groups.size() > 1);

    boolean hasAuthUserGrp = false;
    for (Principal group : groups) {
      if (Permissions.AUTHENTICATED_USERS.equals(group.getName())) {
        hasAuthUserGrp = true;
      } else {
        assertTrue("Group: " + group.getName(), group.getName().contains("@"));
      }
    }
    assertTrue("Missing " + Permissions.AUTHENTICATED_USERS + " group",
        hasAuthUserGrp);
  }

  @Test
  public void testAuthenticateNoGroupsIfPushAclsFalse()
      throws RepositoryException {
    AuthenticationManager fatm = getObjectUnderTest(false);
    SimpleAuthenticationIdentity fai = new SimpleAuthenticationIdentity(
        TestConnection.username, TestConnection.password);
    AuthenticationResponse ar = fatm.authenticate(fai);
    assertEquals(true, ar.isValid());
    assertNull(ar.getGroups());
  }

  @Test
  public void testAuthenticate_fail() throws RepositoryException  {
    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity faiWrong = new SimpleAuthenticationIdentity(TestConnection.username, TestConnection.wrongPassword);
    AuthenticationResponse arWrong = fatm.authenticate(faiWrong);
    assertEquals(false, arWrong.isValid());
  }
}
