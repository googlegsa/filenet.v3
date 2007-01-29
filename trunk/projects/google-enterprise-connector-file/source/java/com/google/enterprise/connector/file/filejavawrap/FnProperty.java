package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Property;
import com.google.enterprise.connector.file.filewrap.IProperty;
import com.google.enterprise.connector.spi.ValueType;

public class FnProperty implements IProperty {
	Property property;

	private static String[] valueTypes;

	static {
		valueTypes = new String[8];
		valueTypes[0] = "binary";
		valueTypes[1] = "BOOLEAN";
		valueTypes[2] = "DATE";
		valueTypes[3] = "DOUBLE";
		valueTypes[4] = "ID";
		valueTypes[5] = "LONG";
		valueTypes[6] = "OBJECT";
		valueTypes[7] = "STRING";
	}

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
