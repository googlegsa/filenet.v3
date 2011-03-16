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

import com.google.enterprise.connector.filenet4.filewrap.IPermissions;

import com.filenet.api.collection.GroupSet;
import com.filenet.api.collection.PermissionList;
import com.filenet.api.collection.UserSet;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Connection;
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
public class FnPermissions implements IPermissions {

	private PermissionList perms;
	private int ACCESS_LEVEL = AccessLevel.VIEW_AS_INT;
	private String ACTIVE_DIRECTORY_SYMBOL = "@";
	private int ACCESS_OBJECT_LEVEL = AccessRight.USE_MARKING_AS_INT;
	private Logger logger = null;

	public FnPermissions(PermissionList perms) {
		this.perms = perms;
		logger = Logger.getLogger(FnDocument.class.getName());
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
		String granteeName;
		logger.log(Level.FINE, "Authorizing user:[" + username + "]");
		while (iter.hasNext()) {
			AccessPermission perm = (AccessPermission) iter.next();
			Integer accessMask = perm.get_AccessMask();
			logger.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

			// Compare to make sure that the access level, to user for a
			// document, is atleast view or above
			if ((accessMask & ACCESS_LEVEL) == ACCESS_LEVEL) {
				accessLevelFailure = false;
				granteeName = perm.get_GranteeName();
				if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
					logger.log(Level.INFO, "Grantee Name is [" + granteeName
							+ "] is of type USER");
					if (granteeName.equalsIgnoreCase(username)
							|| granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username)) {
						logger.log(Level.INFO, "Authorization for user: ["
								+ username + "] is Successful");
						return true;
					} else {
						logger.log(Level.FINER, "Grantee Name ["
								+ granteeName
								+ "] does not match with search user ["
								+ username
								+ "]. Authorization will continue with the next Grantee Name");
					}
				} else if (perm.get_GranteeType() == SecurityPrincipalType.GROUP) { // GROUP
					logger.log(Level.INFO, "Grantee Name [" + granteeName
							+ "] is of type GROUP");
					if (granteeName.equalsIgnoreCase("#AUTHENTICATED-USERS")) {
						// #AUTHENTICATED-USERS is a logical group in FileNet P8
						// Systems, which gets automatically
						// created at the time of FileNet installation. This
						// group contains all the FileNet users.
						// This group cannot be edited by admin i.e. it is not
						// possible to delete the user from
						// this group. Thus every FileNet user is a part of
						// #AUTHENTICATED-USERS group. This is the
						// reason to authenticate a user for a document, if that
						// document contains #AUTHENTICATED-USERS
						// group in its ACL or ACE.
						logger.log(Level.INFO, "Authorization for user: ["
								+ username + "] is Successful");
						return true;
					} else {
						Connection conn = perm.getConnection();
						Group group = null;
						try {
							group = com.filenet.api.core.Factory.Group.fetchInstance(conn, perm.get_GranteeName(), null);
							found = searchUserInGroup(username, group);
							if (found) {
								logger.log(Level.INFO, "Authorization for user: ["
										+ username + "] is Successful");
								return true;
							}
						} catch (Exception e) {
							logger.log(Level.WARNING, "Skipping Group ["
									+ granteeName + "], as it is not found.", e);
						}
					}
				}
			}
		}
		if (accessLevelFailure) {
			// If the document have no view content or more Access Security
			// Level to any user
			logger.log(Level.WARNING, "Authorization for user: ["
					+ username
					+ "] FAILED due to insufficient Access Security Levels. Minimum expected Access Security Level is \"View Content\"");
		} else {
			logger.log(Level.WARNING, "Authorization for user: ["
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

		logger.log(Level.FINE, "Checking user : [" + username
				+ "] For USE right over the Document");

		while (iter.hasNext()) {
			AccessPermission perm = (AccessPermission) iter.next();

			// Check whether the GranteeName and search user name is same or
			// not. If not then Go for next AccessPermission.

			if (checkGranteeName(perm, username)) {
				AccessType accessType = perm.get_AccessType();
				logger.log(Level.FINEST, "Access Type is:[" + accessType + "]");
				Integer accessMask = perm.get_AccessMask();
				logger.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

				if (accessType.equals(AccessType.ALLOW)) {

					// Check whether the search user has USE Right over the
					// document
					if ((accessMask & ACCESS_OBJECT_LEVEL) == ACCESS_OBJECT_LEVEL) {
						logger.log(Level.FINE, " User: [" + username
								+ "] has USE right over the document ");
						return true;
					} else {
						logger.log(Level.FINE, " User: [" + username
								+ "] does not have USE right over the document");
					}
				} else {
					logger.log(Level.FINE, " User: [" + username
							+ "] does not have USE right over the document");
				}
			}
		}
		logger.log(Level.FINE, " User: [" + username
				+ "] does not have USE right over the document");
		return false;
	}

	/**
	 * To check, a given user is in the list of Grantee of the given permission
	 * object.
	 * 
	 * @param Object of the AccessPermission, Search user name.
	 * @return True or False, depending on the success or failure of check for
	 *         grantee name check.
	 * @see com.google.enterprise.connector.filenet4.filewrap.IPermissions#checkGranteeName(AccessPermission,java.lang.String)
	 */

	private boolean checkGranteeName(AccessPermission perm, String username) {

		String granteeName = perm.get_GranteeName();
		boolean found = false;

		logger.log(Level.FINE, "Matching the Grantee Name [" + granteeName
				+ "] with search USER : [" + username + "]");

		// check the type of the Grantee (USER or GROUP) for the given
		// permission

		if (perm.get_GranteeType() == SecurityPrincipalType.USER) {
			logger.log(Level.FINE, "Grantee Name is [" + granteeName
					+ "] is of type USER");

			// Match the Full Grantee name and Search user name.

			if (granteeName.equalsIgnoreCase(username)
					|| granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username)) {
				logger.log(Level.FINE, "Grantee Name [" + granteeName
						+ "] matches with search USER [" + username + "]");

				return true;
			} else if (getShortName(granteeName).equalsIgnoreCase(username)) {// Match
				// the
				// Short
				// Grantee
				// name
				// and
				// Search
				// user
				// name.
				logger.log(Level.FINE, "Grantee Name ["
						+ getShortName(granteeName)
						+ "] matches with search USER [" + username + "]");

				return true;
			} else {
				logger.log(Level.FINE, "Grantee Name ["
						+ granteeName
						+ "] does not match with search user ["
						+ username
						+ "]. Checking will continue with the next Grantee Name");
			}
		} else if (perm.get_GranteeType() == SecurityPrincipalType.GROUP) { // GROUP
			logger.log(Level.FINE, "Grantee Name [" + granteeName
					+ "] is of type GROUP");

			// Check whether the search user is Member of #AUTHENTICATED-USERS
			// Group. If not then Search Username in the Group.

			if (granteeName.equalsIgnoreCase("#AUTHENTICATED-USERS")) {

				logger.log(Level.FINE, "Grantee Name [" + granteeName
						+ "] contains search USER [" + username + "]");
				return true;

			} else {
				Connection conn = perm.getConnection();
				Group group = null;
				try {
					group = com.filenet.api.core.Factory.Group.fetchInstance(conn, perm.get_GranteeName(), null);
					found = searchUserInGroup(username, group);
					if (found) {
						logger.log(Level.FINE, "Grantee Name [" + granteeName
								+ "] contains search USER [" + username + "]");
						return true;
					}
				} catch (Exception e) {
					logger.log(Level.FINE, "Skipping Group [" + granteeName
							+ "], as it is not found." + e.getMessage());
				}
			}
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
				}// end:if(null!=mytok1){
			}// end: while
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
			logger.log(Level.FINER, "Searching the USER [" + username
					+ "] in GROUP [" + group.get_Name() + "]");
			if (user.get_Name().equalsIgnoreCase(username)
					|| user.get_Name().split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username)) {
				logger.log(Level.FINE, "Search USER [" + username
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
