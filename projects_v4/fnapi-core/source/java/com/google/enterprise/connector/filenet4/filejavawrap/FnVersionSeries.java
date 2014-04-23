// Copyright 2007-2010 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
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

  public String getId() {
    return versionSeries.get_Id().toString();

  }

  public Date getModifyDate() {
    // TODO(tdnguyen): revisit this method as the modify date could be the
    // versionSeries.get_CurrentVersion().get_DateCheckedIn() date.
    return new Date();
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    // TODO(tdnguyen): refactor this method.
    return (versionSeries instanceof DeletionEvent);
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

  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return new Date();
  }

  public String getVersionSeriesId() throws RepositoryDocumentException {
    String strId;
    try {
      if (versionSeries instanceof DeletionEvent) {
        // Version series Id is always enclosed with curly braces {versionId}.
        Id id = ((DeletionEvent) versionSeries).get_VersionSeriesId();
        strId = id.toString();
      } else {
        // Remove curly braces surrounding document id.
        Id id = ((Document) versionSeries).get_ReleasedVersion()
            .get_VersionSeries().get_Id();
        strId = id.toString();
        strId = strId.substring(1, strId.length() - 1);
      }
    } catch (Exception e) {
      throw new RepositoryDocumentException("Unable to get the VersionSeriesId",
          e);
    }
    return strId;
  }

  public IDocument getCurrentVersion() throws RepositoryException {
    return new FnDocument(
        (Document) this.versionSeries.get_CurrentVersion());
  }

  public IDocument getReleasedVersion() throws RepositoryException {
    return new FnDocument(
        (Document) this.versionSeries.get_ReleasedVersion());
  }

}
