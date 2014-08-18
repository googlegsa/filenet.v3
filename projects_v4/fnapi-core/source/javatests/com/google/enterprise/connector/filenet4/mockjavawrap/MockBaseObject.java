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
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.mock.AccessPermissionListMock;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.AccessPermissionList;

import java.util.Date;

public class MockBaseObject implements IBaseObject {
  private final IId id;
  private final IId versionSeriesId;
  private final Date lastModified;
  private final boolean isDeleteEvent;
  private final boolean releasedVersion;
  private final AccessPermissionList permissions;

  public MockBaseObject(IId id, IId versionSeriesId, Date lastModified,
     boolean isDeletionEvent, boolean releasedVersion) {
    this(id, versionSeriesId, lastModified, isDeletionEvent, releasedVersion,
        new AccessPermissionListMock());
  }

  public MockBaseObject(IId id, IId versionSeriesId, Date lastModified,
      boolean isDeletionEvent, boolean releasedVersion,
      AccessPermissionList perms) {
    this.id = id;
    this.versionSeriesId = versionSeriesId;
    this.lastModified = lastModified;
    this.isDeleteEvent = isDeletionEvent;
    this.releasedVersion = releasedVersion;
    this.permissions = perms;
  }

  @Override
  public IId get_Id() throws RepositoryDocumentException {
    return id;
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return lastModified;
  }

  @Override
  public IId getVersionSeriesId() throws RepositoryDocumentException {
    return versionSeriesId;
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
          throws RepositoryDocumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return isDeleteEvent;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return releasedVersion;
  }

  AccessPermissionList get_Permissions() throws RepositoryException {
    return permissions;
  }
}
