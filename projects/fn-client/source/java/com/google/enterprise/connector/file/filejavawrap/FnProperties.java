package com.google.enterprise.connector.file.filejavawrap;

import java.util.Iterator;

import com.filenet.wcm.api.Properties;
import com.filenet.wcm.api.Property;
import com.google.enterprise.connector.file.filewrap.IProperties;
import com.google.enterprise.connector.file.filewrap.IProperty;

public class FnProperties implements IProperties {
	private Properties properties;

	public FnProperties(Properties properties) {
		this.properties = properties;

	}

	public IProperty get(int index) {
		return new FnProperty((Property) this.properties.get(index));
	}

	public Iterator iterator() {

		return this.properties.iterator();
	}

	public int size() {
		return this.properties.size();
	}

}
