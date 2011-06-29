package com.google.enterprise.connector.filenet3.filejavawrap;

import com.filenet.wcm.api.Properties;
import com.filenet.wcm.api.Property;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IProperty;

public class FnProperties implements IProperties {
	private Properties properties;

	public FnProperties(Properties properties) {
		this.properties = properties;

	}

	public IProperty get(int index) {
		return new FnProperty((Property) this.properties.get(index));
	}

	public int size() {
		return this.properties.size();
	}

}
