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

package com.google.enterprise.connector.filenet4.api;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.core.Document;
import com.filenet.api.events.DeletionEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MockBaseObject extends FnBaseObject {
  private final boolean releasedVersion;
  private final Document doc;
  private final Map<String, Object> props = new HashMap<>();

  public MockBaseObject(DeletionEvent object, boolean releasedVersion) {
    super(object);
    this.releasedVersion = releasedVersion;
    this.doc = null;
  }

  public MockBaseObject(Document object, boolean releasedVersion) {
    super(object);
    this.releasedVersion = releasedVersion;
    this.doc = object;
  }

  @Override
  public Date getPropertyDateValueDelete(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isReleasedVersion() {
    if (isDeletionEvent()) {
      // TODO(jlacey): This object may not be in the object store.
      // See ObjectMocks details.
      return releasedVersion;
    } else {
      return super.isReleasedVersion();
    }
  }

  AccessPermissionList get_Permissions() {
    return doc.get_Permissions();
  }

  public void setProperty(String name, Object value) {
    props.put(name, value);
  }

  public Object getProperty(String name) {
    return props.get(name);
  }

  public Map<String, Object> getProperties() {
    return props;
  }
}
