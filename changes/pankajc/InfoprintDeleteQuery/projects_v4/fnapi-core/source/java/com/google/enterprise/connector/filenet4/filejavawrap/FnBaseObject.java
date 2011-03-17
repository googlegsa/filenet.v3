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

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;
import com.filenet.apiimpl.core.DeletionEventImpl;
import com.filenet.apiimpl.core.DocumentImpl;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnBaseObject implements IBaseObject {

	private IndependentObject object;

	private static Logger LOGGER = Logger.getLogger(FnBaseObject.class.getName());

	public FnBaseObject(IndependentObject object) {
		this.object = object;
		PropertyFilter pf = new PropertyFilter();

		pf.addIncludeProperty(new FilterElement(null, null, null,
				PropertyNames.RELEASED_VERSION));
		pf.addIncludeProperty(new FilterElement(null, null, null,
				PropertyNames.VERSION_SERIES));
		pf.addIncludeProperty(new FilterElement(null, null, null,
				PropertyNames.VERSION_SERIES_ID));
		pf.addIncludeProperty(new FilterElement(null, null, null,
				PropertyNames.ID));

		this.object.fetchProperties(pf);
	}

	/**
	 * Fetches the ClasName of a object from FileNet.
	 */
	public String getClassNameEvent() throws RepositoryDocumentException {
		return this.object.getClassName();
	}

	/**
	 * Fetches the ID of a object from FileNet.
	 */
	public String getId(ActionType action) throws RepositoryDocumentException {
		String id;
		try {

			if (SpiConstants.ActionType.DELETE.equals(action)) {
				id = ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).get_Id().toString();
			} else {// if action==SpiConstants.ActionType.ADD
				id = ((Document) this.object).get_Id().toString();
			}

			id = id.substring(1, id.length() - 1);
		} catch (Exception e) {
			LOGGER.warning("Unable to get Id for action " + action);
			throw new RepositoryDocumentException(e);
		}
		return id;
	}

	/**
	 * Fetches the DateLastModified of a object from FileNet.
	 */
	public Date getModifyDate(ActionType action)
			throws RepositoryDocumentException {
		Date ModifyDate = new Date();
		try {
			if (SpiConstants.ActionType.DELETE.equals(action)) {
				ModifyDate = ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).get_DateCreated();
			} else {// if action==SpiConstants.ActionType.ADD
				ModifyDate = ((Document) this.object).get_DateLastModified();
			}
		} catch (Exception e) {
			LOGGER.warning("Unable to get Modified Date for action " + action);
			throw new RepositoryDocumentException(e);
		}
		return ModifyDate;
	}

	public Date getPropertyDateValueDelete(String name)
			throws RepositoryDocumentException {

		try {
			Properties props = ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).getProperties();
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Property prop = (Property) it.next();
				String propName = prop.getPropertyName();
				if (propName.equalsIgnoreCase(name)) {
					return prop.getDateTimeValue();
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error while trying to get the property "
					+ name
					+ " of the file "
					+ ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).get_Id()
					+ " " + e.getMessage());
			RepositoryDocumentException re = new RepositoryDocumentException(e);
			throw re;
		}
		return null;
	}

	/**
	 * Fetches the VersionSeriesId of a object from FileNet.
	 */
	public String getVersionSeriesId(ActionType action)
			throws RepositoryDocumentException {

		Id id;
		String strId;
		try {
			if (SpiConstants.ActionType.DELETE.equals(action)) {
				id = ((DeletionEventImpl) this.object).get_VersionSeriesId();
			} else {// if action==SpiConstants.ActionType.ADD
				id = ((DocumentImpl) this.object).get_ReleasedVersion().get_VersionSeries().get_Id();
			}
		} catch (Exception e) {
			LOGGER.warning("Unable to get Version Series Id ");
			throw new RepositoryDocumentException(e);
		}
		strId = id.toString();
		strId = strId.substring(1, strId.length() - 1);
		return strId;
	}

}
