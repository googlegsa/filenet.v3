package com.google.enterprise.connector.filenet3.filejavawrap;

import com.filenet.wcm.api.User;
import com.google.enterprise.connector.filenet3.filewrap.IUser;

public class FnUser implements IUser {

	User user;

	public FnUser(User user) {
		this.user = user;

	}

	public String getName() {
		return this.user.getName();
	}

}
