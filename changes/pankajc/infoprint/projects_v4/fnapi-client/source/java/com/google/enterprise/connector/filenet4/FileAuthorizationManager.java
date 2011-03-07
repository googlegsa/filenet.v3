package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.admin.DocumentClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.core.Factory;
import com.filenet.api.security.MarkingSet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuthorizationManager implements AuthorizationManager {

	IConnection conn;
	IObjectStore objectStore;
	boolean checkMarkings;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileAuthorizationManager.class.getName());
	}

	public FileAuthorizationManager(IConnection conn, IObjectStore objectStore,
			boolean checkMarkings) {
		this.conn = conn;
		this.objectStore = objectStore;
		this.checkMarkings = checkMarkings;
	}

	public Collection authorizeDocids(Collection docids,
			AuthenticationIdentity identity) throws RepositoryException {

		if (null == docids) {
			logger.severe("Got null docids for authZ .. returning null");
			return null;
		}

		List authorizeDocids = new ArrayList();
		List docidList = new ArrayList(docids);
		IVersionSeries versionSeries = null;
		AuthorizationResponse authorizationResponse;

		logger.info("Authorizing docids for user: " + identity.getUsername());

		// authenticate superuser
		// conn.getUserContext().authenticate(objectStore.getSUserLogin(),objectStore.getSUserPassword());
		// check availability of marking set on docyument class

		try {
			DocumentClassDefinition dcd = Factory.DocumentClassDefinition.fetchInstance(this.objectStore.getObjectStore(), GuidConstants.Class_Document, null);
			PropertyDefinitionList pdslist = dcd.get_PropertyDefinitions();
			Iterator<PropertyDefinition> pdsiter = pdslist.iterator();
			boolean hasMarkings = false;

			while (pdsiter.hasNext()) {
				PropertyDefinition pd = pdsiter.next();

				if (pd instanceof PropertyDefinitionString) {
					MarkingSet msl = ((PropertyDefinitionString) pd).get_MarkingSet();
					if (msl != null) {
						logger.log(Level.INFO, "Document class has properties associated with Markings set");
						hasMarkings = true;
						break;
					}
				}
			}
			if (hasMarkings == true) {
				if (this.checkMarkings == true) {
					logger.log(Level.INFO, "Connector is configured to perform marking set's check for authorization");
				} else {
					logger.log(Level.INFO, "Connector is not configured to perform marking set's check for authorization");
				}
			} else {
				logger.log(Level.INFO, "Document class does not have properties associated with Markings set hence; Not perform marking set's check for authorization");
				this.checkMarkings = false;
			}
		} catch (Exception ecp) {
			logger.log(Level.SEVERE, ecp.getStackTrace().toString());
		}

		for (int i = 0; i < docidList.size(); i++) {
			String docId = (String) docidList.get(i);
			try {
				logger.config("Getting version series for document DocID: "
						+ docId);
				versionSeries = (IVersionSeries) objectStore.getObject(ClassNames.VERSION_SERIES, URLDecoder.decode(docId, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.WARNING, "Unable to Decode: Encoding is not supported for the document with ID: "
						+ docId);
				versionSeries = null;
			} catch (RepositoryException e) {
				logger.log(Level.WARNING, "Error : document Version Series Id "
						+ docId + " may no longer exist. Message: "
						+ e.getLocalizedMessage());
				versionSeries = null;
			}

			if (versionSeries != null) {
				logger.config("Authorizing DocID: " + docId + " for user: "
						+ identity.getUsername());
				if (versionSeries.getReleasedVersion().getPermissions().authorize(identity.getUsername())) {
					logger.log(Level.INFO, "As per the ACLS User "
							+ identity.getUsername()
							+ " is authorized for document Id" + docId);
					authorizationResponse = new AuthorizationResponse(true,
							docId);

					if (this.checkMarkings) {
						logger.log(Level.INFO, "Authorizing DocID: " + docId
								+ " for user: " + identity.getUsername()
								+ " for Marking sets ");

						// check whether document has marking set or not //
						if (versionSeries.getReleasedVersion().getActiveMarkings() != null) {
							logger.log(Level.INFO, "Document has properties associated with Markings set");

							authorizationResponse = new AuthorizationResponse(
									versionSeries.getReleasedVersion().getActiveMarkings().authorize(identity.getUsername()),
									docId);
							/*
							 * if
							 * (versionSeries.getReleasedVersion().getActiveMarkings
							 * ().authorize(identity.getUsername())) {
							 * logger.log(Level.INFO, "User " +
							 * identity.getUsername() +
							 * " is authorized for document Id " + docId);
							 * authorizationResponse = new
							 * AuthorizationResponse(
							 * versionSeries.getReleasedVersion
							 * ().getActiveMarkings
							 * ().authorize(identity.getUsername()), docId); }
							 * else { logger.log(Level.INFO, "User " +
							 * identity.getUsername() +
							 * " is NOT authorized for document Id " + docId);
							 * authorizationResponse = new
							 * AuthorizationResponse( false, docId); }
							 */

						} else {
							logger.log(Level.INFO, "Document does not have properties associated with Marking Sets "
									+ docId);
							logger.log(Level.INFO, "User "
									+ identity.getUsername()
									+ " is authorized for document Id " + docId);
							authorizationResponse = new AuthorizationResponse(
									true, docId);
						}
					} else {
						logger.log(Level.INFO, "Either Document class does not have properties associated with Markings set or Connetcor is not configurd to check Marking sets ");
						logger.log(Level.INFO, "User " + identity.getUsername()
								+ " is authorized for document Id " + docId);
						authorizationResponse = new AuthorizationResponse(true,
								docId);
					}
				} else {
					authorizationResponse = new AuthorizationResponse(false,
							docId);
					logger.log(Level.INFO, "As per the ACLS User "
							+ identity.getUsername()
							+ " is NOT authorized for document Id " + docId);
				}
			} else {
				authorizationResponse = new AuthorizationResponse(false, docId);
				logger.log(Level.INFO, "User " + identity.getUsername()
						+ " is NOT authorized for document Id " + docId
						+ "version series null");
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
