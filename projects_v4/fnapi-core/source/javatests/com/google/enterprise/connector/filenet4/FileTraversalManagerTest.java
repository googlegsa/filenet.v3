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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.util.EmptyDocumentList;

import org.junit.Before;
import org.junit.Test;

public class FileTraversalManagerTest {
  private DocumentList emptyList;
  private Traverser traverser;
  private FileTraversalManager traversalMgr;

  @Before
  public void setUp() throws RepositoryException {
    emptyList = new EmptyDocumentList("");
    traverser = createMock(Traverser.class);
    traversalMgr = new FileTraversalManager(traverser);
  }

  @Test
  public void testStartTraversal() throws RepositoryException {
    expect(traverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(emptyList);
    replay(traverser);

    DocumentList docList = traversalMgr.startTraversal();
    assertEquals(emptyList, docList);
    verify(traverser);
  }

  @Test
  public void testResumeTraversal() throws RepositoryException {
    expect(traverser.getDocumentList(isA(Checkpoint.class)))
        .andReturn(emptyList);
    replay(traverser);

    // The checkpoint must be a valid JSON string.
    DocumentList docList = traversalMgr.resumeTraversal("{}");
    assertEquals(emptyList, docList);
    verify(traverser);
  }

  @Test
  public void testSetBatchHint() throws RepositoryException {
    traverser.setBatchHint(42);
    replay(traverser);

    traversalMgr.setBatchHint(42);
    verify(traverser);
  }
}
