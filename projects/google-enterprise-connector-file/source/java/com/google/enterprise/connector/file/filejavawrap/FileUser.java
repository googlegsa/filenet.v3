package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.User;
import com.google.enterprise.connector.file.filewrap.IUser;

public class FileUser implements IUser {

	User user;
	public FileUser(User user) {
		this.user = user;
		// TODO Auto-generated constructor stub
	}

}
