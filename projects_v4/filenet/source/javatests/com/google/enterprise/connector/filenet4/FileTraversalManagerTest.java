// Copyright 2014 Google Inc. All Rights Reserved.
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

import static com.google.enterprise.connector.filenet4.Mocks.mockDocument;
import static com.google.enterprise.connector.filenet4.Mocks.mockTraverser;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.util.EmptyDocumentList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FileTraversalManagerTest {
  private static final DocumentList EMPTY_LIST = new EmptyDocumentList("");

  @Test
  public void testSetTraversalContext() throws RepositoryException {
    SimpleTraversalContext traversalContext = new SimpleTraversalContext();
    Traverser traverser = createMock(Traverser.class);
    traverser.setTraversalContext(traversalContext);
    replay(traverser);

    FileTraversalManager traversalMgr = new FileTraversalManager(traverser);
    traversalMgr.setTraversalContext(traversalContext);
    verify(traverser);
  }

  @Test
  public void testSetBatchHint() throws RepositoryException {
    Traverser traverser = createMock(Traverser.class);
    traverser.setBatchHint(42);
    replay(traverser);

    TraversalManager traversalMgr = new FileTraversalManager(traverser);
    traversalMgr.setBatchHint(42);
    verify(traverser);
  }

  @Test
  public void testStartTraversal() throws RepositoryException {
    Traverser traverser = createMock(Traverser.class);
    expect(traverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(EMPTY_LIST);
    replay(traverser);

    TraversalManager traversalMgr = new FileTraversalManager(traverser);
    DocumentList docList = traversalMgr.startTraversal();
    assertEquals(null, docList.nextDocument());
    verify(traverser);
  }

  @Test
  public void testResumeTraversal() throws RepositoryException {
    Traverser traverser = createMock(Traverser.class);
    expect(traverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(EMPTY_LIST);
    replay(traverser);

    TraversalManager traversalMgr = new FileTraversalManager(traverser);

    // The checkpoint must be a valid JSON string.
    DocumentList docList = traversalMgr.resumeTraversal("{}");
    assertEquals(null, docList.nextDocument());
    verify(traverser);
  }

  @Test
  public void testResumeTraversal_allNulls() throws RepositoryException {
    Traverser traverser = createMock(Traverser.class);
    expect(traverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(null).anyTimes();
    replay(traverser);

    TraversalManager traversalMgr =
        new FileTraversalManager(traverser, traverser);

    // The checkpoint must be a valid JSON string.
    DocumentList docList = traversalMgr.resumeTraversal("{}");
    assertEquals(null, docList);
    verify(traverser);
  }

  @Test
  public void testResumeTraversal_skipNulls() throws RepositoryException {
    Traverser nullTraverser = createMock(Traverser.class);
    expect(nullTraverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(null);
    Traverser emptyTraverser = createMock(Traverser.class);
    expect(emptyTraverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(EMPTY_LIST);
    replay(nullTraverser, emptyTraverser);

    TraversalManager traversalMgr =
        new FileTraversalManager(nullTraverser, emptyTraverser);

    // The checkpoint must be a valid JSON string.
    DocumentList docList = traversalMgr.resumeTraversal("{}");
    assertEquals(null, docList.nextDocument());
    verify(nullTraverser, emptyTraverser);
  }

  @Test
  public void testResumeTraversal_nonEmpty() throws RepositoryException {
    int traverserCount = 3;
    int documentCount = 10;
    int batchHint = 5;

    Traverser[] traversers = new Traverser[traverserCount];
    for (int i = 0; i < traversers.length; i++) {
      List<Document> documents = new ArrayList<>();
      for (int j = 0; j < documentCount; j++) {
        documents.add(mockDocument());
      }
      traversers[i] = mockTraverser(documents, "list " + i);
    }

    TraversalManager traversalMgr = new FileTraversalManager(traversers);
    traversalMgr.setBatchHint(batchHint);

    // The checkpoint must be a valid JSON string.
    DocumentList docList = traversalMgr.resumeTraversal("{}");
    int count = 0;
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      count++;
    }
    assertEquals(traverserCount * batchHint, count);
    assertEquals("list 2", docList.checkpoint());
  }
}
