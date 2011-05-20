package com.google.enterprise.connector.filenet3.filejavawrap;

import com.google.enterprise.connector.filenet3.FileUtil;
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

/**
 * Wrapper class over the FileNet API class Permissions. This class is
 * responsible to authorize a target user against all the Access Control Entries
 * of a target document.
 * 
 * @author pankaj_chouhan
 */
public class FnPermissions implements IPermissions {

	EntireNetwork en;

	Permissions perms;

	public static int LEVEL_VIEW = Permission.LEVEL_VIEW;

	private static Logger LOGGER = Logger.getLogger(FnPermissions.class.getName());

	private static final String AUTHENTICATED_USERS = "#AUTHENTICATED-USERS";

	private static final String ACTIVE_DIRECTORY_SYMBOL = "@";

	private static final int PRINCIPAL_SEARCH_TYPE_NONE = Realm.PRINCIPAL_SEARCH_TYPE_NONE;
	private static final int PRINCIPAL_SEARCH_ATTR_NONE = Realm.PRINCIPAL_SEARCH_ATTR_NONE;
	private static final int PRINCIPAL_SEARCH_SORT_NONE = Realm.PRINCIPAL_SEARCH_SORT_NONE;

	public FnPermissions(EntireNetwork refEn, Permissions refPerms) {
		LOGGER.finest("FnPermissions:" + refPerms.toString());
		this.en = refEn;
		this.perms = refPerms;
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
		LOGGER.log(Level.FINE, "Authorizing user:[" + username + "]");
		while (iter.hasNext()) {
			Permission perm = (Permission) iter.next();
			Integer accessMask = perm.getAccess();
			LOGGER.log(Level.FINEST, "Access Mask is:[" + accessMask + "]");

			// Compare to make sure that the access level, to user for a
			// document, is atleast view or above.
			if ((accessMask & LEVEL_VIEW) == LEVEL_VIEW) {
				accessLevelFailure = false;
				granteeName = perm.getGranteeName();
				if (perm.getGranteeType() == BaseObject.TYPE_USER) {
					LOGGER.log(Level.INFO, "Grantee Name is [" + granteeName
							+ "] is of type USER");
					// compare username with complete granteeName or shortName
					// of the grantee
					if (granteeName.equalsIgnoreCase(username)
							|| granteeName.split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username)
							|| FileUtil.getShortName(granteeName).equalsIgnoreCase(username)) {
						LOGGER.log(Level.INFO, "Authorization for user: ["
								+ username + "] is Successful");
						return true;
					} else {
						LOGGER.log(Level.FINER, "Grantee Name ["
								+ granteeName
								+ "] does not match with search user ["
								+ username
								+ "]. Authorization will continue with the next Grantee Name");
					}
				} else if (perm.getGranteeType() == BaseObject.TYPE_GROUP) { // GROUP
					LOGGER.log(Level.INFO, "Grantee Name [" + granteeName
							+ "] is of type GROUP");
					if (perm.getGranteeName().equalsIgnoreCase(AUTHENTICATED_USERS)) {
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
						LOGGER.log(Level.INFO, "Authorization for user: ["
								+ username + "] is Successful");
						return true;
					} else {
						Groups groups = en.getUserRealm().findGroups(perm.getGranteeName().split(ACTIVE_DIRECTORY_SYMBOL)[0], PRINCIPAL_SEARCH_TYPE_NONE, PRINCIPAL_SEARCH_ATTR_NONE, PRINCIPAL_SEARCH_SORT_NONE, 0);

						Group group = null;
						Iterator it = groups.iterator();
						while (it.hasNext()) {
							group = (Group) it.next();
						}
						if (group != null) {
							found = searchUserInGroup(username, group);
							if (found) {
								LOGGER.log(Level.INFO, "Authorization for user: ["
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
	 * It is a recursive function which will search a target user in the
	 * provided group. If the group contains some other group then it
	 * recursively searches the target user in those groups as well.
	 */
	private boolean searchUserInGroup(String username, Group group) {
		User user;
		Group subGroup;
		boolean found;
		Users us = group.getUsers();
		Iterator itUser = us.iterator();
		while (itUser.hasNext()) {
			user = (User) itUser.next();
			LOGGER.log(Level.FINER, "Authorization for USER [" + user.getName()
					+ "] of GROUP [" + group.getName() + "]");
			// compare username with complete username or the shortName of the
			// group member
			if (user.getName().equalsIgnoreCase(username)
					|| user.getName().split(ACTIVE_DIRECTORY_SYMBOL)[0].equalsIgnoreCase(username)
					|| FileUtil.getShortName(user.getName()).equalsIgnoreCase(username)) {
				LOGGER.log(Level.INFO, "Authorization for USER ["
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
