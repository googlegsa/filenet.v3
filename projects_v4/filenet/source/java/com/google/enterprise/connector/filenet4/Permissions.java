// Copyright 2007-2010 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class over the FileNet API class Permissions. This class is
 * responsible to authorize a target user against all the Access Control Entries
 * of a target document.
 */
public class Permissions {
  private static final Logger LOGGER =
      Logger.getLogger(Permissions.class.getName());

  public static interface Factory {
    Permissions getInstance(AccessPermissionList perms, String owner);
    Permissions getInstance(AccessPermissionList perms);
  }

  public static Factory getFactory() {
    return new Factory() {
      @Override
      public Permissions getInstance(AccessPermissionList perms, String owner) {
        return new Permissions(perms, owner);
      }

      @Override
      public Permissions getInstance(AccessPermissionList perms) {
        return getInstance(perms, null);
      }
    };
  }

  public static final String AUTHENTICATED_USERS = "#AUTHENTICATED-USERS";
  private static final String CREATOR_OWNER = "#CREATOR-OWNER";

  private static int VIEW_ACCESS_RIGHTS =
      AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT;
  private static int USE_MARKING = AccessRight.USE_MARKING_AS_INT;

  private final AccessPermissionList perms;
  private final String owner;
  private final SetMultimap<PermissionSource, String> allowUsers;
  private final SetMultimap<PermissionSource, String> allowGroups;
  private final SetMultimap<PermissionSource, String> denyUsers;
  private final SetMultimap<PermissionSource, String> denyGroups;

  public Permissions(AccessPermissionList perms, String owner) {
    this.perms = perms;
    this.owner = owner;
    this.allowUsers = HashMultimap.create();
    this.allowGroups = HashMultimap.create();
    this.denyUsers = HashMultimap.create();
    this.denyGroups = HashMultimap.create();
    processPermissions();
  }

  public Permissions(AccessPermissionList perms) {
    this(perms, null);
  }

