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

package com.google.enterprise.connector.filenet4.api;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FnSearch implements ISearch {
  private static final Logger logger =
      Logger.getLogger(FnSearch.class.getName());

  private final SearchScope search;

  public FnSearch(SearchScope search) {
    this.search = search;
  }

  @Override
  public IndependentObjectSet execute(String query, int pageSize,
      int maxRecursion) {
    logger.log(Level.FINEST, "Execute query: {0}", query);

    SearchSQL sqlObject = new SearchSQL();
    sqlObject.setQueryString(query);

    PropertyFilter myFilter;
    if (maxRecursion > 0) {
      myFilter = new PropertyFilter();
      myFilter.setMaxRecursion(1);
    } else {
      myFilter = null;
    }

    return search.fetchObjects(sqlObject, pageSize, myFilter, Boolean.TRUE);
  }

  @Override
  public IndependentObjectSet execute(String query) {
    return execute(query, 100, 1);
  }
}
