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

package com.google.enterprise.connector.filenet4.filejavawrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

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
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getConnection(String)'
   */
  @Test
  public void testGetConnection() throws RepositoryException {
    assertNotNull(conn);
    assertEquals(TestConnection.uri,
        ((FnConnection) conn).getConnection().getURI());
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getObjectStore(String,
   * IConnection, String, String)'
   */
  @Test
  public void testGetObjectStore() throws RepositoryLoginException,
          RepositoryException {
    assertNotNull(ios);
    assertEquals(TestConnection.objectStore, ios.get_Name());
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getSearch(IObjectStore)'
   */
  @Test
  public void testGetSearch() throws RepositoryException {
    ISearch is = iof.getSearch(ios);
    IObjectSet test = is.execute(
        "SELECT TOP 50 d.Id, d.DateLastModified FROM Document AS d WHERE d.Id='"
        + TestConnection.docId1 + "' and VersionStatus=1 "
        + "and ContentSize IS NOT NULL  AND (ISCLASS(d, Document) " 
        + "OR ISCLASS(d, WorkflowDefinition))  ORDER BY DateLastModified,Id");
    assertEquals(1, test.getSize());
    Iterator<? extends IBaseObject> it = test.getIterator();
    while (it.hasNext()) {
      IBaseObject ibo = it.next();
      assertEquals("{" + TestConnection.docId1 + "}", ibo.get_Id().toString());
    }
  }

}
