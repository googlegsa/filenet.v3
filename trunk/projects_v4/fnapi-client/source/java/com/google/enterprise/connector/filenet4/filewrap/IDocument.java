// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Interface between the Client Document and Core document.
 *
 * @author pankaj_chouhan
 */
public interface IDocument extends IBaseObject {
  void fetch(Set<String> includedMeta) throws RepositoryDocumentException;

  IPermissions getPermissions() throws RepositoryException;

  InputStream getContent() throws RepositoryDocumentException;

  IVersionSeries getVersionSeries() throws RepositoryDocumentException;;

  Set getPropertyName() throws RepositoryDocumentException;

  String getPropertyType(String name) throws RepositoryDocumentException;

  void getPropertyStringValue(String name, List<Value> set)
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

  IActiveMarkingList getActiveMarkings() throws RepositoryDocumentException;;

}
