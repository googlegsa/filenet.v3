package com.google.enterprise.connector.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

public class FileSimpleProperty extends SimpleProperty {

	  public FileSimpleProperty(String name, Value value) {
		    super(name,value);
		  }

		  public FileSimpleProperty(String name, List valueList) {
			  super(name,valueList);
		  }

		  public FileSimpleProperty(String name, String value) {
			  super(name,value);
		  }

		  public FileSimpleProperty(String name, boolean value) {
			  super(name,value);
		  }

		  public FileSimpleProperty(String name, long value) {
			  super(name,value);
		  }

		  public FileSimpleProperty(String name, double value) {
			  super(name,value);
		  }
	
		 /* (non-Javadoc)
		   * 
		   * @see com.google.enterprise.connector.spi.Property#getValues()
		   */
		  public Iterator getValues() throws RepositoryException {
		   
		    List l = new ArrayList(1);
		    l.add(super.getValue());
		    return l.iterator();
		  }

}
