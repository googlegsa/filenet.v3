// Copyright (C) 2007-2010 Google Inc.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.isA;

import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileTraversalManagerTest extends FileNetTestCase {
  FileSession fs;
  FileTraversalManager ftm;
  FileConnector connec;
  String checkpoint;

  protected void setUp() throws RepositoryException {
    connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession)connec.login();
    ftm = (FileTraversalManager) fs.getTraversalManager();
  }

  /*
   * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.startTraversal()'
   */
  public void testStartTraversal() throws RepositoryException {

    ftm.setBatchHint(TestConnection.batchSize);
    DocumentList set = this.ftm.startTraversal();
    long counter = 0;
    com.google.enterprise.connector.spi.Document doc = null;
    doc = set.nextDocument();
    while (doc != null) {
      if (counter == 113) {
        checkpoint = set.checkpoint();
        System.out.println(checkpoint);
      }
        doc = set.nextDocument();
        counter++;
    }
    assertEquals(TestConnection.batchSize, counter);
  }

  /*
   * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.resumeTraversal(String)'
   */
  public void testResumeTraversal() throws RepositoryException {

    ftm.setBatchHint(TestConnection.batchSize);
    DocumentList set = this.ftm.resumeTraversal(TestConnection.checkpoint2);
    assertNotNull(set);
    int counter = 0;
    com.google.enterprise.connector.spi.Document doc = null;
    doc = set.nextDocument();
    while (doc != null) {
      doc = set.nextDocument();
      counter++;
    }
    assertEquals(TestConnection.batchSize, counter);

  }

  /*
   * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.setBatchHint(int)'
   */
  public void testSetBatchHint() throws RepositoryException {
    this.ftm.setBatchHint(10);
    DocumentList set = this.ftm.startTraversal();
    int counter = 0;
    while (set.nextDocument() != null) {
      counter++;
    }
    assertEquals(10, counter);
  }

  public void testEmptyObjectStoreMock() throws Exception {
    IObjectStore os = createNiceMock(IObjectStore.class);
    IObjectSet objectSet = createNiceMock(IObjectSet.class);

    IObjectFactory factory = createMock(IObjectFactory.class);
    ISearch search = createMock(ISearch.class);
    expect(factory.getSearch(os)).andReturn(search);
    expect(search.execute(isA(String.class))).andReturn(objectSet).times(2);
    replay(os, factory, search, objectSet);

    FileTraversalManager traversalMgr =
        new FileTraversalManager(factory, os, connec);
    DocumentList docList = traversalMgr.startTraversal();
    assertNull(docList);
    verify(os, factory, search, objectSet);
  }
}
