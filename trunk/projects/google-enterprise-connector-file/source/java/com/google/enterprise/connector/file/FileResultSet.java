package com.google.enterprise.connector.file;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.google.enterprise.connector.spi.ResultSet;

public class FileResultSet extends LinkedList implements ResultSet{
	
	private static final long serialVersionUID = 1L;

	public FileResultSet(){
		super();
	}
	
	public FileResultSet(Collection co){
		super(co);
	}
	
	public Iterator iterator(){
		ListIterator iterator = listIterator(0);
		return iterator;
	}

}
