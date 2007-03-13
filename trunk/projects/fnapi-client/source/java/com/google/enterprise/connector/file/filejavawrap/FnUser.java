package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.User;
import com.google.enterprise.connector.file.filewrap.IUser;

public class FnUser implements IUser {

	User user;

	public FnUser(User user) {
		this.user = user;

	}

	public String getName() {
		return this.user.getName();
	}

}
