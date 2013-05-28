// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.filewrap.IPermissions;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.GroupSet;
import com.filenet.api.collection.UserSet;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Factory;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class over the FileNet API class Permissions. This class is
 * responsible to authorize a target user against all the Access Control Entries
 * of a target document.
 *
 * @author pankaj_chouhan
 */
@SuppressWarnings("rawtypes")
public class FnPermissions implements IPermissions {
  private static final Logger LOGGER =
      Logger.getLogger(FnDocument.class.getName());

  private AccessPermissionList perms;
  private int ACCESS_LEVEL = AccessLevel.VIEW_AS_INT;
  private final String AUTHENTICATED_USERS = "#AUTHENTICATED-USERS";
  private String ACTIVE_DIRECTORY_SYMBOL = "@";
  private int ACCESS_OBJECT_LEVEL = AccessRight.USE_MARKING_AS_INT;
  private PropertyFilter pf = new PropertyFilter();

  public FnPermissions(AccessPermissionList perms) {
    this.perms = perms;
    setPropertyFilter();
  }

  private void setPropertyFilter() {
    pf.addIncludeProperty(new FilterElement(null, null, null,
            PropertyNames.EMAIL, null));
    pf.addIncludeProperty(new FilterElement(null, null, null,
            PropertyNames.SHORT_NAME, null));
    pf.addIncludeProperty(new FilterElement(null, null, null,
            PropertyNames.NAME, null));
    pf.addIncludeProperty(new FilterElement(null, null, null,
            PropertyNames.DISTINGUISHED_NAME, null));
  }

