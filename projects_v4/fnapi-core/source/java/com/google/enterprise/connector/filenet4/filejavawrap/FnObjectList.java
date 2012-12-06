package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.Iterator;
import java.util.List;

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;

public class FnObjectList implements IObjectSet {
  List<? extends IBaseObject> objectList;

  public FnObjectList(List<? extends IBaseObject> objectList) {
    super();
    this.objectList = objectList;
  }

  public int getSize() {
    return objectList.size();
  }

  public Iterator getIterator() {
    return objectList.iterator();
  }

}
