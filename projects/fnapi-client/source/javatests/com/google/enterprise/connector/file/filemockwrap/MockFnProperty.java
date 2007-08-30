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

	// public ValueType getValueType() {
	// if (this.property.getType() == MockRepositoryProperty.PropertyType.DATE)
	// {
	// return ValueType.DATE;
	// } else if (this.property.getType() ==
	// MockRepositoryProperty.PropertyType.INTEGER) {
	// return ValueType.LONG;
	// } else if (this.property.getType() ==
	// MockRepositoryProperty.PropertyType.STRING) {
	// return ValueType.STRING;
	// } else if (this.property.getType() ==
	// MockRepositoryProperty.PropertyType.UNDEFINED) {
	// return ValueType.BINARY;// Not sure that it makes sense. TODO test.
	// } else {
	// return null;
	// }
	// }

}
