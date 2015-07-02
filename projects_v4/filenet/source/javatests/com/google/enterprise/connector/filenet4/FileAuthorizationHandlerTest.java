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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterators;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.util.Id;

import org.junit.Test;

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
        new FileAuthorizationHandler(null, factory, null, checkMarkings);
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
        new FileAuthorizationHandler(null, factory, null, true);
    assertEquals(true, out.hasMarkings());

    verify(factory);
  }
}
