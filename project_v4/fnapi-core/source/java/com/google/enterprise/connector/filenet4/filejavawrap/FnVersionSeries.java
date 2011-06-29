package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import com.filenet.api.core.Document;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.util.Id;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnVersionSeries implements IVersionSeries {

	private VersionSeries versionSeries;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnVersionSeries.class.getName());
	}

	public FnVersionSeries(VersionSeries versionSeries) {
		this.versionSeries = versionSeries;
	}

	public String getId(ActionType action) {
		logger.info("getId, FnVersionSeries");
		return versionSeries.get_Id().toString();

	}

	public Date getModifyDate(ActionType action)
			throws RepositoryDocumentException {
		Date ModifyDate = new Date();
		return ModifyDate;
	}

	public String getClassNameEvent() throws RepositoryDocumentException {
		return this.versionSeries.getClassName();
	}

	public Date getPropertyDateValueDelete(String name)
			throws RepositoryDocumentException {
		return new Date();
	}

	public String getVersionSeriesId(ActionType action)
			throws RepositoryDocumentException {
		Id id;
		String strId;
		try {

			if (SpiConstants.ActionType.DELETE.equals(action)) {
				id = ((com.filenet.apiimpl.core.DeletionEventImpl) versionSeries).get_VersionSeriesId();
			} else {// if action==SpiConstants.ActionType.ADD
				id = ((com.filenet.apiimpl.core.DocumentImpl) versionSeries).get_ReleasedVersion().get_VersionSeries().get_Id();
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unable to get the VersionSeriesId");
			throw new RepositoryDocumentException(e);
		}
		// logger.info("versionId : ID : "+id);
		strId = id.toString();
		// logger.info("versionId : tostring : "+strId);
		strId = strId.substring(1, strId.length() - 1);
		// logger.info("versionId : cut start/end : "+strId);
		return strId;
	}

	public IDocument getCurrentVersion() throws RepositoryException {
		return new FnDocument(
				(Document) this.versionSeries.get_CurrentVersion());
	}

	public IDocument getReleasedVersion() throws RepositoryException {
		return new FnDocument(
				(Document) this.versionSeries.get_ReleasedVersion());
	}

}
