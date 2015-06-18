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

package com.google.enterprise.connector.filenet4.api;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.core.Folder;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Interface between the Client Document and Core document.
 */
public interface IDocument extends IBaseObject {

  AccessPermissionList get_Permissions();

  String get_Owner();

  InputStream getContent();

  IVersionSeries getVersionSeries();

  Set<String> getPropertyNames();

  void getProperty(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyStringValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyGuidValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyLongValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyDoubleValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyDateValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyBooleanValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  void getPropertyBinaryValue(String name, List<Value> list)
      throws RepositoryDocumentException;

  ActiveMarkingList get_ActiveMarkings() throws RepositoryDocumentException;

  Folder get_SecurityFolder();
}
