// Copyright 2015 Google Inc. All Rights Reserved.
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
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.enterprise.connector.filenet4.api.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;

import org.junit.After;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TraverserFactoryFixture {
  private final List<Object> mocksToVerify = new ArrayList<>();

  @After
  public void verifyMocks() {
    for (Object mock : mocksToVerify) {
      verify(mock);
    }
  }

  protected void replayAndVerify(Object... mocks) {
    replay(mocks);
    Collections.addAll(mocksToVerify, mocks);
  }

  protected FileDocumentTraverser getFileDocumentTraverser(
      FileConnector connector, IObjectSet objectSet)
      throws RepositoryException {
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(isA(String.class))).andReturn(objectSet).times(2);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher);
    replayAndVerify(os, searcher, objectFactory);

    return new FileDocumentTraverser(objectFactory, os, connector);
  }

  protected SecurityFolderTraverser getSecurityFolderTraverser(
      FileConnector connector, IObjectSet folderSet)
      throws RepositoryException {
    IObjectStore os = createNiceMock(IObjectStore.class);
    IBaseObjectFactory baseFactory = createNiceMock(IBaseObjectFactory.class);
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(isA(String.class), eq(100), eq(0), eq(baseFactory)))
        .andReturn(folderSet).atLeastOnce();
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    expect(objectFactory.getFactory(isA(String.class)))
        .andReturn(baseFactory).atLeastOnce();
    replayAndVerify(os, searcher, objectFactory);

    return new SecurityFolderTraverser(objectFactory, os, connector);
  }

  protected SecurityPolicyTraverser getSecurityPolicyTraverser(
      FileConnector connector, IObjectSet secPolicySet, IObjectSet docSet)
      throws RepositoryException {
    IObjectStore os = createNiceMock(IObjectStore.class);
    IBaseObjectFactory baseFactory = createNiceMock(IBaseObjectFactory.class);
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(isA(String.class), eq(100), eq(0), eq(baseFactory)))
        .andReturn(secPolicySet).atLeastOnce();
    if (secPolicySet.getSize() > 0) {
      expect(searcher.execute(isA(String.class), eq(100), eq(1),
              eq(baseFactory)))
          .andReturn(docSet)
          .times(secPolicySet.getSize(), secPolicySet.getSize() * 2);
    }
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    expect(objectFactory.getFactory(isA(String.class)))
        .andReturn(baseFactory).atLeastOnce();
    replayAndVerify(objectFactory, os, searcher);

    return new SecurityPolicyTraverser(objectFactory, os, connector);
  }
}
