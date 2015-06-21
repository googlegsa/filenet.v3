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

package com.google.enterprise.connector.filenet4;

import com.google.common.collect.Iterators;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;

import java.util.Iterator;

public class EmptyObjectSet implements IndependentObjectSet {
  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public Iterator<?> iterator() {
    return Iterators.emptyIterator();
  }

  @Override
  public PageIterator pageIterator() {
    throw new UnsupportedOperationException();
  }
}
