package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.filenet3.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet3.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.ISession;
import com.google.enterprise.connector.filenet3.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuthorizationManager implements AuthorizationManager {
	IObjectFactory objectFactory;

	IObjectStore objectStore;

	String pathToWcmApiConfig;

	ISession session;

	private static Logger LOGGER = Logger.getLogger(FileAuthorizationManager.class.getName());

	public FileAuthorizationManager(IObjectFactory reffileObjectFactory,
			String refPathToWcmApiConfig, IObjectStore refObjectStore,
			ISession refSession) {
		this.objectFactory = reffileObjectFactory;
		this.pathToWcmApiConfig = refPathToWcmApiConfig;
		this.objectStore = refObjectStore;
		this.session = refSession;
	}

	/***
	 * To authorize a given username against the grantee-names, present in all
	 * the Access Control Entries for all the permission of the target document.
	 * 
	 * @param (java.util.Collection<String> List of Document IDs to authorize,
	 *        com.google.enterprise.connector.spi.AuthenticationIdentity
	 *        Search_user_identity)
	 * @return Collection of AuthorizationResponse objects(True or
	 *         False,DocumentID), depending on the success or failure of
	 *         authorization.
	 * @see com.google.enterprise.connector.spi.AuthorizationManager#authorizeDocids(java.util.Collection,
	 *      com.google.enterprise.connector.spi.AuthenticationIdentity)
	 */
	public Collection authorizeDocids(Collection docids,
			AuthenticationIdentity username) throws RepositoryException {
		LOGGER.log(Level.FINEST, "Entering into authorizeDocids(Collection docids, AuthenticationIdentity username)");

		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;

		for (int i = 0; i < docidList.size(); i++) {
			String docId = (String) docidList.get(i);
			LOGGER.log(Level.FINE, "Check Authorization for document ID "
					+ docId);
			try {
				versionSeries = (IVersionSeries) objectStore.getObject(IBaseObject.TYPE_VERSIONSERIES, URLDecoder.decode(docId, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LOGGER.log(Level.WARNING, "Unable to Decode: Encoding is not supported for the document with ID: "
						+ docId);
				throw new RepositoryException(e);
			}

			if (versionSeries.getReleasedVersion().getPermissions(session.getSession()).authorize(username.getUsername())) {
				LOGGER.log(Level.FINE, "User: " + username.getUsername()
						+ " is authorized for document ID " + docId);
				authorizationResponse = new AuthorizationResponse(true, docId);
			} else {
				LOGGER.fine("User: " + username.getUsername()
						+ " is NOT authorized for document ID " + docId);
				authorizationResponse = new AuthorizationResponse(false, docId);

			}
			authorizeDocids.add(authorizationResponse);
		}

		LOGGER.log(Level.FINEST, "Exiting from authorizeDocids(Collection docids, AuthenticationIdentity username)");
		return authorizeDocids;
	}

	public List authorizeTokens(List tokenList, String username)
			throws RepositoryException {

		return null;
	}

}
