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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.util.Id;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

public class CheckpointTest {
  /** Asserts that the given field in the checkpoint is null. */
  static void assertNullField(Checkpoint cp, JsonField field)
      throws RepositoryException {
    if (!cp.isNull(field)) {
      fail("Expected a null field but was:<" + cp.getString(field) + ">");
    }
    try {
      cp.getString(field);
      fail("Expected a RepositoryException based on a NPE for " + field);
    } catch (RepositoryException expected) {
    }
  }

  /** Asserts that the two dates match within ten seconds. */
  static void assertDateNearly(Date expectedDate, String actualDate)
      throws ParseException {
    long expectedMillis = expectedDate.getTime();
    long actualMillis = Value.iso8601ToCalendar(actualDate).getTimeInMillis();
    assertTrue(actualDate, Math.abs(expectedMillis - actualMillis) < 10000L);
  }


  @Test
  public void uninitializedCheckpoint_add() throws RepositoryException {
    Checkpoint cp = new Checkpoint();
    assertNullField(cp, JsonField.LAST_MODIFIED_TIME);
    assertNullField(cp, JsonField.UUID);
  }

  @Test
  public void uninitializedCheckpoint_delete() throws Exception {
    Checkpoint cp = new Checkpoint();
    Date now = new Date();
    assertDateNearly(now, cp.getString(JsonField.LAST_DELETION_EVENT_TIME));
    assertEquals("", cp.getString(JsonField.UUID_DELETION_EVENT));
  }

  @Test
  public void uninitializedCheckpoint_deleteDocs() throws Exception {
    Checkpoint cp = new Checkpoint();
    Date now = new Date();
    assertDateNearly(now, cp.getString(JsonField.LAST_CUSTOM_DELETION_TIME));
    assertEquals("", cp.getString(JsonField.UUID_CUSTOM_DELETED_DOC));
  }

  @Test
  public void initializedCheckpoint() throws Exception {
    String expectedId = "{AAAAAAAA-0000-0000-0000-000000000000}";
    Checkpoint cp = new Checkpoint();
    Date now = new Date();
    cp.setTimeAndUuid(JsonField.LAST_MODIFIED_TIME, now,
        JsonField.UUID, new Id(expectedId));
    assertDateNearly(now, cp.getString(JsonField.LAST_MODIFIED_TIME));
    assertEquals(expectedId, cp.getString(JsonField.UUID));
  }
}
