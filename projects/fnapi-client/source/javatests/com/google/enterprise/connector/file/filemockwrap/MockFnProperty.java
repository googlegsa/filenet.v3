package com.google.enterprise.connector.file.filemockwrap;

import com.google.enterprise.connector.file.filewrap.IProperty;
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
		// TODO Auto-generated method stub
		return null;
	}
}
