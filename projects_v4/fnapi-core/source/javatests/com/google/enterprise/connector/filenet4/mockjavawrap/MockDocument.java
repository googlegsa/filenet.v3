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

package com.google.enterprise.connector.filenet4.mockjavawrap;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.filenet4.filewrap.IActiveMarkingList;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockDocument implements IDocument {
  public final static String FN_ID = "Id";
  public final static String FN_LAST_MODIFIED = "DateLastModified";

  private final IBaseObject doc;
  private final Map<String, ?> props;

  public MockDocument(IBaseObject object) throws RepositoryDocumentException {
    this.doc = object;
    this.props = ImmutableMap.of(FN_ID, doc.get_Id(),
        FN_LAST_MODIFIED, doc.getModifyDate());
  }

  @Override
  public IId get_Id() throws RepositoryDocumentException {
    return doc.get_Id();
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return doc.getModifyDate();
  }

  @Override
  public IId getVersionSeriesId() throws RepositoryDocumentException {
    return doc.getVersionSeriesId();
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return doc.getPropertyDateValueDelete(name);
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return doc.isReleasedVersion();
  }

  @Override
  public IPermissions getPermissions() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getContent() throws RepositoryDocumentException {
    return null;
  }

  @Override
  public IVersionSeries getVersionSeries() throws RepositoryDocumentException {
    return new MockVersionSeries(this.doc);
  }

  @Override
  public Set<String> getPropertyNames() throws RepositoryDocumentException {
    return props.keySet();
  }

  @Override
  public void getProperty(String name, List<Value> list)
      throws RepositoryDocumentException {
    if (FN_ID.equalsIgnoreCase(name)) {
      getPropertyGuidValue(name, list);
    } else if (FN_LAST_MODIFIED.equalsIgnoreCase(name)) {
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
  public void getPropertyGuidValue(String name, List<Value> list)
      throws RepositoryDocumentException {
    String val = (String) props.get(name);
    list.add(Value.getStringValue(val));
  }

  @Override
  public void getPropertyLongValue(String name, List<Value> list)
      throws RepositoryDocumentException {}

  @Override
  public void getPropertyDoubleValue(String name, List<Value> list)
      throws RepositoryDocumentException {}

  @Override
  public void getPropertyDateValue(String name, List<Value> list)
      throws RepositoryDocumentException {
    Date val = (Date) props.get(name);
    Calendar cal = Calendar.getInstance();
    cal.setTime(val);
    list.add(Value.getDateValue(cal));
  }

  @Override
  public void getPropertyBooleanValue(String name, List<Value> list)
      throws RepositoryDocumentException {}

  @Override
  public void getPropertyBinaryValue(String name, List<Value> list)
      throws RepositoryDocumentException {}

  @Override
  public IActiveMarkingList get_ActiveMarkings()
      throws RepositoryDocumentException { return null; }
}
