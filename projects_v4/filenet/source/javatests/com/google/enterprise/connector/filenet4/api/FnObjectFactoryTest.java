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

package com.google.enterprise.connector.filenet4.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.collection.SecurityTemplateList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.security.SecurityPolicy;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class FnObjectFactoryTest {
  FileSession fs;
  IObjectStore ios;
  IConnection conn;
  IObjectFactory iof;

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
    // Domain domain = Factory.Domain.getInstance(conn.getConnection(),
    // "P8.V4");
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.api.FnObjectFactory.getConnection(String)'
   */
  @Test
  public void testGetConnection() throws RepositoryException {
    assertNotNull(conn);
    assertEquals(TestConnection.uri,
        ((FnConnection) conn).getConnection().getURI());
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.api.FnObjectFactory.getObjectStore(String,
   * IConnection, String, String)'
   */
  @Test
  public void testGetObjectStore() throws RepositoryException {
    assertNotNull(ios);
    assertEquals(TestConnection.objectStore,
        ((FnObjectStore) ios).getObjectStore().get_Name());
  }

  /**
   * Tests that get_SecurityTemplates does not return null on a SecurityPolicy.
   */
  @Test
  public void testNullSecurityTemplates() throws RepositoryException {
    SecurityPolicy testPolicy = Factory.SecurityPolicy.createInstance(
        ((FnObjectStore) ios).getObjectStore(), null);
    testPolicy.set_SecurityTemplates(null);
    SecurityTemplateList list = testPolicy.get_SecurityTemplates();
    if (list == null) {
      fail(list.toString());
    }

    // The security policy must be saved to have an ID, and must have
    // a display name to be saved.
    testPolicy.set_DisplayName("testNullSecurityTemplates");
    testPolicy.save(RefreshMode.REFRESH);
    try {
      list = testPolicy.get_SecurityTemplates();
      if (list == null) {
        fail(list.toString());
      }
    } finally {
      testPolicy.delete();
      testPolicy.save(RefreshMode.NO_REFRESH);
    }
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.api.FnObjectFactory.getSearch(IObjectStore)'
   */
  @Test
  public void testGetSearch() throws RepositoryException {
    ISearch is = iof.getSearch(ios);
    IObjectSet test = is.execute(
        "SELECT TOP 50 d.Id, d.DateLastModified FROM Document AS d WHERE d.Id='"
        + TestConnection.docId1 + "' and VersionStatus=1 "
        + "and ContentSize IS NOT NULL  AND (ISCLASS(d, Document) " 
        + "OR ISCLASS(d, WorkflowDefinition))  ORDER BY DateLastModified,Id");
    Iterator<?> it = test.iterator();
    assertTrue(it.hasNext());
    IBaseObject ibo = (IBaseObject) it.next();
    assertEquals("{" + TestConnection.docId1 + "}", ibo.get_Id().toString());
    assertFalse(it.hasNext());
  }

}
