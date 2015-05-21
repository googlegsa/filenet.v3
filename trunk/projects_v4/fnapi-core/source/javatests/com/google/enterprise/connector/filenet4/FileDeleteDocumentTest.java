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

import static com.google.enterprise.connector.spi.SpiConstants.ActionType.DELETE;
import static com.google.enterprise.connector.spi.SpiConstants.PROPNAME_ACTION;
import static com.google.enterprise.connector.spi.SpiConstants.PROPNAME_DOCID;
import static com.google.enterprise.connector.spi.SpiConstants.PROPNAME_LASTMODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.util.Id;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

public class FileDeleteDocumentTest {
  private Date now;
  private Document doc;

  private static final String ZEROS_ID =
      "{00000000-0000-0000-0000-000000000000}";

  @Before
  public void setUp() throws RepositoryException {
    now = new Date();
    doc = new FileDeleteDocument(Id.ZERO_ID, now);
  }

  @Test
  public void getPropertyNames() throws RepositoryException {
    assertEquals(ImmutableSet.of(), doc.getPropertyNames());
  }

  @Test
  public void findProperty_action() throws RepositoryException {
    assertEquals(DELETE.toString(),
        Value.getSingleValueString(doc, PROPNAME_ACTION));
  }

  @Test
  public void findProperty_docid() throws RepositoryException {
    assertEquals(ZEROS_ID, Value.getSingleValueString(doc, PROPNAME_DOCID));
  }

  @Test
  public void findProperty_lastmodified() throws RepositoryException {
    Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    assertEquals(Value.calendarToIso8601(cal),
        Value.getSingleValueString(doc, PROPNAME_LASTMODIFIED));
  }

  /**
   * Tests that all of the unexpected pre-defined property names return null.
   */
  @Test
  public void findProperty_null()
      throws IllegalAccessException, RepositoryException {
    ImmutableSet<String> expectedNames =
        ImmutableSet.of(PROPNAME_ACTION, PROPNAME_DOCID, PROPNAME_LASTMODIFIED);
    for (Field field : SpiConstants.class.getFields()) {
      String name = field.getName();
      if (name.startsWith("PROPNAME")) {
        String value = (String) field.get(null);
        if (!expectedNames.contains(value)) {
          assertNull(doc.findProperty(value));
        }
      }
    }
  }
}
