package com.google.enterprise.connector.filenet3.filejavawrap;

import com.google.enterprise.connector.filenet3.filewrap.IPermissions;

import com.filenet.wcm.api.BaseObject;
import com.filenet.wcm.api.EntireNetwork;
import com.filenet.wcm.api.Group;
import com.filenet.wcm.api.Groups;
import com.filenet.wcm.api.Permission;
import com.filenet.wcm.api.Permissions;
import com.filenet.wcm.api.Realm;
import com.filenet.wcm.api.User;
import com.filenet.wcm.api.Users;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnPermissions implements IPermissions {

	EntireNetwork en;

	Permissions perms;

	public static int LEVEL_VIEW = Permission.LEVEL_VIEW;

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FnPermissions.class.getName());
	}

	public FnPermissions(EntireNetwork refEn, Permissions refPerms) {
		logger.finest("FnPermissions:" + refPerms.toString());
		this.en = refEn;
		this.perms = refPerms;
	}

	public boolean authorize(String username) {
		boolean found;
		boolean accessLevelFailure = true;
		Iterator iter = perms.iterator();
		String granteeName;
		logger.log(Level.FINE, "Authorizing user:[" + username + "]");
		while (iter.hasNext()) {
			Permission perm = (Permission) iter.next();
			Integer accessMask = perm.getAccess();
			logger.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

			// Compare to make sure that the access level, to user for a
			// document, is atleast view or above.
			if ((accessMask & LEVEL_VIEW) == LEVEL_VIEW) {
				accessLevelFailure = false;
				granteeName = perm.getGranteeName();
				if (perm.getGranteeType() == BaseObject.TYPE_USER) {
					logger.log(Level.INFO, "Grantee Name is [" + granteeName
							+ "] is of type USER");
					if (granteeName.equalsIgnoreCase(username)
							|| granteeName.split("@")[0].equalsIgnoreCase(username)) {
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
				} else if (perm.getGranteeType() == BaseObject.TYPE_GROUP) { // GROUP
					logger.log(Level.INFO, "Grantee Name [" + granteeName
							+ "] is of type GROUP");
					if (perm.getGranteeName().equalsIgnoreCase("#AUTHENTICATED-USERS")) {
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
						Groups groups = en.getUserRealm().findGroups(perm.getGranteeName().split("@")[0], Realm.PRINCIPAL_SEARCH_TYPE_EXACT, Realm.PRINCIPAL_SEARCH_ATTR_DISPLAY_NAME, Realm.PRINCIPAL_SEARCH_SORT_NONE, 0);

						Group group = null;
						Iterator it = groups.iterator();
						while (it.hasNext()) {
							group = (Group) it.next();
						}
						if (group != null) {
							found = searchUserInGroup(username, group);
							if (found) {
								logger.log(Level.INFO, "Authorization for user: ["
										+ username + "] is Successful");
								return true;
							}
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

	private boolean searchUserInGroup(String username, Group group) {
		User user;
		Group subGroup;
		boolean found;
		Users us = group.getUsers();
		Iterator itUser = us.iterator();
		while (itUser.hasNext()) {
			user = (User) itUser.next();
			logger.log(Level.FINER, "Authorization for USER [" + user.getName()
					+ "] of GROUP [" + group.getName() + "]");
			if (user.getName().equalsIgnoreCase(username)
					|| user.getName().split("@")[0].equalsIgnoreCase(username)) {
				logger.log(Level.INFO, "Authorization for USER ["
						+ user.getName() + "] of GROUP [" + group.getName()
						+ "] is successful");
				return true;
			}
		}
		Groups gs = group.getSubGroups();
		Iterator itGroup = gs.iterator();
		while (itGroup.hasNext()) {
			subGroup = (Group) itGroup.next();
			found = searchUserInGroup(username, subGroup);
			if (found) {
				return true;
			}
		}
		return false;
	}

}
