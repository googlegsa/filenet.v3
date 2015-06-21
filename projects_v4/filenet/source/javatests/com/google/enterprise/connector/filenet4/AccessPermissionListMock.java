// Copyright 2013 Google Inc. All Rights Reserved.
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

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.AccessPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class AccessPermissionListMock implements AccessPermissionList {

  private List<AccessPermission> perms;

  public AccessPermissionListMock() {
    this.perms = new ArrayList<AccessPermission>();
  }

  @Override
  public boolean isEmpty() {
    return perms.isEmpty();
  }

  @Override
  public Iterator<AccessPermission> iterator() {
    return perms.iterator();
  }

  @Override
  public boolean add(Object e) {
    return perms.add((AccessPermission) e);
  }

  @Override
  public void add(int index, Object element) {
    perms.add(index, (AccessPermission) element);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean addAll(Collection c) {
    return perms.addAll(c);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean addAll(int index, Collection c) {
    return perms.addAll(index, c);
  }

  @Override
  public void clear() {
    perms.clear();
  }

  @Override
  public boolean contains(Object o) {
    return perms.contains(o);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean containsAll(Collection c) {
    return perms.containsAll(c);
  }

  @Override
  public Object get(int index) {
    return perms.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return perms.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return perms.lastIndexOf(o);
  }

  @Override
  public ListIterator<AccessPermission> listIterator() {
    return perms.listIterator();
  }

  @Override
  public ListIterator<AccessPermission> listIterator(int index) {
    return perms.listIterator(index);
  }

  @Override
  public boolean remove(Object o) {
    return perms.remove(o);
  }

  @Override
  public Object remove(int index) {
    return perms.remove(index);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean removeAll(Collection c) {
    return perms.removeAll(c);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean retainAll(Collection c) {
    return perms.retainAll(c);
  }

  @Override
  public Object set(int index, Object element) {
    return perms.set(index, (AccessPermission) element);
  }

  public void shuffle() {
    Collections.shuffle(perms);
  }

  @Override
  public int size() {
    return perms.size();
  }

  @Override
  public List<AccessPermission> subList(int fromIndex, int toIndex) {
    return perms.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return perms.toArray();
  }

  @Override
  public Object[] toArray(Object[] a) {
    return perms.toArray(a);
  }

}
