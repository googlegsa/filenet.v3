package com.google.enterprise.connector.file.filejavawrap;

import java.util.Iterator;

import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.filenet.wcm.api.Permissions;
import com.filenet.wcm.api.Permission;

public class FnPermissions implements IPermissions {

	Permissions perms;
	String authenticatedUser = "#AUTHENTICATED-USERS";
	
	public static int RIGHT_READ = Permission.RIGHT_READ;
	
	public FnPermissions(Permissions perms){
		this.perms = perms;
	}

	public int asMask(String username) {
		Iterator iter = perms.iterator();
		while(iter.hasNext()){
			Permission permi = (Permission)iter.next();
			if(permi.getGranteeName().startsWith(username)){
				return permi.getAccessType() & Permission.RIGHT_READ;
			}
				
		}
		iter = perms.iterator();
		while(iter.hasNext()){
			Permission permi = (Permission)iter.next();
			if(permi.getGranteeName().startsWith(authenticatedUser)){
				return permi.getAccessType() & Permission.RIGHT_READ;
			}
		}
		return 0;
		
	}
}