  /**
   * To authorize a given username against the grantee-names, present in all
   * the Access Control Entries for all the permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of
   *         authorization.
   */
  public boolean authorize(String username) {
    boolean found;
    boolean accessLevelFailure = true;
    Iterator iter = perms.iterator();
    User currentUser = null;
    String granteeName;
    String granteeType;
    LOGGER.log(Level.FINE, "Authorizing user:[" + username + "]");

    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();
        Integer accessMask = perm.get_AccessMask();
        LOGGER.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

        // Compare to make sure that the access level, to user for a
        // document, is atleast view or above
        if ((accessMask & ACCESS_LEVEL) == ACCESS_LEVEL) {
          accessLevelFailure = false;
          granteeName = perm.get_GranteeName();
          LOGGER.log(Level.FINEST, "Grantee Name is:[" + granteeName
                  + "]");
          granteeType = perm.get_GranteeType().toString();
          LOGGER.log(Level.FINEST, "Grantee Type is:[" + granteeType
                  + "]");
          if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
            LOGGER.log(Level.INFO, "Grantee Name is ["
                    + granteeName
                    + "] is of type USER");
            currentUser = Factory.User.fetchInstance(perm.getConnection(), granteeName, pf);

            // compare username with complete granteeName or
            // shortName
            // of the grantee
            if (granteeName.equalsIgnoreCase(username)) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + granteeName
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if (getShortName(granteeName) != null
                    && (getShortName(granteeName).equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + getShortName(granteeName)
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if ((granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0] != null)
                    && (granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0])
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if ((FileUtil.getShortName(granteeName) != null)
                    && (FileUtil.getShortName(granteeName).equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (FileUtil.getShortName(granteeName))
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if (((currentUser.get_Name() != null))
                    && (currentUser.get_Name().equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (currentUser.get_Name())
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if ((currentUser.get_Email() != null)
                    && (currentUser.get_Email().equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (currentUser.get_Email())
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if ((currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0] != null)
                    && (currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0])
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if ((getShortName(currentUser.get_Name()) != null)
                    && (getShortName(currentUser.get_Name()).equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (getShortName(currentUser.get_Name()))
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if (((currentUser.get_ShortName() != null))
                    && (currentUser.get_ShortName().equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (currentUser.get_ShortName())
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else if (((currentUser.get_DistinguishedName() != null))
                    && (currentUser.get_DistinguishedName().equalsIgnoreCase(username))) {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + (currentUser.get_DistinguishedName())
                      + "] matches with search USER [" + username
                      + "]");
              return true;
            } else {
              LOGGER.log(Level.INFO, "Grantee Name ["
                      + granteeName
                      + "] does not match with search user ["
                      + username
                      + "]. Authorization will continue with the next Grantee Name");
            }
          } else if (perm.get_GranteeType() == SecurityPrincipalType.GROUP) { // GROUP
            LOGGER.log(Level.INFO, "Grantee Name [" + granteeName
                    + "] is of type GROUP");
            if (granteeName.equalsIgnoreCase(AUTHENTICATED_USERS)) {
              // #AUTHENTICATED-USERS is a logical group in
              // FileNet P8
              // Systems, which gets automatically
              // created at the time of FileNet installation. This
              // group contains all the FileNet users.
              // This group cannot be edited by admin i.e. it is
              // not
              // possible to delete the user from
              // this group. Thus every FileNet user is a part of
              // #AUTHENTICATED-USERS group. This is the
              // reason to authenticate a user for a document, if
              // that
              // document contains #AUTHENTICATED-USERS
              // group in its ACL or ACE.
              LOGGER.log(Level.INFO, "Authorization for user: ["
                      + username + "] is Successful");
              return true;
            } else {
              Connection conn = perm.getConnection();
              Group group = null;
              try {
                group = com.filenet.api.core.Factory.Group.fetchInstance(conn, perm.get_GranteeName(), null);
                found = searchUserInGroup(username, group);
                if (found) {
                  LOGGER.log(Level.INFO, "Authorization for user: ["
                          + username + "] is Successful");
                  return true;
                }
              } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Skipping Group ["
                        + granteeName
                        + "], as it is not found.", e);
              }
            }
          }
        }
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING, "Exception occured in authorizing user against permissions. "
                + ecp.getMessage(), ecp);
      }
    }

    if (accessLevelFailure) {
      // If the document have no view content or more Access Security
      // Level to any user
      LOGGER.log(Level.WARNING, "Authorization for user: ["
              + username
              + "] FAILED due to insufficient Access Security Levels. Minimum expected Access Security Level is \"View Content\"");
    } else {
      LOGGER.log(Level.WARNING, "Authorization for user: ["
              + username
              + "] FAILED. Probable reason: ["
              + username
              + "] does not have sufficient security access rights and hence not listed as one of authorized users");
    }
    return false;
  }

  /**
   * To check, a given user has at least USE right or above, over all the
   * marking permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of check for
   *         USE right.
   * @see com.google.enterprise.connector.filenet4.filewrap.IPermissions#authorizeMarking(java.lang.String)
   */

  public boolean authorizeMarking(String username) {

    String granteeName;
    Iterator iter = perms.iterator();
    boolean hasUseRights = false;

    LOGGER.log(Level.FINE, "Checking user : [" + username
            + "] For USE right over the Document");

    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();

        LOGGER.log(Level.INFO, "Checking user : [" + username
                + "] For USE right over the Document with new Grantee");

        if (checkGranteeName(perm, username)) {

          LOGGER.log(Level.FINEST, "Access Type is:["
                  + perm.get_AccessType() + "]");

          LOGGER.log(Level.FINEST, "Access Mask is:["
                  + perm.get_AccessMask() + "]");

          if (perm.get_AccessType().equals(AccessType.ALLOW)) {
            // Check whether the search user has USE Right over the
            // document or not.
            if ((perm.get_AccessMask() & ACCESS_OBJECT_LEVEL) == ACCESS_OBJECT_LEVEL) {
              // LOGGER.log(Level.FINE, " User: [" + username
              // + "] has USE right over the document ");
              hasUseRights = true;
              return true;
            } else {
              LOGGER.log(Level.FINE, " User: ["
                      + username
                      + "] does not have USE right over the document");
              hasUseRights = false;
            }
          } else {
            LOGGER.log(Level.FINE, " User: [" + username
                    + "] does not have USE right over the document");
            hasUseRights = false;
          }
        }

        // Check whether the Grantee Name and search username is same or
        // not. If not then check the next AccessPermission.
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING, "Exception occured in authorizing user against permissions. "
                + ecp.getMessage(), ecp);
      }
    }

    if (hasUseRights == true) {
      LOGGER.log(Level.INFO, " User: [" + username
              + "] has USE right over the document ");
      return true;
    } else {
      LOGGER.log(Level.WARNING, " User: [" + username
              + "] does not have USE right over the document");
      return false;
    }

  }

  /**
   * To check, a given user is in the list of Grantee of the given permission
   * object.
   *
   * @param Object of the AccessPermission, Search user name.
   * @return True or False, depending on the success or failure of check for
   *         grantee name check.
   * @throws Exception
   * @see com.google.enterprise.connector.filenet4.filewrap.IPermissions#checkGranteeName(AccessPermission,java.lang.String)
   */

  private boolean checkGranteeName(AccessPermission perm, String username)
          throws Exception {

    String granteeName = perm.get_GranteeName();
    boolean found = false;
    User currentUser = null;

    LOGGER.log(Level.INFO, "Matching the Grantee Name [" + granteeName
            + "] with search USER : [" + username + "]");

    // check the type of the Grantee (USER or GROUP) for the given
    // permission
    try {

      if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
        LOGGER.log(Level.FINE, "Grantee Name is [" + granteeName
                + "] is of type USER");

        currentUser = Factory.User.fetchInstance(perm.getConnection(), granteeName, pf);
        // Match the full Grantee Name and search user name.

        if (granteeName.equalsIgnoreCase(username)) {
          LOGGER.log(Level.INFO, "Grantee Name [" + granteeName
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if (getShortName(granteeName) != null
                && (getShortName(granteeName).equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + getShortName(granteeName)
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if ((granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0] != null)
                && (granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0])
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if ((FileUtil.getShortName(granteeName) != null)
                && (FileUtil.getShortName(granteeName).equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (FileUtil.getShortName(granteeName))
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if (((currentUser.get_Name() != null))
                && (currentUser.get_Name().equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (currentUser.get_Name())
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if ((currentUser.get_Email() != null)
                && (currentUser.get_Email().equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (currentUser.get_Email())
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if ((currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0] != null)
                && (currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (currentUser.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0])
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if ((getShortName(currentUser.get_Name()) != null)
                && (getShortName(currentUser.get_Name()).equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (getShortName(currentUser.get_Name()))
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if (((currentUser.get_ShortName() != null))
                && (currentUser.get_ShortName().equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (currentUser.get_ShortName())
                  + "] matches with search USER [" + username + "]");
          return true;
        } else if (((currentUser.get_DistinguishedName() != null))
                && (currentUser.get_DistinguishedName().equalsIgnoreCase(username))) {
          LOGGER.log(Level.INFO, "Grantee Name ["
                  + (currentUser.get_DistinguishedName())
                  + "] matches with search USER [" + username + "]");
          return true;
        } else {
          LOGGER.log(Level.WARNING, "Grantee Name ["
                  + granteeName
                  + "] does not match with search user ["
                  + username
                  + "]. Checking will continue with the next Grantee Name");
        }
      } else if (perm.get_GranteeType() == SecurityPrincipalType.GROUP) { // GROUP
        LOGGER.log(Level.FINE, "Grantee Name [" + granteeName
                + "] is of type GROUP");

        // Check whether the search user is member of
        // #AUTHENTICATED-USERS
        // Group. If not then search username in the group.

        if (granteeName.equalsIgnoreCase(AUTHENTICATED_USERS)) {

          LOGGER.log(Level.INFO, "Grantee [" + granteeName
                  + "] contains search USER [" + username + "]");
          return true;

        } else {
          Connection conn = perm.getConnection();
          Group group = null;
          try {
            group = com.filenet.api.core.Factory.Group.fetchInstance(conn, perm.get_GranteeName(), null);
            found = searchUserInGroup(username, group);
            if (found) {
              LOGGER.log(Level.INFO, "Grantee [" + granteeName
                      + "] contains search USER [" + username
                      + "]");
              return true;
            }
          } catch (Exception e) {
            LOGGER.log(Level.FINE, "Skipping Group [" + granteeName
                    + "], as it is not found." + e.getMessage());
          }
        }
      }
    } catch (Exception ecp) {
      LOGGER.log(Level.WARNING, ecp.getMessage(), ecp);
      throw ecp;
    }
    return false;
  }

  /**
   * To return short name of the Search user name provided as a parameter.
   *
   * @param Search user name.
   * @return short name of the Search user name provided as a parameter.
   * @see com.google.enterprise.connector.filenet4.filewrap.IPermissions#getShortName(java.lang.String)
   */

  private String getShortName(String longName) {
    StringTokenizer strtok = new StringTokenizer(longName, ",");
    String shortUserName = null;
    if (strtok.countTokens() > 1) {
      while ((null != strtok) && (strtok.hasMoreTokens())) {

        String mytok1 = strtok.nextToken();
        if (null != mytok1) {
          // filter for the shortened name
          StringTokenizer innerToken = new StringTokenizer(mytok1,
                  "=");
          if ((null != innerToken) && (innerToken.countTokens() == 2)) {
            String key = innerToken.nextToken();
            if (null != key) {
              if ((key.equalsIgnoreCase("cn"))
                      || (key.equalsIgnoreCase("uid"))) {
                shortUserName = innerToken.nextToken();
                break;
              }
            }
          }
        } // end:if (null!=mytok1) {
      } // end: while
    } else if (longName.contains("@")) {
      shortUserName = longName.substring(0, longName.indexOf("@"));
    }
    return shortUserName;
  }

  /*
   * It is a recursive function which will search a target user in the
   * provided group. If the group contains some other group then it
   * recursively searches the target user in those groups as well.
   */
  private boolean searchUserInGroup(String username, Group group) {
    User user;
    Group subGroup;
    boolean found;
    UserSet us = group.get_Users();
    Iterator itUser = us.iterator();
    while (itUser.hasNext()) {
      user = (User) itUser.next();
      LOGGER.log(Level.FINER, "Searching the USER [" + user.get_Name()
              + "][" + user.get_Email() + "] in GROUP ["
              + group.get_Name() + "]");

      if ((user.get_Name() != null)
              && (user.get_Name().equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      } else if ((((user.get_DistinguishedName()) != null))
              && (user.get_DistinguishedName().equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      } else if (((user.get_Email() != null))
              && (user.get_Email().equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      } else if ((user.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0] != null)
              && (user.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      } else if (((FileUtil.getShortName(user.get_Name()) != null))
              && (FileUtil.getShortName(user.get_Name()).equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      } else if ((user.get_ShortName() != null)
              && (user.get_ShortName().equalsIgnoreCase(username))) {
        LOGGER.log(Level.FINE, "Search USER [" + username
                + "] found in GROUP [" + group.get_Name() + "]");
        return true;
      }
    }
    GroupSet gs = group.get_Groups();
    Iterator itGroup = gs.iterator();
    while (itGroup.hasNext()) {
      subGroup = (Group) itGroup.next();
      found = searchUserInGroup(username, subGroup);
      if (found)
        return true;
    }
    return false;
  }
}
