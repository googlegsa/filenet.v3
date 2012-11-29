package com.google.enterprise.connector.filenet3.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IVersionSeries extends IBaseObject, IGettableObject,
		IReadableMetadataObject {

	public IDocument getCurrentVersion();

	public IDocument getReleasedVersion();

}
