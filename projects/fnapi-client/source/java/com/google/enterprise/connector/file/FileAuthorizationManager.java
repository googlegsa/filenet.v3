package com.google.enterprise.connector.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;

public class FileAuthorizationManager implements AuthorizationManager {
	IObjectFactory objectFactory;

	IObjectStore objectStore;

	String pathToWcmApiConfig;
	
	ISession session;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthorizationManager.class.getName());
	}
	
	public FileAuthorizationManager(IObjectFactory fileObjectFactory,
			String pathToWcmApiConfig, IObjectStore objectStore,ISession session) {
		objectFactory = fileObjectFactory;
		this.pathToWcmApiConfig = pathToWcmApiConfig;
		this.objectStore = objectStore;
		this.session = session;
	}

	public Collection authorizeDocids(Collection docids,
			AuthenticationIdentity username) throws RepositoryException {

		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;
		for (int i = 0; i < docidList.size(); i++) {
			logger.fine("check authorization for doc "+docidList.get(i));
			try {
				versionSeries = (IVersionSeries) objectStore.getObject(
						IBaseObject.TYPE_VERSIONSERIES, URLDecoder.decode(
								(String) docidList.get(i), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RepositoryException(e);

			}
			
			if (versionSeries.getReleasedVersion().getPermissions(session.getSession()).authorize(
					username.getUsername())) {
				logger.fine("user: "+username.getUsername()+ " authorized for doc "+docidList.get(i));
				authorizationResponse = new AuthorizationResponse(true,
						(String) docidList.get(i));
			} else {
				logger.fine("user: "+username.getUsername()+ " NOT authorized for doc "+docidList.get(i));
				authorizationResponse = new AuthorizationResponse(false,
						(String) docidList.get(i));

			}
			authorizeDocids.add(authorizationResponse);
		}

		return authorizeDocids;
	}

	public List authorizeTokens(List tokenList, String username)
			throws RepositoryException {

		return null;
	}

}
