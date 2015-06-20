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

package com.google.enterprise.connector.filenet4;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.VersionStatus;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.core.Versionable;
import com.filenet.api.events.DeletionEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@code Comparable} wrapper on IndependentObject that includes the type,
 * so that added and updated documents can be distinguished from the
 * deleted documents returned by the additional delete query.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
class SearchObject implements Comparable<SearchObject> {
  private static final Logger logger =
      Logger.getLogger(SearchObject.class.getName());

  public static enum Type { ADD, CUSTOM_DELETE, DELETION_EVENT };

  private final IndependentObject object;
  private final DatabaseType databaseType;
  private final Type type;

  public SearchObject(IndependentObject object, DatabaseType databaseType,
      Type type) {
    this.object = object;
    this.databaseType = databaseType;
    this.type = type;
  }

  @Override
  public int compareTo(SearchObject that) {
    int val = this.getModifyDate().compareTo(that.getModifyDate());
    if (val == 0) {
      val = this.get_Id().compareTo(that.get_Id(), databaseType);
    }
    return val;
  }

  public Type getType() {
    return type;
  }

  public boolean isReleasedVersion() {
    if (object instanceof DeletionEvent) {
      // Lookup VersionSeries of the deleted document.  If an exception is
      // thrown, the deleted document is the last document in the series;
      // therefore, we'll send the deletion request to the GSA; otherwise, we
      // will return false.  If the deleted document is the released version,
      // FileNet will delete all versions of the document.
      DeletionEvent de = (DeletionEvent) object;
      try {
        VersionSeries vs = Factory.VersionSeries.fetchInstance(
            de.getObjectStore(), de.get_VersionSeriesId(), null);
        logger.log(Level.FINEST,
            "The version series {0} for deleted document [{1}] is found",
            new Object[] {vs.get_Id(), de.get_SourceObjectId()});
        return false;
      } catch (EngineRuntimeException e) {
        logger.log(Level.FINEST,
            "The version series {0} for deleted document [{1}] is NOT found",
            new Object[] {de.get_VersionSeriesId(), de.get_SourceObjectId()});
        return true;
      }
    } else {
      // TODO(jlacey): I think the check for null is obsolete, but I'm
      // not sure whether get_CurrentVersion can return null.
      Versionable versionable = ((Document) object).get_CurrentVersion();
      return versionable != null
          && VersionStatus.RELEASED.equals(versionable.get_VersionStatus());
    }
  }

  public Id get_Id() {
    if (object instanceof DeletionEvent) {
      return ((DeletionEvent) object).get_Id();
    } else {
      return ((Document) object).get_Id();
    }
  }

  public Date getModifyDate() {
    Date modifiedDate;
    if (object instanceof DeletionEvent) {
      modifiedDate = ((DeletionEvent) object).get_DateCreated();
      logger.log(Level.FINEST, "[DeletionEvent] Created on {0}", modifiedDate);
    } else {
      modifiedDate = ((Document) object).get_DateLastModified();
      logger.log(Level.FINEST, "[Document] Last modified on {0}", modifiedDate);
    }
    return modifiedDate;
  }

  public Id getVersionSeriesId() {
    if (object instanceof DeletionEvent) {
      return ((DeletionEvent) this.object).get_VersionSeriesId();
    } else {
      return ((Document) this.object).get_ReleasedVersion()
          .get_VersionSeries().get_Id();
    }
  }
}
