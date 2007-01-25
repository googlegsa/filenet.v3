package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileAuthenticationManager implements AuthenticationManager {
	IObjectFactory objectFactory;

	String wcmConfigFilePath;

	public FileAuthenticationManager(IObjectFactory object, String wcm) {
		objectFactory = object;
		wcmConfigFilePath = wcm;
	}

	public boolean authenticate(String username, String password)
			throws LoginException, RepositoryException {

		System.out.println("FileAuthentication method authenticate");
		
		ISession sess = objectFactory.getSession("gsa-authenticate", null,
				username, password);
		try {
			sess.setConfiguration(new FileInputStream(wcmConfigFilePath));
			IUser user = sess.verify();
			if (user == null) {
				return false;
			}

		} catch (FileNotFoundException e) {
			RepositoryException re = new LoginException(e.getMessage(), e
					.getCause());
			re.setStackTrace(e.getStackTrace());
			throw re;
		}
		System.out.println("FileAuthentication method authenticate send true");
		return true;
	}

}
