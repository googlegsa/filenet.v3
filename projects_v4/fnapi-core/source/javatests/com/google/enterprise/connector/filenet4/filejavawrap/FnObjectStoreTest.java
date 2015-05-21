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
import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import org.junit.Before;
import org.junit.Test;

public class FnObjectStoreTest {
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
    IConnection conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    // Domain domain = Factory.Domain.getInstance(conn.getConnection(),
    // "P8.V4");
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);
  }

  @Test
  public void testGetObject() throws RepositoryException {
    fd = (IDocument) ios.getObject(ClassNames.DOCUMENT, TestConnection.docId1);
    assertNotNull(fd);
    assertEquals("{" + TestConnection.docId1 + "}", fd.get_Id().toString());
  }

  @Test
  public void testFetchObject() throws RepositoryException {
    fd = (IDocument) ios.fetchObject(ClassNames.DOCUMENT,
        new Id(TestConnection.docId1),
        FileUtil.getDocumentPropertyFilter(TestConnection.included_meta));
    assertNotNull(fd);
    assertEquals("{" + TestConnection.docId1 + "}", fd.get_Id().toString());
  }

  @Test
  public void testGetName() throws RepositoryException {
    assertEquals(TestConnection.objectStore, ios.get_Name());
  }

  @Test
  public void testGetObjectStore() throws RepositoryException {
    assertNotNull(((FnObjectStore) ios).getObjectStore());
  }
}
