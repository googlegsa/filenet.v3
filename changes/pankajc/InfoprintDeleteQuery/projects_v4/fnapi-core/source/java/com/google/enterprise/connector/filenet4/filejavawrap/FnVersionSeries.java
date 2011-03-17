/*
 * Copyright 2009 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
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
import com.filenet.apiimpl.core.DeletionEventImpl;
import com.filenet.apiimpl.core.DocumentImpl;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnVersionSeries implements IVersionSeries {

	private VersionSeries versionSeries;
	private static Logger LOGGER = Logger.getLogger(FnVersionSeries.class.getName());

	public FnVersionSeries(VersionSeries versionSeries) {
		this.versionSeries = versionSeries;
	}

	/**
	 * To fetch ID of the document form FileNet and return.
	 */
	public String getId(ActionType action) {
		LOGGER.info("getId, FnVersionSeries");
		return versionSeries.get_Id().toString();
	}

	public Date getModifyDate(ActionType action)
			throws RepositoryDocumentException {
		Date ModifyDate = new Date();
		return ModifyDate;
	}

	/**
	 * To fetch ClasName of the document form FileNet and return.
	 */
	public String getClassNameEvent() throws RepositoryDocumentException {
		return this.versionSeries.getClassName();
	}

	public Date getPropertyDateValueDelete(String name)
			throws RepositoryDocumentException {
		return new Date();
	}

	/**
	 * To fetch VersionSeriesID of the document form FileNet and return.
	 */

	public String getVersionSeriesId(ActionType action)
			throws RepositoryDocumentException {
		Id id;
		String strId;
		try {

			if (SpiConstants.ActionType.DELETE.equals(action)) {
				id = ((DeletionEventImpl) versionSeries).get_VersionSeriesId();
			} else {// if action==SpiConstants.ActionType.ADD
				id = ((DocumentImpl) versionSeries).get_ReleasedVersion().get_VersionSeries().get_Id();
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Unable to get the VersionSeriesId");
			throw new RepositoryDocumentException(e);
		}
		// logger.info("versionId : ID : "+id);
		strId = id.toString();
		// logger.info("versionId : tostring : "+strId);
		strId = strId.substring(1, strId.length() - 1);
		// logger.info("versionId : cut start/end : "+strId);
		return strId;
	}

	/**
	 * To fetch CurrentVersion of the document form FileNet and return.
	 */

	public IDocument getCurrentVersion() throws RepositoryException {
		return new FnDocument(
				(Document) this.versionSeries.get_CurrentVersion());
	}

	/**
	 * To fetch ReleasedVersion of the document form FileNet and return.
	 */
	public IDocument getReleasedVersion() throws RepositoryException {
		return new FnDocument(
				(Document) this.versionSeries.get_ReleasedVersion());
	}

}
