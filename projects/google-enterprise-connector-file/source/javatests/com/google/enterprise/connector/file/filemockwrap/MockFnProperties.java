package com.google.enterprise.connector.file.filemockwrap;

import java.util.Iterator;
import java.util.Vector;

import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;

public class MockFnProperties implements IProperties {

	Vector mrPL;

	int size = 0;

	protected MockFnProperties(MockRepositoryPropertyList propLst) {
		Iterator it = propLst.iterator();
		while (it.hasNext()) {
			this.mrPL.add(it.next());
			size++;
		}
	}

	public IProperty get(int index) {
		return new MockFnProperty((MockRepositoryProperty) mrPL.get(index));
	}

	public Iterator iterator() {
		return this.mrPL.iterator();
	}

	public int size() {
		return this.size;
	}

}
