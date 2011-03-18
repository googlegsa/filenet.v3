package com.google.enterprise.connector.filenet4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.filenet.api.constants.ClassNames;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
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

	public Collection authorizeDocids(Collection docids,AuthenticationIdentity identity) throws RepositoryException {

		if(null==docids){
			logger.severe("Got null docids for authZ .. returning null");
			return null;
		}
			
		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;
	
		logger.info("Authorizing docids for user: "+identity.getUsername());
		
		//authenticate superuser
//		conn.getUserContext().authenticate(objectStore.getSUserLogin(),objectStore.getSUserPassword());

		for (int i = 0; i < docidList.size(); i++) {
			String docId = (String) docidList.get(i);
			try {
				logger.config("Getting version series for document DocID: "+docId);
				versionSeries = (IVersionSeries) objectStore.getObject(ClassNames.VERSION_SERIES, URLDecoder.decode(docId, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.WARNING, "Unable to Decode: Encoding is not supported for the document with ID: " + docId);
				versionSeries = null;				
			} catch (RepositoryException e) {
				logger.log(Level.WARNING, "Error : document Version Series Id "+docId+" may no longer exist. Message: "+e.getLocalizedMessage());
				versionSeries = null;
			}
			
			if (versionSeries != null){
				logger.config("Authorizing DocID: "+docId+" for user: "+identity.getUsername());
				if (versionSeries.getReleasedVersion().getPermissions().authorize(identity.getUsername())) {
					authorizationResponse = new AuthorizationResponse(true, docId);
					logger.log(Level.INFO, "User " + identity.getUsername()+ " is authorized for document Id " + docId);
				} else {
					authorizationResponse = new AuthorizationResponse(false, docId);
					logger.log(Level.INFO, "User " + identity.getUsername() + " is NOT authorized for document Id " + docId);
				}
			}else{
				authorizationResponse = new AuthorizationResponse(false, docId);
				logger.log(Level.INFO, "User " + identity.getUsername() + " is NOT authorized for document Id " + docId);
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
