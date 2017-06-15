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

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.filenet.api.util.Id;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockDocument implements IDocument {
  private final Document doc;
  private final Map<String, Object> props;

  public MockDocument(Document object) {
    this.doc = object;
    this.props = new HashMap<String, Object>();
    props.put(PropertyNames.ID, doc.get_Id());
    props.put(PropertyNames.DATE_LAST_MODIFIED, doc.get_DateLastModified());
    props.put(PropertyNames.MIME_TYPE, doc.get_MimeType());
    props.put(PropertyNames.CONTENT_SIZE,
        String.valueOf(doc.get_ContentSize()));
  }

  @Override
  public Id get_Id() {
    return doc.get_Id();
  }

  @Override
  public Date get_DateLastModified() {
    return doc.get_DateLastModified();
  }

  @Override
  public AccessPermissionList get_Permissions() {
    return doc.get_Permissions();
  }

  @Override
  public String get_Owner() {
    return null;
  }

  @Override
  public InputStream getContent() {
    return new ByteArrayInputStream(
        "sample content".getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Double get_ContentSize() {
    return doc.get_ContentSize();
  }

  @Override
  public String get_MimeType() {
    return doc.get_MimeType();
  }

  @Override
  public IVersionSeries getVersionSeries() {
    return new MockVersionSeries(this.doc);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class ActiveMarkingListMock
      extends ArrayList implements ActiveMarkingList { }

  @Override
  public ActiveMarkingList get_ActiveMarkings()
      throws RepositoryDocumentException {
    return new ActiveMarkingListMock();
  }

  @Override
  public Folder get_SecurityFolder() {
    return null;
  }

  @Override
  public Set<String> getPropertyNames() {
    return props.keySet();
  }

  @Override
  public void getProperty(String name, List<Value> list)
      throws RepositoryDocumentException {
    if (PropertyNames.ID.equalsIgnoreCase(name)) {
      String val = (String) props.get(name);
      list.add(Value.getStringValue(val));
    } else if (PropertyNames.DATE_LAST_MODIFIED.equalsIgnoreCase(name)) {
      getPropertyDateValue(name, list);
    } else {
      getPropertyValue(name, list);
    }
  }

  private void getPropertyValue(String name, List<Value> list)
      throws RepositoryDocumentException {
    Object obj = props.get(name);
    if (obj == null) return;

    if (obj instanceof String) {
      getPropertyStringValue(name, list);
    } else if (obj instanceof Date) {
      getPropertyDateValue(name, list);
    }
  }

  @Override
  public void getPropertyStringValue(String name, List<Value> list)
      throws RepositoryDocumentException {
    String val = (String) props.get(name);
    list.add(Value.getStringValue(val));
  }

  @Override
  public void getPropertyDateValue(String name, List<Value> list)
      throws RepositoryDocumentException {
    Date val = (Date) props.get(name);
    Calendar cal = Calendar.getInstance();
    cal.setTime(val);
    list.add(Value.getDateValue(cal));
  }
}
