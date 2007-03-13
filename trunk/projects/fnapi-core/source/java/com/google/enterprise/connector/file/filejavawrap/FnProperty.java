package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Property;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.spi.ValueType;

public class FnProperty implements IProperty {
	Property property;

	private static ValueType[] correspondancevalueTypes;

	static {
		correspondancevalueTypes = new ValueType[8];
		correspondancevalueTypes[0] = ValueType.BINARY;
		correspondancevalueTypes[1] = ValueType.BOOLEAN;
		correspondancevalueTypes[2] = ValueType.DATE;
		correspondancevalueTypes[3] = ValueType.DOUBLE;
		correspondancevalueTypes[4] = ValueType.STRING;
		correspondancevalueTypes[5] = ValueType.LONG;
		correspondancevalueTypes[6] = ValueType.STRING;
		correspondancevalueTypes[7] = ValueType.STRING;
	}

	public FnProperty(Property property) {
		this.property = property;
	}

	public String getName() {
		return property.getName();
	}

	public ValueType getValueType() {
		return correspondancevalueTypes[property.getType() - 1];
	}

}
