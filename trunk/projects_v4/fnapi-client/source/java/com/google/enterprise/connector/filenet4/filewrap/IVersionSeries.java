package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

public interface IVersionSeries extends IBaseObject {

  public IDocument getCurrentVersion() throws RepositoryException;

  public IDocument getReleasedVersion() throws RepositoryException;

}