  /**
   * To authorize a given username against the grantee-names, present in all
   * the Access Control Entries for all the permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of
   *         authorization.
   */
  public boolean authorize(User user) {
    boolean isAuthorized = false;
    Iterator<?> iter = perms.iterator();

    LOGGER.log(Level.FINE, "Authorizing user:[" + user.get_Name() + "]");

    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();
        Integer accessMask = perm.get_AccessMask();
        LOGGER.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

        if (AccessType.DENY.equals(perm.get_AccessType())) {
          // Checking for denied read or view content rights.  If either one is
          // true and user is a member of the ACE, the user is not authorized.
          if ((VIEW_ACCESS_RIGHTS & accessMask) != 0
              && matchesUser(perm, user)) {
            LOGGER.log(Level.FINEST,
                "Access is denied for user {0} via grantee {1}",
                new Object[] {user.get_Name(), perm.get_GranteeName()});
            return false;
          }
        } else {
          // Compare to make sure that the access level, to user for a
          // document, is at least view or above
          if ((VIEW_ACCESS_RIGHTS & accessMask) == VIEW_ACCESS_RIGHTS
              && isAuthorized == false && matchesUser(perm, user)) {
            isAuthorized = true;
          }
        }
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING,
            "Exception occured in authorizing user against permissions. "
            + ecp.getMessage(), ecp);
      }
    }

    LOGGER.log(Level.FINEST, "User [{0}] is {1}authorized to access document",
        new Object[] {user.get_Name(), (isAuthorized) ? "" : "not "});
    return isAuthorized;
  }

  /**
   * To check, a given user has at least USE right or above, over all the
   * marking permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of check for
   *         USE right.
   * @see com.google.enterprise.connector.filenet4.api.IPermissions#authorizeMarking(java.lang.String)
   */
  public boolean authorizeMarking(User user, Integer constraintMask) {
    boolean hasUseRight = false;

    Iterator<?> iter = perms.iterator();
    while (iter.hasNext()) {
      try {
        AccessPermission perm = (AccessPermission) iter.next();
        LOGGER.log(Level.FINEST, "Checking access rights for {0} user: "
            + "grantee[{1}], access mask[{2}], constraint mask[{3}]",
            new Object[] {user.get_Name(), perm.get_GranteeName(),
                perm.get_AccessMask(), constraintMask});

        if ((perm.get_AccessMask() & USE_MARKING) == USE_MARKING) {
          if (hasUseRight == false
              && AccessType.ALLOW.equals(perm.get_AccessType())
              && matchesUser(perm, user)) {
            hasUseRight = true;
          } else if (AccessType.DENY.equals(perm.get_AccessType())
              && (AccessLevel.FULL_CONTROL_AS_INT == constraintMask)
              && matchesUser(perm, user)) {
            LOGGER.log(Level.FINE, "User: [{0}] has Deny USE right and Deny "
                + "all access rights over the document", user.get_Name());
            return false;
          }
        }
      } catch (Exception ecp) {
        LOGGER.log(Level.WARNING, "Exception occurred in authorizing user "
            + "against permissions. " + ecp.getMessage(), ecp);
      }
    }
    if (hasUseRight) {
      LOGGER.log(Level.FINE, "User [{0}] has USE right over the document",
          user.get_Name());
      return true;
    } else {
      boolean authorizeByConstraints =
          (VIEW_ACCESS_RIGHTS & constraintMask) == 0;
      LOGGER.log(Level.FINE, "User [{0}] is {1}authorized by constraints",
          new Object[] {user.get_Name(),
              (authorizeByConstraints ? "" : "not ")});
      return authorizeByConstraints;
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
   * @see com.google.enterprise.connector.filenet4.api.IPermissions#checkGranteeName(AccessPermission,java.lang.String)
   */
  private boolean matchesUser(AccessPermission perm, User user) {
    String granteeName = perm.get_GranteeName();
    String granteeType = perm.get_GranteeType().toString();
    String accessType = perm.get_AccessType().toString();
    LOGGER.log(Level.FINER, "Grantee Name: [{0}], type: {1}, access type: {2}",
        new Object[] {granteeName, granteeType, accessType});

    if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
      if (granteeName.equalsIgnoreCase(CREATOR_OWNER)) {
        if (owner != null && matchesAnyString(owner, user.get_Name(),
            user.get_Email(), user.get_DistinguishedName())) {
          LOGGER.log(Level.FINER,
              "Authorization: [{0}] matches the creator owner",
              user.get_Name());
          return true;
        }
        LOGGER.log(Level.FINER, "Authorization [{0}]: [{1}] is not {2} owner",
            new Object[] {accessType, user.get_Name(), owner});
        return false;
      }
      if (matchesAnyString(granteeName, user.get_Name(), user.get_Email(),
          user.get_DistinguishedName())) {
        LOGGER.log(Level.FINER,
            "Authorization [{0}]: [{1}] grantee matches with search user [{2}]",
            new Object[] {accessType, granteeName, user.get_Name()});
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
        LOGGER.log(Level.FINER, "Authorization [{0}]: [{1}] user matches {2}",
            new Object[] {accessType, user.get_Name(), AUTHENTICATED_USERS});
        return true;
      }

      Iterator<?> iter = user.get_MemberOfGroups().iterator();
      while (iter.hasNext()) {
        Group group = (Group) iter.next();
        if (matchesAnyString(granteeName, group.get_Name(),
            group.get_DistinguishedName(), group.get_DisplayName())) {
          LOGGER.log(Level.FINER,
              "Authorization [{0}]: [{1}] user is a member of group {2}",
              new Object[] {accessType, user.get_Name(), granteeName});
          return true;
        }
      }
    }
    return false;
  }

  private void processPermissions() {
    Iterator<?> iter = perms.iterator();
    while (iter.hasNext()) {
      AccessPermission perm = (AccessPermission) iter.next();
      int mask = perm.get_AccessMask();
      if ((mask & VIEW_ACCESS_RIGHTS) != VIEW_ACCESS_RIGHTS) {
        continue;
      }
      if (perm.get_AccessType() == AccessType.ALLOW) {
        if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
          allowUsers.put(perm.get_PermissionSource(), perm.get_GranteeName());
        } else {
          allowGroups.put(perm.get_PermissionSource(), perm.get_GranteeName());
        }
      } else {
        if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
          denyUsers.put(perm.get_PermissionSource(), perm.get_GranteeName());
        } else {
          denyGroups.put(perm.get_PermissionSource(), perm.get_GranteeName());
        }
      }
    }
  }

  public Set<String> getAllowUsers() {
    return new HashSet<String>(allowUsers.values());
  }

  public Set<String> getAllowUsers(PermissionSource permSrc) {
    return allowUsers.get(permSrc);
  }

  public Set<String> getAllowGroups() {
    return new HashSet<String>(allowGroups.values());
  }

  public Set<String> getAllowGroups(PermissionSource permSrc) {
    return allowGroups.get(permSrc);
  }

  public Set<String> getDenyUsers() {
    return new HashSet<String>(denyUsers.values());
  }

  public Set<String> getDenyUsers(PermissionSource permSrc) {
    return denyUsers.get(permSrc);
  }

  public Set<String> getDenyGroups() {
    return new HashSet<String>(denyGroups.values());
  }

  public Set<String> getDenyGroups(PermissionSource permSrc) {
    return denyGroups.get(permSrc);
  }
}
