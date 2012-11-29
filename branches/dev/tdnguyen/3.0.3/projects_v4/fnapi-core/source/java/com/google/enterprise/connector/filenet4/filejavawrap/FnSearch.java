package com.google.enterprise.connector.filenet4.filejavawrap;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.google.enterprise.connector.filenet4.FileTraversalManager;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;

public class FnSearch implements ISearch {

  SearchScope search;

  private static Logger logger;

  private static String dateFirstPush ;

  static {
    logger = Logger.getLogger(FnSearch.class.getName());
  }
  public FnSearch(SearchScope search) {
    this.search = search;
  }

  public IObjectSet execute(String query) throws RepositoryException {
    LinkedList objectList = new LinkedList();
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
