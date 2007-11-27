package com.google.enterprise.connector.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.google.enterprise.connector.file.filewrap.IBaseObject;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;

public class FileAuthorizationManager implements AuthorizationManager {
	IObjectFactory objectFactory;

	IObjectStore objectStore;

	String pathToWcmApiConfig;

	public FileAuthorizationManager(IObjectFactory fileObjectFactory,
			String pathToWcmApiConfig, IObjectStore objectStore) {
		objectFactory = fileObjectFactory;
		this.pathToWcmApiConfig = pathToWcmApiConfig;
		this.objectStore = objectStore;
	}

	public Collection authorizeDocids(Collection docids,
			AuthenticationIdentity username) throws RepositoryException {

		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;
		for (int i = 0; i < docidList.size(); i++) {
			try {
				versionSeries = (IVersionSeries) objectStore.getObject(
						IBaseObject.TYPE_VERSIONSERIES, URLDecoder.decode(
								(String) docidList.get(i), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RepositoryException(e);

			}
			if (versionSeries.getReleasedVersion().getPermissions().asMask(
					username.getUsername()) == 1) {
				authorizationResponse = new AuthorizationResponse(true,
						(String) docidList.get(i));
			} else {
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
