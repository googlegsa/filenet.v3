package com.google.enterprise.connector.filenet3.filemockwrap;

import com.google.enterprise.connector.filenet3.filewrap.IProperty;
import com.google.enterprise.connector.mock.MockRepositoryProperty;

public class MockFnProperty implements IProperty {

	MockRepositoryProperty property;

	protected MockFnProperty(MockRepositoryProperty mrProp) {
		this.property = mrProp;
	}

	public String getName() {
		return this.property.getName();
	}

	public String getValueType() {
		return null;
	}

	public Object getValue() {
		return null;
	}
}
