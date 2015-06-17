// Copyright 2015 Google Inc. All Rights Reserved.
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
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.MockBaseObject;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.util.Id;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ObjectMocks {
  private static final SimpleDateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private static Date parseTime(String timeStr) {
    try {
      return dateFormatter.parse(timeStr);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }

  public static IBaseObject newBaseObject(String guid, String timeStr,
      boolean isReleasedVersion) {
    return new MockBaseObject(new Id(guid), new Id(guid), parseTime(timeStr),
        false, isReleasedVersion);
  }

  public static IBaseObject newBaseObject(String guid, String timeStr,
      boolean isReleasedVersion, AccessPermissionList perms) {
    return new MockBaseObject(new Id(guid), new Id(guid), parseTime(timeStr),
        false, isReleasedVersion, perms);
  }

  public static IBaseObject newDeletionEvent(String guid, String timeStr,
      boolean isReleasedVersion) {
    return new MockBaseObject(new Id(guid), new Id(guid), parseTime(timeStr),
        true, isReleasedVersion);
  }

  public static MockObjectStore newObjectStore(DatabaseType dbType,
      IObjectSet... objectSets) throws RepositoryDocumentException {
    return new MockObjectStore(dbType, generateObjectMap(objectSets));
  }

  /**
   * Generate a map of IBaseObject objects.
   *
   * @param objectSets zero or more object sets
   * @return a map from ID to object for all objects in the given sets
   */
  public static Map<Id, IBaseObject> generateObjectMap(IObjectSet... objectSets)
      throws RepositoryDocumentException {
    Map<Id, IBaseObject> objectMap = new HashMap<Id, IBaseObject>();
    for (IObjectSet objectSet : objectSets) {
      Iterator<?> iter = objectSet.iterator();
      while (iter.hasNext()) {
        IBaseObject object = (IBaseObject) iter.next();
        objectMap.put(object.get_Id(), object);
      }
    }
    return objectMap;
  }
}
