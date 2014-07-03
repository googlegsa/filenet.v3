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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class FnSearch implements ISearch {
  private static final Logger logger =
      Logger.getLogger(FnSearch.class.getName());

  private final SearchScope search;

  public FnSearch(SearchScope search) {
    this.search = search;
  }

  @Override
  public IObjectSet execute(String query) throws RepositoryException {
    LinkedList<FnBaseObject> objectList = new LinkedList<FnBaseObject>();
    IndependentObjectSet myObjects;

    SearchSQL sqlObject = new SearchSQL();
    sqlObject.setQueryString(query);

    Integer myPageSize = new Integer(100);

    PropertyFilter myFilter = new PropertyFilter();
    int myFilterLevel = 1;

    myFilter.setMaxRecursion(myFilterLevel);

    Boolean continuable = new Boolean(true);

    try {
      myObjects = search.fetchObjects(sqlObject, myPageSize, myFilter,
          continuable);
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
    Iterator it = myObjects.iterator();
    while (it.hasNext()) {
      objectList.add(new FnBaseObject((IndependentObject) it.next()));
    }
    return new FnObjectList(objectList);
  }

}
