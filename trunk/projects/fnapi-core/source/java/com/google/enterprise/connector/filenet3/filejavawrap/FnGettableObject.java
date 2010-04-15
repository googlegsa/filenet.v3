package com.google.enterprise.connector.filenet3.filejavawrap;

import com.filenet.wcm.api.GettableObject;
import com.google.enterprise.connector.filenet3.filewrap.IGettableObject;

public class FnGettableObject implements IGettableObject {

	private GettableObject gettableObject;

	public FnGettableObject(GettableObject object) {
		this.gettableObject = object;
	}

	public String getId() {
		return this.gettableObject.getId();
	}

}
