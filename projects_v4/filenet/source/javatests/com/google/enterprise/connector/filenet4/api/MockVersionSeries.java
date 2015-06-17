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
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.util.Id;

import java.util.Date;

public class MockVersionSeries implements IVersionSeries {
  private final IBaseObject object;

  public MockVersionSeries(IBaseObject object) {
    this.object = object;
  }

  @Override
  public Id get_Id() {
    return object.get_Id();
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return object.getModifyDate();
  }

  @Override
  public Id getVersionSeriesId() {
    return object.getVersionSeriesId();
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return object.getPropertyDateValueDelete(name);
  }

  @Override
  public boolean isDeletionEvent() {
    return false;
  }

  @Override
  public boolean isReleasedVersion() {
    return object.isReleasedVersion();
  }

  @Override
  public IDocument get_ReleasedVersion() throws RepositoryException {
    return new MockDocument(object);
  }
}
