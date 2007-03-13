package com.google.enterprise.connector.file.filewrap;

import java.util.Iterator;

public interface IProperties {

	public IProperty get(int index);

	public Iterator iterator();

	public int size();
}
