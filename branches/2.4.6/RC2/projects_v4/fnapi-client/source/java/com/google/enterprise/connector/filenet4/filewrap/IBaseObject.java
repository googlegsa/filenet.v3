package com.google.enterprise.connector.filenet4.filewrap;

import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public interface IBaseObject {

	public String getId(ActionType action) throws RepositoryDocumentException;
	public Date getModifyDate(ActionType action) throws RepositoryDocumentException;
	public String getVersionSeriesId() throws RepositoryDocumentException;
	public Date getPropertyDateValueDelete(String name) throws RepositoryDocumentException;
	public String getClassNameEvent() throws RepositoryDocumentException;
}
