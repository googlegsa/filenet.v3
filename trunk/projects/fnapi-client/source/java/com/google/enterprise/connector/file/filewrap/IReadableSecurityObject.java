package com.google.enterprise.connector.file.filewrap;

import com.filenet.wcm.api.Session;

public interface IReadableSecurityObject extends IBaseObject,
		IReadableMetadataObject {

	public IPermissions getPermissions(Session s);

}
