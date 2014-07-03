// Copyright 2014 Google Inc.  All Rights Reserved.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.util.Id;

public class FnId implements IId {
  private final Id id;

  public FnId(String sid) throws RepositoryDocumentException {
    if (Strings.isNullOrEmpty(sid)) {
      throw new RepositoryDocumentException("ID is null or empty");
    }
    this.id = new Id(sid);
  }

  public FnId(Id id) throws RepositoryDocumentException {
    if (id == null) {
      throw new RepositoryDocumentException("ID is null");
    }
    this.id = id;
  }

  @Override
  public int compareTo(IId otherId, DatabaseType dbType) {
    return id.compareTo(((FnId) otherId).getId(), dbType);
  }

  public Id getId() {
    return id;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FnId)) {
      return false;
    }
    FnId other = (FnId) obj;
    return id.equals(other.id);
  }
}
