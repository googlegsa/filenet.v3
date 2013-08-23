// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IUser;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.security.Group;

import java.util.Iterator;
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

  private static final String AUTHENTICATED_USERS = "#AUTHENTICATED-USERS";
  private static final String CREATOR_OWNER = "#CREATOR-OWNER";

  private int ACCESS_LEVEL = AccessLevel.VIEW_AS_INT;
  private int ACCESS_OBJECT_LEVEL = AccessRight.USE_MARKING_AS_INT;

  private final AccessPermissionList perms;
  private final String owner;
  private final PropertyFilter pf;
  
  public FnPermissions(AccessPermissionList perms, String owner) {
    this.perms = perms;
    this.owner = owner;
    this.pf = new PropertyFilter();
    setPropertyFilter();
  }

  public FnPermissions(AccessPermissionList perms) {
    this(perms, null);
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
  public boolean authorize(IUser user) {
    boolean accessLevelFailure = true;
    Iterator iter = perms.iterator();

    LOGGER.log(Level.FINE, "Authorizing user:[" + user.getName() + "]");

    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();
        Integer accessMask = perm.get_AccessMask();
        LOGGER.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

        // Compare to make sure that the access level, to user for a
        // document, is at least view or above
        if ((accessMask & ACCESS_LEVEL) == ACCESS_LEVEL) {
          accessLevelFailure = false;
          if (matchesUser(perm, user)) {
            return true;
          }
        }
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING,
            "Exception occured in authorizing user against permissions. "
            + ecp.getMessage(), ecp);
      }
    }

    if (accessLevelFailure) {
      // If the document have no view content or more Access Security
      // Level to any user
      LOGGER.log(Level.WARNING, "Authorization for user: ["
          + user.getName()
          + "] FAILED due to insufficient Access Security Levels. "
          + "Minimum expected Access Security Level is \"View Content\"");
    } else {
      LOGGER.log(Level.WARNING, "Authorization for user: ["
          + user.getName()
          + "] FAILED.  Probable reason: ["
          + user.getName()
          + "] does not have sufficient security access rights"
          + " and hence not listed as one of authorized users");
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

  public boolean authorizeMarking(IUser user) {
    Iterator iter = perms.iterator();
    boolean hasUseRights = false;

    LOGGER.log(Level.FINE, "Checking user : [" + user.getName()
            + "] For USE right over the Document");

    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();
        LOGGER.log(Level.INFO, "Checking user : [" + user.getName()
            + "] For USE right over the Document with new Grantee");

        if (matchesUser(perm, user)) {
          LOGGER.log(Level.FINEST, "Access Type: [{0}], Mask: {1}",
              new Object[] {perm.get_AccessType(), perm.get_AccessMask()});

          if (perm.get_AccessType().equals(AccessType.ALLOW)) {
            // Check whether the search user has USE Right over the
            // document or not.
            if ((perm.get_AccessMask() & ACCESS_OBJECT_LEVEL) == ACCESS_OBJECT_LEVEL) {
              // LOGGER.log(Level.FINE, " User: [" + username
              // + "] has USE right over the document ");
              hasUseRights = true;
              break;
            }
          }
        }
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING, "Exception occured in authorizing user against permissions. "
                + ecp.getMessage(), ecp);
      }
    }

    if (hasUseRights == true) {
      LOGGER.log(Level.INFO, "User: [" + user.getName()
          + "] has USE right over the document ");
      return true;
    } else {
      LOGGER.log(Level.INFO,
          "User: [{0}] does not have USE right over the document",
          user.getName());
      return false;
    }

  }

  private boolean matchesAnyString(String name, String... otherNames) {
    for (String otherName : otherNames) {
      if (name.equalsIgnoreCase(otherName)) {
        return true;
      }
    }
    return false;
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

  private boolean matchesUser(AccessPermission perm, IUser user) {
    String granteeName = perm.get_GranteeName();
    String granteeType = perm.get_GranteeType().toString();
    LOGGER.log(Level.FINER, "Grantee Name: [{0}], type: {1}",
        new Object[] {granteeName, granteeType});

    if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
      if (granteeName.equalsIgnoreCase(CREATOR_OWNER)) {
        if (owner != null && matchesAnyString(owner, user.getName(),
            user.getEmail(), user.getDistinguishedName())) {
          LOGGER.log(Level.FINER,
              "Authorization: [{0}] is authorized as the creator owner",
              user.getName());
          return true;
        }
        LOGGER.log(Level.FINER, "Authorization: [{0}] is not {1} owner",
            new Object[] {user.getName(), owner});
        return false;
      }
      if (matchesAnyString(granteeName, user.getName(), user.getEmail(),
          user.getDistinguishedName())) {
        LOGGER.log(Level.FINER,
            "Grantee Name [{0}] matches with search USER [{1}]",
            new Object[] {granteeName, user.getName()});
        return true;
      }
    } else if (perm.get_GranteeType() == SecurityPrincipalType.GROUP) {
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
        LOGGER.log(Level.FINER, "Authorization for user [{0}]: {1}",
            new Object[] {user.getName(), AUTHENTICATED_USERS});
        return true;
      }
      // This is a performance optimization to check grantee name [group]
      // against groups that the current user is a member of.
      if (user.getGroupNames().contains(granteeName.toLowerCase())) {
        LOGGER.log(Level.FINER,
            "Authorization for user [{0}]: member of group {1}",
            new Object[] {user.getName(), granteeName});
        return true;
      }
      for (Group group : user.getGroups()) {
        if (matchesAnyString(granteeName, group.get_DistinguishedName(),
            group.get_DisplayName())) {
          LOGGER.log(Level.FINER,
              "Authorization for user [{0}]: member of group {1}",
              new Object[] {user.getName(), granteeName});
          return true;
        }
      }
    }
    return false;
  }
}
