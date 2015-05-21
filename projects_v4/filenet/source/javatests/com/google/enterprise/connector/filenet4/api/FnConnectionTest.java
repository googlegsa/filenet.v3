// Copyright 2007 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.core.Connection;
import com.filenet.api.util.UserContext;

import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;

public class FnConnectionTest {
  FileSession fs;
  IObjectStore ios;
  IConnection conn;
  UserContext uc;
  IObjectFactory iof;
  IDocument fd;

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());

    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession) connec.login();

    iof = (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
    conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
  }

  @Test
  public void testGetConnection() throws RepositoryException {
    Connection test = ((FnConnection) conn).getConnection();
    assertNotNull(test);
    assertEquals(TestConnection.uri, test.getURI());
  }

  @Test
  public void testGetSubject() throws RepositoryException {
    Subject subject = conn.getSubject();
    assertNotNull(subject);
  }

  @Test
  public void testGetUserContext() throws RepositoryException {
    IUserContext test = conn.getUserContext();
    assertNotNull(test);
    assertEquals(TestConnection.currentUserContext,
        FileUtil.convertDn(test.getName()));
  }
}
