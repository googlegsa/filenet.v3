package com.google.enterprise.connector.file.filewrap;

import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

public interface IReadableSecurityObject extends IBaseObject,
		IReadableMetadataObject {

	public IPermissions getPermissions(Session s) throws RepositoryDocumentException;

}
