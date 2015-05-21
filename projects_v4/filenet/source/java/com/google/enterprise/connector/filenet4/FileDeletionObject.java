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

import com.google.enterprise.connector.filenet4.api.IBaseObject;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import com.filenet.api.util.Id;

import java.util.Date;

/**
 * The main purpose of this wrapper is to provide object identity for custom
 * deleted objects apart from other FileNet objects and deletion events.  The
 * "instanceof FileDeletionObject" operation can be used to distinguish custom
 * deleted objects from other objects or documents.
 */
public class FileDeletionObject implements IBaseObject {
  private final IBaseObject baseObject;

  public FileDeletionObject(IBaseObject baseObject) {
    this.baseObject = baseObject;
  }

  @Override
  public Id get_Id() throws RepositoryDocumentException {
    return baseObject.get_Id();
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return baseObject.getModifyDate();
  }

  @Override
  public Id getVersionSeriesId() throws RepositoryDocumentException {
    return baseObject.getVersionSeriesId();
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
          throws RepositoryDocumentException {
    return baseObject.getPropertyDateValueDelete(name);
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return baseObject.isReleasedVersion();
  }
}
