package com.google.enterprise.connector.filenet3.filejavawrap;

import com.filenet.wcm.api.Property;
import com.google.enterprise.connector.filenet3.filewrap.IProperty;

public class FnProperty implements IProperty {
	Property property;

	private static String[] correspondancevalueTypes;

	static {
		correspondancevalueTypes = new String[8];
		correspondancevalueTypes[0] = "Binary";
		correspondancevalueTypes[1] = "Boolean";
		correspondancevalueTypes[2] = "Date";
		correspondancevalueTypes[3] = "Double";
		correspondancevalueTypes[4] = "String";
		correspondancevalueTypes[5] = "Long";
		correspondancevalueTypes[6] = "String";
		correspondancevalueTypes[7] = "String";
	}

	public FnProperty(Property refProperty) {
		this.property = refProperty;
	}

	public String getName() {
		return property.getName();
	}

	public String getValueType() {
		return correspondancevalueTypes[property.getType() - 1];
	}

	public Object getValue() {
		return this.property.getValue();
	}

}
