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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterators;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.IVersionSeries;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.ActiveMarking;
import com.filenet.api.security.Marking;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.security.User;
import com.filenet.api.util.Id;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class FileAuthorizationHandlerTest {
  /** A more readable way to specify the connector check_markings config. */
  private enum MarkingsConfig { CHECK, SKIP };

  /**
   * @param expectedHasMarkings the expected hasMarkings return value
   * @param markingsConfig the connector check_markings property config
   * @param markingSet the marking set on a Document class attribute,
   *     or {@code null} to specify no marking sets
   */
  private void testHasMarkings(boolean expectedHasMarkings,
      MarkingsConfig markingsConfig, MarkingSet markingSet)
      throws RepositoryException {
    boolean checkMarkings = (markingsConfig == MarkingsConfig.CHECK);

    IObjectFactory factory = createMock(IObjectFactory.class);
    PropertyDefinition otherProperty = createMock(PropertyDefinition.class);
    PropertyDefinitionString stringProperty =
        createMock(PropertyDefinitionString.class);

    if (checkMarkings) {
      // We expect these calls iff we are going to check for marking sets.
      expect(factory.getPropertyDefinitions(isNull(IObjectStore.class),
              isA(Id.class), isNull(PropertyFilter.class)))
          .andReturn(Iterators.forArray(otherProperty, stringProperty));
      expect(stringProperty.get_MarkingSet()).andReturn(markingSet);
    }
    replay(factory, otherProperty, stringProperty);
    if (markingSet != null) {
      replay(markingSet);
    }

    FileAuthorizationHandler out =
        new FileAuthorizationHandler(null, factory, null, checkMarkings, null);
    assertEquals(expectedHasMarkings, out.hasMarkings());

    verify(factory, otherProperty, stringProperty);
    if (markingSet != null) {
      verify(markingSet);
    }
  }

  @Test
  public void testHasMarkings_on_with() throws RepositoryException {
    testHasMarkings(true, MarkingsConfig.CHECK, createMock(MarkingSet.class));
  }

  @Test
  public void testHasMarkings_on_without() throws RepositoryException {
    testHasMarkings(false, MarkingsConfig.CHECK, null);
  }

  @Test
  public void testHasMarkings_off_with() throws RepositoryException {
    testHasMarkings(false, MarkingsConfig.SKIP, createMock(MarkingSet.class));
  }

  @Test
  public void testHasMarkings_off_without() throws RepositoryException {
    testHasMarkings(false, MarkingsConfig.SKIP, null);
  }

  @Test
  public void testHasMarkings_exception() throws RepositoryException {
    IObjectFactory factory = createMock(IObjectFactory.class);
    expect(factory.getPropertyDefinitions(isNull(IObjectStore.class),
            isA(Id.class), isNull(PropertyFilter.class)))
        .andThrow(new RepositoryException("pretend something bad happened"));
    replay(factory);

    FileAuthorizationHandler out =
        new FileAuthorizationHandler(null, factory, null, true, null);
    assertEquals(true, out.hasMarkings());

    verify(factory);
  }

  private void testAuthorizeDocid(boolean expectedIsValid, boolean hasMarking,
      boolean isAuthorized, boolean isMarkingAuthorized,
      boolean authorizeMarkings) throws RepositoryException {
    String docid = "{AAAAAAAA-0000-0000-0000-000000000000}";

    Marking marking = createNiceMock(Marking.class);
    ActiveMarking activeMarking = createMock(ActiveMarking.class);
    IDocument doc = createNiceMock(IDocument.class);
    expect(activeMarking.get_Marking()).andStubReturn(marking);
    // TODO(jlacey): Switch to andReturn when this is only called once.
    expect(doc.get_ActiveMarkings()).andStubReturn(
        hasMarking ? new ActiveMarkingListMock(activeMarking) : null);

    IVersionSeries vs = createMock(IVersionSeries.class);
    expect(vs.get_ReleasedVersion()).andStubReturn(doc);
    IObjectStore objectStore = createMock(IObjectStore.class);
    expect(objectStore.getObject(ClassNames.VERSION_SERIES, docid))
        .andReturn(vs);

    User user = createMock(User.class);
    expect(user.get_Name()).andStubReturn("Jane Doe");

    Permissions permissions = createMock(Permissions.class);
    expect(permissions.authorize(user)).andReturn(isAuthorized);
    if (isAuthorized && authorizeMarkings && hasMarking) {
      expect(permissions.authorizeMarking(eq(user), anyInt()))
          .andReturn(isMarkingAuthorized);
    }
    replay(marking, activeMarking, doc, vs, objectStore, user, permissions);

    FileAuthorizationHandler out = new FileAuthorizationHandler(null, null,
        objectStore, true, new MockPermissionsFactory(permissions));

    assertEquals(new AuthorizationResponse(expectedIsValid, docid),
        out.authorizeDocid(docid, user, authorizeMarkings));
    verify(marking, activeMarking, doc, vs, objectStore, user, permissions);
  }

  @Test
  public void testAuthorizeDocid_validMarking() throws RepositoryException {
    testAuthorizeDocid(true, true, true, true, true);
  }

  @Test
  public void testAuthorizeDocid_invalidMarking() throws RepositoryException {
    testAuthorizeDocid(false, true, true, false, true);
  }

  @Test
  public void testAuthorizeDocid_noActiveMarkings() throws RepositoryException {
    testAuthorizeDocid(true, false, true, false, true);
  }

  @Test
  public void testAuthorizeDocid_authorizeMarkingsFalse()
      throws RepositoryException {
    testAuthorizeDocid(true, true, true, false, false);
  }

  @Test
  public void testAuthorizeDocid_invalidAcl()
      throws RepositoryException {
    testAuthorizeDocid(false, true, false, true, true);
  }

  @Test
  public void testAuthorizeDocid_fetchException() throws RepositoryException {
    String docid = "{AAAAAAAA-0000-0000-0000-000000000000}";
    RepositoryException thrown =
        new RepositoryDocumentException("pretend something bad happened");

    IObjectStore objectStore = createMock(IObjectStore.class);
    expect(objectStore.getObject(ClassNames.VERSION_SERIES, docid))
        .andThrow(thrown);
    User user = createNiceMock(User.class);
    replay(objectStore, user);

    FileAuthorizationHandler out = new FileAuthorizationHandler(null, null,
        objectStore, true, null);

    try {
      out.authorizeDocid(docid, user, true);
    } catch (RepositoryException caught) {
      if (caught != thrown) {
        throw caught;
      }
    }
    verify(objectStore, user);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class ActiveMarkingListMock
      extends ArrayList implements ActiveMarkingList {
    ActiveMarkingListMock(ActiveMarking... markings) {
      Collections.addAll(this, markings);
    }
  }

  private static class MockPermissionsFactory implements Permissions.Factory {
    private final Permissions permissions;

    MockPermissionsFactory(Permissions permissions) {
      this.permissions = permissions;
    }

    @Override
    public Permissions getInstance(AccessPermissionList perms, String owner) {
      return permissions;
    }

    @Override
    public Permissions getInstance(AccessPermissionList perms) {
      return permissions;
    }
  }
}
