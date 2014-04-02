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
