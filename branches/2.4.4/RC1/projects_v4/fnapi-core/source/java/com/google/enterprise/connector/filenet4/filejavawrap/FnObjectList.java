package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;

public class FnObjectList implements IObjectSet {
	LinkedList objectList;

	public FnObjectList(LinkedList objectList) {
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
