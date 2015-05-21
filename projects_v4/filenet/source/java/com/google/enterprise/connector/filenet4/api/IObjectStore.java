// Copyright 2007 Google Inc. All Rights Reserved.
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
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;

import java.util.Iterator;

public interface IObjectStore {

  public IBaseObject getObject(String type, String id)
      throws RepositoryDocumentException;

  public IBaseObject getObject(String type, Id id)
      throws RepositoryDocumentException;

  public IBaseObject fetchObject(String type, Id id,
      PropertyFilter filter) throws RepositoryDocumentException;

  String get_Name() throws RepositoryException;

  Iterator<PropertyDefinition> getPropertyDefinitions(Id objectId,
      PropertyFilter filter) throws RepositoryException;

  public DatabaseType get_DatabaseType() throws RepositoryException;

  public void refreshSUserContext() throws RepositoryLoginException;

  public String getSUserLogin();

  public String getSUserPassword();
}
