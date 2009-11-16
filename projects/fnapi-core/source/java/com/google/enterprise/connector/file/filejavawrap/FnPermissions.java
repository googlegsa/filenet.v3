package com.google.enterprise.connector.file.filejavawrap;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.filenet.wcm.api.BaseObject;
import com.filenet.wcm.api.EntireNetwork;
import com.filenet.wcm.api.Group;
import com.filenet.wcm.api.Groups;
import com.filenet.wcm.api.Permissions;
import com.filenet.wcm.api.Permission;
import com.filenet.wcm.api.Realm;
import com.filenet.wcm.api.User;
import com.filenet.wcm.api.Users;

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
		Iterator iter = perms.iterator();
		String granteeName;
		while (iter.hasNext()) {
			Permission perm = (Permission) iter.next();
			if ((perm.getAccess() & LEVEL_VIEW) == LEVEL_VIEW) {
				granteeName = perm.getGranteeName();

				logger.log(Level.INFO, "Grantee Name is: " + granteeName);
				if (perm.getGranteeType() == BaseObject.TYPE_USER) {
					if (granteeName.equalsIgnoreCase(username)
							|| granteeName.split("@")[0]
									.equalsIgnoreCase(username)){
						logger.log(Level.INFO, "Authorization for user: " + username + " is Successful");
						return true;
					}
				} else if (perm.getGranteeType() == BaseObject.TYPE_GROUP) { // GROUP
					if(perm.getGranteeName().equalsIgnoreCase("#AUTHENTICATED-USERS")){
						logger.log(Level.INFO, "Document have access permission for #AUTHENTICATED-USERS");
						return true;
					}else{
						Groups groups = en.getUserRealm().findGroups(
								perm.getGranteeName().split("@")[0],
								Realm.PRINCIPAL_SEARCH_TYPE_EXACT,
								Realm.PRINCIPAL_SEARCH_ATTR_DISPLAY_NAME,
								Realm.PRINCIPAL_SEARCH_SORT_NONE, 0);

						Group group = null;
						Iterator it = groups.iterator();
						while (it.hasNext()) {
							group = (Group) it.next();
						}
						if (group != null) {
							found = searchUserInGroup(username, group);
							if (found){
								logger.log(Level.INFO, "Authorization for user: " + username + " is Successful");
								return true;
							}
						}
					}
				}
			}
		}
		logger.log(Level.INFO, "Authorization for user: " + username + " FAILED");
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
			if (user.getName().equalsIgnoreCase(username)
					|| user.getName().split("@")[0].equalsIgnoreCase(username)) {
				return true;
			}
		}
		Groups gs = group.getSubGroups();
		Iterator itGroup = gs.iterator();
		while (itGroup.hasNext()) {
			subGroup = (Group) itGroup.next();
			found = searchUserInGroup(username, subGroup);
			if (found){
				return true;
			}
		}
		return false;
	}

}
