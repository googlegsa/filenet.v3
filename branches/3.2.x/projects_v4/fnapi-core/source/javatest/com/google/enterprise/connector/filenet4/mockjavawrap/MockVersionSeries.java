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

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Date;

public class MockVersionSeries implements IVersionSeries {
  private final IBaseObject object;

  public MockVersionSeries(IBaseObject object) {
    this.object = object;
  }

  @Override
  public IId getId() throws RepositoryDocumentException {
    return object.getId();
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return object.getModifyDate();
  }

  @Override
  public IId getVersionSeriesId() throws RepositoryDocumentException {
    return object.getVersionSeriesId();
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return object.getPropertyDateValueDelete(name);
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return object.isReleasedVersion();
  }

  @Override
  public IDocument getCurrentVersion() throws RepositoryException {
    return new MockDocument(object);
  }

  @Override
  public IDocument getReleasedVersion() throws RepositoryException {
    return new MockDocument(object);
  }
}
