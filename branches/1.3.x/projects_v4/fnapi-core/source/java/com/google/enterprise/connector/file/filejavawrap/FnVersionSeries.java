package com.google.enterprise.connector.file.filejavawrap;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.core.Document;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.util.Id;
import com.google.enterprise.connector.file.FileDocument;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public class FnVersionSeries implements IVersionSeries {

	private VersionSeries versionSeries;

	private static Logger logger = null;
	{
		logger = Logger.getLogger(FnVersionSeries.class.getName());
	}
	
	public FnVersionSeries(VersionSeries versionSeries) {
		this.versionSeries = versionSeries;
		
		try {
			
		}catch (Exception e){
			// TODO change this
			//System.out.println("REFRESH EXCEPTION");
			e.printStackTrace();
		}
		
	}

	public String getId(ActionType action) {
		logger.info("getId, FnVersionSeries");
		return versionSeries.get_Id().toString();

	}
		
	public Date getModifyDate(ActionType action) throws RepositoryDocumentException {
		Date ModifyDate = new Date();
		return ModifyDate;
	}
	
	public String getClassNameEvent() throws RepositoryDocumentException {
		try {
			return this.versionSeries.getClassName();
		} catch (Exception e1) {
			RepositoryDocumentException re = new RepositoryDocumentException(e1);
			throw re;
		}
	}
	
	public Date getPropertyDateValueDelete(String name) throws RepositoryDocumentException {
		return new Date();
	}
	
	public String getVersionSeriesId() throws RepositoryDocumentException {
		Id id;
		String strId;
		try {
			id = ((com.filenet.apiimpl.core.DeletionEventImpl) versionSeries).get_VersionSeriesId();
		}catch (Exception e){
			 throw new RepositoryDocumentException(e);
		}
		logger.info("versionId : ID : "+id);
		strId=id.toString();
		logger.info("versionId : tostring : "+strId);
		strId = strId.substring(1,strId.length()-1);
		logger.info("versionId : cut start/end : "+strId);
		return strId;
	}

	public IDocument getCurrentVersion() throws RepositoryException {
		return new FnDocument((Document) this.versionSeries
				.get_CurrentVersion());
	}

	public IDocument getReleasedVersion() throws RepositoryException {
		return new FnDocument((Document) this.versionSeries
				.get_ReleasedVersion());
	}

}
