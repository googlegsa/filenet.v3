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
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;

import java.util.Iterator;
import java.util.Map;

public class MockObjectStore implements IObjectStore {
  private final Map<Id, IBaseObject> objects;
  private final String name;
  private final DatabaseType dbType;

  public MockObjectStore(String name, DatabaseType databaseType,
      Map<Id, IBaseObject> objects) {
    this.name = name;
    this.objects = objects;
    this.dbType = databaseType;
  }

  @Override
  public IBaseObject getObject(String type, String id)
      throws RepositoryDocumentException {
    return getObject(type, new Id(id));
  }

  @Override
  public IBaseObject getObject(String type, Id id)
      throws RepositoryDocumentException {
    return objects.get(id);
  }

  @Override
  public IBaseObject fetchObject(String type, Id id, PropertyFilter filter)
          throws RepositoryDocumentException {
    IBaseObject obj = objects.get(id);
    if (ClassNames.DOCUMENT.equals(type)) {
      return new MockDocument(obj);
    } else if (ClassNames.VERSION_SERIES.equals(type)) {
      return new MockVersionSeries(obj);
    } else {
      return obj;
    }
  }

  @Override
  public String get_Name() throws RepositoryException {
    return this.name;
  }

  @Override
  public Iterator<PropertyDefinition> getPropertyDefinitions(Id objectId,
      PropertyFilter filter) throws RepositoryException {
    return null;
  }

  @Override
  public void refreshSUserContext() throws RepositoryLoginException {
    // Not implemented
  }

  @Override
  public DatabaseType get_DatabaseType() {
    return this.dbType;
  }

  public Map<Id, IBaseObject> getObjects() {
    return objects;
  }
}
