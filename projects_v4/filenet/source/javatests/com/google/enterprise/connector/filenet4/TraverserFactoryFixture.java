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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.enterprise.connector.filenet4.EngineSetMocks.SecurityPolicySetMock;
import com.google.enterprise.connector.filenet4.api.IBaseObject;
import com.google.enterprise.connector.filenet4.api.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.After;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// Some JVMs require this class to be public in order for JUnit to
// call newInstance on the subclasses.
public class TraverserFactoryFixture {
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
    return getFileDocumentTraverser(connector, objectSet,
        new Capture<String>(CaptureType.NONE));
  }

  protected FileDocumentTraverser getFileDocumentTraverser(
      FileConnector connector, IObjectSet objectSet, Capture<String> capture)
      throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);
    IObjectStore os = createMockBuilder(PartialObjectStore.class)
        .withConstructor(objectSet).createNiceMock();

    // The first search result is for added and update documents, and
    // the second and optional third results (both empty) are for
    // deleted documents.
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(capture(capture))).andReturn(objectSet)
        .andReturn(new EmptyObjectSet()).times(1, 2);

    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher);
    replayAndVerify(connection, os, searcher, objectFactory);

    return new FileDocumentTraverser(connection, objectFactory, os, connector);
  }

  /** Partial mock for IObjectStore since fetchObject is not trivial. */
  private static abstract class PartialObjectStore implements IObjectStore {
    private final IObjectSet objectSet;

    PartialObjectStore(IObjectSet objectSet) {
      this.objectSet = objectSet;
    }

    @Override
    public IBaseObject fetchObject(String type, Id id, PropertyFilter filter)
        throws RepositoryDocumentException {
      Iterator<?> it = objectSet.iterator();
      while (it.hasNext()) {
        IBaseObject obj = (IBaseObject) it.next();
        if (obj.get_Id().equals(id)) {
          return obj;
        }
      }
      // TODO(jlacey): Does the real implementation throw an exception instead?
      return null;
    }
  }

  protected SecurityFolderTraverser getSecurityFolderTraverser(
      FileConnector connector, FolderSet folderSet)
      throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(isA(String.class), eq(100), eq(0)))
        .andReturn(folderSet).atLeastOnce();
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    replayAndVerify(connection, os, searcher, objectFactory);

    return new SecurityFolderTraverser(connection, objectFactory, os,
        connector);
  }

  protected SecurityPolicyTraverser getSecurityPolicyTraverser(
      FileConnector connector, SecurityPolicySetMock secPolicySet,
      IObjectSet docSet) throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);
    IObjectStore os = createNiceMock(IObjectStore.class);
    IBaseObjectFactory baseFactory = createNiceMock(IBaseObjectFactory.class);
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(isA(String.class), eq(100), eq(0)))
        .andReturn(secPolicySet).atLeastOnce();
    if (!secPolicySet.isEmpty()) {
      expect(searcher.execute(isA(String.class), eq(100), eq(1),
              eq(baseFactory)))
          .andReturn(docSet)
          .times(secPolicySet.size(), secPolicySet.size() * 2);
    }
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    expect(objectFactory.getFactory(isA(String.class)))
        .andReturn(baseFactory).atLeastOnce();
    replayAndVerify(connection, objectFactory, os, searcher);

    return new SecurityPolicyTraverser(connection, objectFactory, os,
        connector);
  }

  protected AccessPermissionList getPermissions(PermissionSource source) {
    List<AccessPermission> aces = TestObjectFactory.generatePermissions(
        1, 1, 1, 1, (AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT),
        0, source);
    return TestObjectFactory.newPermissionList(aces);
  }
}
