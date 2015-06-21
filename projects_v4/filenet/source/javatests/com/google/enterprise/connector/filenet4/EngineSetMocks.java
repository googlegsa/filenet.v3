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

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.EngineSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.GroupSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;
import com.filenet.api.collection.SecurityPolicySet;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.security.Group;
import com.filenet.api.security.SecurityPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

class EngineSetMocks {
  public static class DocumentSetMock
      extends EngineSetMock<Document> implements DocumentSet {
    public DocumentSetMock() { super(); }
    public DocumentSetMock(Collection<? extends Document> values) {
      super(values);
    }
  }

  public static class FolderSetMock
      extends EngineSetMock<Folder> implements FolderSet {
    public FolderSetMock() { super(); }
    public FolderSetMock(Collection<? extends Folder> values) { super(values); }
  }

  public static class GroupSetMock
      extends EngineSetMock<Group> implements GroupSet {
    public GroupSetMock() { super(); }
    public GroupSetMock(Collection<? extends Group> values) { super(values); }
  }

  public static class IndependentObjectSetMock
      extends EngineSetMock<IndependentObject> implements IndependentObjectSet {
    public IndependentObjectSetMock() { super(); }
    public IndependentObjectSetMock(
        Collection<? extends IndependentObject> values) {
      super(values);
    }
  }

  public static class SecurityPolicySetMock
      extends EngineSetMock<SecurityPolicy> implements SecurityPolicySet {
    public SecurityPolicySetMock() { super(); }
    public SecurityPolicySetMock(Collection<? extends SecurityPolicy> values) {
      super(values);
    }
  }

  private static class EngineSetMock<T> implements EngineSet {
    private final Collection<? extends T> values;

    public EngineSetMock() {
      this.values = Collections.emptySet();
    }

    public EngineSetMock(Collection<? extends T> values) {
      this.values = values;
    }

    @Override
    public boolean isEmpty() {
      return values.isEmpty();
    }

    @Override
    public Iterator<?> iterator() {
      return values.iterator();
    }

    @Override
    public PageIterator pageIterator() {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return values.size();
    }
  }
}
