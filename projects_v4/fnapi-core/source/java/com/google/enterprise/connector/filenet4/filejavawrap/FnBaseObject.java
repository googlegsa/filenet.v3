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

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class FnBaseObject implements IBaseObject {
  private static final Logger logger =
      Logger.getLogger(FnBaseObject.class.getName());

  private final IndependentObject object;

  public FnBaseObject(IndependentObject object) {
    this.object = object;
  }

  public String getClassNameEvent() throws RepositoryDocumentException {
    return this.object.getClassName();
  }

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
      logger.warning("Unable to get Id for action " + action);
      throw new RepositoryDocumentException(e);
    }
    return id;
  }

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
      logger.warning("Unable to get Modified Date for action " + action);
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
      logger.log(Level.WARNING, "Error while trying to get the property "
          + name
          + " of the file "
          + ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).get_Id()
          + " " + e.getMessage());
      RepositoryDocumentException re = new RepositoryDocumentException(e);
      throw re;
    }
    return null;
  }

  public String getVersionSeriesId(ActionType action)
      throws RepositoryDocumentException {

    Id id;
    String strId;
    try {
      if (SpiConstants.ActionType.DELETE.equals(action)) {
        id = ((com.filenet.apiimpl.core.DeletionEventImpl) this.object).get_VersionSeriesId();
      } else {// if action==SpiConstants.ActionType.ADD
        id = ((com.filenet.apiimpl.core.DocumentImpl) this.object).get_ReleasedVersion().get_VersionSeries().get_Id();
      }
    } catch (Exception e) {
      logger.warning("Unable to get Version Series Id ");
      throw new RepositoryDocumentException(e);
    }
    strId = id.toString();
    strId = strId.substring(1, strId.length() - 1);
    return strId;
  }

}
