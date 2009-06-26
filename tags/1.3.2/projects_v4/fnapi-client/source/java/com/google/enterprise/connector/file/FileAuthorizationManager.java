package com.google.enterprise.connector.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.filenet.api.constants.ClassNames;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;

public class FileAuthorizationManager implements AuthorizationManager {

	IConnection conn;

	IObjectStore objectStore;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthorizationManager.class.getName());
	}

	public FileAuthorizationManager(IConnection conn, IObjectStore objectStore) {
		this.conn = conn;
		this.objectStore = objectStore;
	}

	public Collection authorizeDocids(Collection docids,
			AuthenticationIdentity identity) throws RepositoryException {

		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;
	
		//authenticate superuser
		conn.getUserContext().authenticate(objectStore.getSUserLogin(),objectStore.getSUserPassword());

		for (int i = 0; i < docidList.size(); i++) {
			String docId = (String) docidList.get(i);
			try {
				versionSeries = (IVersionSeries) objectStore.getObject(
						ClassNames.VERSION_SERIES, URLDecoder.decode(
								docId, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.warning("docVsId " + docId+ " UnsupportedEncodingException");
				versionSeries = null;
				
			}
			catch (RepositoryException e) {
				logger.info("Error : docVsId "+docId+" may no longer exist\nMessage: "+e.getLocalizedMessage());
				versionSeries = null;
			}
			
			if (versionSeries != null){
				if (versionSeries.getReleasedVersion().getPermissions().authorize(identity.getUsername())) {
					authorizationResponse = new AuthorizationResponse(true, docId);
					logger.fine("User " + identity.getUsername()
							+ " authorized for docVsId " + docId);
				} else {
					authorizationResponse = new AuthorizationResponse(false, docId);
					logger.fine("User " + identity.getUsername()
							+ " not authorized for docVsId " + docId);
				}
			}else{
				authorizationResponse = new AuthorizationResponse(false, docId);
				logger.fine("User " + identity.getUsername()
						+ " not authorized for docVsId " + docId);
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
