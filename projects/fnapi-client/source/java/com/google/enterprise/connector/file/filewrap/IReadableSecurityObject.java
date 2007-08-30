package com.google.enterprise.connector.file.filewrap;

public interface IReadableSecurityObject extends IBaseObject,
		IReadableMetadataObject {

	public IPermissions getPermissions();

}
