// Copyright 2008 Google Inc.
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

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;

import java.util.Iterator;
import java.util.List;

public class FnObjectList implements IObjectSet {
  private final List<? extends IBaseObject> objectList;

  public FnObjectList(List<? extends IBaseObject> objectList) {
    this.objectList = objectList;
  }

  @Override
  public int getSize() {
    return objectList.size();
  }

  @Override
  public Iterator<? extends IBaseObject> getIterator() {
    return objectList.iterator();
  }
}
