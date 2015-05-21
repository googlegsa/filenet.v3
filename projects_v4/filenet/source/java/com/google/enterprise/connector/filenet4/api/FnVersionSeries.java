// Copyright 2008 Google Inc. All Rights Reserved.
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

import com.filenet.api.constants.VersionStatus;
import com.filenet.api.core.Document;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.events.DeletionEvent;
import com.filenet.api.util.Id;

import java.util.Date;
import java.util.logging.Logger;

public class FnVersionSeries implements IVersionSeries {
  private static final Logger logger =
      Logger.getLogger(FnVersionSeries.class.getName());

  private final VersionSeries versionSeries;

  public FnVersionSeries(VersionSeries versionSeries) {
    this.versionSeries = versionSeries;
  }

  @Override
  public Id get_Id() throws RepositoryDocumentException {
    return versionSeries.get_Id();
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    try {
      return versionSeries.get_CurrentVersion().get_DateCheckedIn();
    } catch (Exception e) {
      throw new RepositoryDocumentException(
          "Failed to retrieve the last modified or checked-in date.", e);
    }
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    if (versionSeries instanceof DeletionEvent) {
      return false;
    } else {
      return VersionStatus.RELEASED.equals(
          versionSeries.get_CurrentVersion().get_VersionStatus());
    }
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return new Date();
  }

  @Override
  public Id getVersionSeriesId() throws RepositoryDocumentException {
    return get_Id();
  }

  @Override
  public IDocument get_ReleasedVersion() throws RepositoryException {
    return new FnDocument(
        (Document) this.versionSeries.get_ReleasedVersion());
  }
}
