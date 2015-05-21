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

import com.filenet.api.constants.VersionStatus;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.core.Versionable;
import com.filenet.api.events.DeletionEvent;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.util.Id;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class FnBaseObject implements IBaseObject {
  private static final Logger logger =
      Logger.getLogger(FnBaseObject.class.getName());

  private final IndependentObject object;

  public FnBaseObject(IndependentObject object) {
    this.object = object;
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return (object instanceof DeletionEvent);
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
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
      } catch (Exception e) {
        logger.log(Level.FINEST,
            "The version series {0} for deleted document [{1}] is NOT found",
            new Object[] {de.get_VersionSeriesId(), de.get_SourceObjectId()});
        return true;
      }
    } else {
      Versionable versionable = null;
      if (object instanceof VersionSeries) {
        versionable = ((VersionSeries) object).get_CurrentVersion();
      } else if (object instanceof Document) {
        versionable = ((Document) object).get_CurrentVersion();
      }
      return versionable != null
          && VersionStatus.RELEASED.equals(versionable.get_VersionStatus());
    }
  }

  @Override
  public Id get_Id() throws RepositoryDocumentException {
    if (object instanceof DeletionEvent) {
      return ((DeletionEvent) object).get_Id();
    } else {
      return ((Document) object).get_Id();
    }
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
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

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    try {
      Properties props = ((DeletionEvent) this.object).getProperties();
      Iterator it = props.iterator();
      while (it.hasNext()) {
        Property prop = (Property) it.next();
        String propName = prop.getPropertyName();
        if (propName.equalsIgnoreCase(name)) {
          return prop.getDateTimeValue();
        }
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error while trying to get the property "
          + name
          + " of the file "
          + ((DeletionEvent) this.object).get_Id()
          + " " + e.getMessage());
      RepositoryDocumentException re = new RepositoryDocumentException(e);
      throw re;
    }
    return null;
  }

  @Override
  public Id getVersionSeriesId() throws RepositoryDocumentException {
    if (object instanceof DeletionEvent) {
      return ((DeletionEvent) this.object).get_VersionSeriesId();
    } else {
      return ((Document) this.object).get_ReleasedVersion()
          .get_VersionSeries().get_Id();
    }
  }
}
