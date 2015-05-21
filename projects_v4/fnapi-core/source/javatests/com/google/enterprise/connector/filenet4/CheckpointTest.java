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

import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.util.Id;

import org.junit.Test;

import java.util.Date;

public class CheckpointTest {
  @Test(expected = RepositoryException.class)
  public void uninitializedCheckpoint() throws RepositoryException {
    Checkpoint cp = new Checkpoint();
    cp.getString(JsonField.UUID);
  }

  @Test
  public void initializedCheckpoint() throws RepositoryException {
    String expectedId = "{AAAAAAAA-0000-0000-0000-000000000000}";
    Checkpoint cp = new Checkpoint();
    cp.setTimeAndUuid(JsonField.LAST_MODIFIED_TIME, new Date(),
        JsonField.UUID, new Id(expectedId));
    assertEquals(expectedId, cp.getString(JsonField.UUID));
  }
}
