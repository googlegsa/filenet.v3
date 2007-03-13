package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.ValueType;

public interface IProperty {
	
	public String getName();

	public ValueType getValueType();
}
