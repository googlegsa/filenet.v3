package com.google.enterprise.connector.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileConnector implements Connector {

	private static Logger logger = Logger.getLogger(FileConnector.class.getName());

	public static boolean DEBUG = false;

	public static int DEBUG_LEVEL = 1;

	private String objectFactory;

	private String login;

	private String password;

	private String credTag;

	private String objectStoreName;

	private String pathToWcmApiConfig;

	private String displayUrl;

	private String isPublic;

	static {
		

		File propertiesFile = new File("../config/logging.properties");
		Properties properties = null;
		FileInputStream fileInputStream = null;
		if (propertiesFile.isFile() == true) {
			try {
				fileInputStream = new FileInputStream(propertiesFile);
			} catch (FileNotFoundException e) {
				logger.setLevel(Level.OFF);
			}
			if (fileInputStream != null) {
				properties = new Properties();
				try {
					properties.load(fileInputStream);
					DEBUG = properties.getProperty("DEBUG").equals("true");
					DEBUG_LEVEL = Integer.parseInt(properties
							.getProperty("LEVEL"));
				} catch (IOException e) {
					fileInputStream = null;
					properties = null;
				} finally {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
			}
		}
	}

	public Session login() throws LoginException, RepositoryException {
		Session sess = null;
		if (!(objectFactory == null || login == null || password == null
				|| objectStoreName == null || displayUrl == null)) {

			sess = new FileSession(objectFactory, login, password, 
					credTag, objectStoreName, pathToWcmApiConfig, displayUrl,
					isPublic);
		}
		return sess;

	}

	

	public String getCredTag() {
		return credTag;
	}

	public void setCredTag(String credTag) {
		this.credTag = credTag;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(String objectFactory) {
		this.objectFactory = objectFactory;
	}

	public String getObjectStoreName() {
		return objectStoreName;
	}

	public void setObjectStoreName(String objectStoreName) {
		this.objectStoreName = objectStoreName;
	}

	public String getPathToWcmApiConfig() {
		return pathToWcmApiConfig;
	}

	public void setPathToWcmApiConfig(String pathToWcmApiConfig) {
		this.pathToWcmApiConfig = pathToWcmApiConfig;
	}

	public String getDisplayUrl() {
		return displayUrl;
	}

	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}

	public String getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
	}

}
