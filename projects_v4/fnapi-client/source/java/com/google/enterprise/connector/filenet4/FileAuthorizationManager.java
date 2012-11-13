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
import com.filenet.api.util.UserContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

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

    // check for the marking sets applied over the document class

    try {
      // In some cases current FileNet connection looses UserContext
      // object associated with it; hence need to fetch userContext for
      // each and every AuthZ request

      Subject subject = this.conn.getSubject();
      UserContext.get().pushSubject(subject);

      DocumentClassDefinition documentClassDefinition = Factory.DocumentClassDefinition.fetchInstance(this.objectStore.getObjectStore(), GuidConstants.Class_Document, null);
      PropertyDefinitionList propertyDefinitionList = documentClassDefinition.get_PropertyDefinitions();
      Iterator<PropertyDefinition> propertyDefinitionIterator = propertyDefinitionList.iterator();
      boolean hasMarkings = false;

      while (propertyDefinitionIterator.hasNext()) {
        PropertyDefinition propertyDefinition = propertyDefinitionIterator.next();

        if (propertyDefinition instanceof PropertyDefinitionString) {
          MarkingSet markingSet = ((PropertyDefinitionString) propertyDefinition).get_MarkingSet();
          if (markingSet != null) {
            logger.log(Level.INFO, "Document class has property associated with Markings set");
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
        logger.log(Level.INFO, "Document class does not have properties associated with Markings set hence; Marking set's check is  not required for authorization");
        this.checkMarkings = false;
      }
    } catch (Exception ecp) {
      logger.log(Level.SEVERE, ecp.getStackTrace().toString());
    }
    // Iterate through the DocId list and authorize the search user. Add the
    // authorization result to
    // AuthorizationResponse list
    for (int i = 0; i < docidList.size(); i++) {
      String docId = (String) docidList.get(i);
      try {
        logger.config("Getting version series for document DocID: "
                + docId);
        versionSeries = (IVersionSeries) objectStore.getObject(ClassNames.VERSION_SERIES, URLDecoder.decode(docId, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        logger.log(Level.WARNING, "Unable to Decode: Encoding is not supported for the document with DocID: "
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
        // Check whether the search user is authorized to view document
        // contents or
        // not.
        if (versionSeries.getReleasedVersion().getPermissions().authorize(identity.getUsername())) {
          logger.log(Level.INFO, "As per the ACLS User "
                  + identity.getUsername()
                  + " is authorized for document DocID " + docId);
          authorizationResponse = new AuthorizationResponse(true,
                  docId);

          if (this.checkMarkings) {
            logger.log(Level.INFO, "Authorizing DocID: " + docId
                    + " for user: " + identity.getUsername()
                    + " for Marking sets ");

            // check whether current document has property values
            // set for properties associated with marking sets or
            // not //
            if (versionSeries.getReleasedVersion().getActiveMarkings() != null) {
              logger.log(Level.INFO, "Document has property associated with Markings set");

              // check whether USER is authorized to view the
              // document as per the Marking set security applied
              // over it.

              if (versionSeries.getReleasedVersion().getActiveMarkings().authorize(identity.getUsername())) {
                logger.log(Level.INFO, "As per the Marking Sets User "
                        + identity.getUsername()
                        + " is authorized for document DocID "
                        + docId);
                authorizationResponse = new AuthorizationResponse(
                        true, docId);
              } else {
                logger.log(Level.INFO, "As per the Marking Sets User "
                        + identity.getUsername()
                        + " is NOT authorized for document DocID "
                        + docId);
                authorizationResponse = new AuthorizationResponse(
                        false, docId);
              }

            } else {
              logger.log(Level.INFO, "Document does not have property associated with Marking Sets "
                      + docId);
              logger.log(Level.INFO, "User "
                      + identity.getUsername()
                      + " is authorized for document DocID "
                      + docId);
              authorizationResponse = new AuthorizationResponse(
                      true, docId);
            }
          } else {
            logger.log(Level.INFO, "Either Document class does not have property associated with Markings set or Connetcor is not configurd to check Marking sets ");
            logger.log(Level.INFO, "User " + identity.getUsername()
                    + " is authorized for document DocID " + docId);
            authorizationResponse = new AuthorizationResponse(true,
                    docId);
          }
        } else {
          authorizationResponse = new AuthorizationResponse(false,
                  docId);
          logger.log(Level.INFO, "As per the ACLS User "
                  + identity.getUsername()
                  + " is NOT authorized for document DocID " + docId);
        }
      } else {
        authorizationResponse = new AuthorizationResponse(false, docId);
        logger.log(Level.INFO, "User " + identity.getUsername()
                + " is NOT authorized for document DocID " + docId
                + "version series null");
      }
      authorizeDocids.add(authorizationResponse);
    }
    UserContext.get().popSubject();
    return authorizeDocids;
  }

  public List authorizeTokens(List tokenList, String username)
          throws RepositoryException {

    return null;
  }
}
