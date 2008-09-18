package com.google.enterprise.connector.file.filewrap;

import java.util.Date;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public interface IBaseObject {

	public String getId(ActionType action) throws RepositoryException;
	public Date getModifyDate(ActionType action) throws RepositoryException;
	public String getVersionSeriesId() throws RepositoryException;
	public Date getPropertyDateValueDelete(String name) throws RepositoryException;
	public String getClassNameEvent() throws RepositoryException;
}
