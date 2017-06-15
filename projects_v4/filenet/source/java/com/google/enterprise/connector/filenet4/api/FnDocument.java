// Copyright 2007-2010 Google Inc.  All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.collection.BooleanList;
import com.filenet.api.collection.DateTimeList;
import com.filenet.api.collection.Float64List;
import com.filenet.api.collection.IdList;
import com.filenet.api.collection.Integer32List;
import com.filenet.api.collection.StringList;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyBinary;
import com.filenet.api.property.PropertyBinaryList;
import com.filenet.api.property.PropertyBoolean;
import com.filenet.api.property.PropertyBooleanList;
import com.filenet.api.property.PropertyDateTime;
import com.filenet.api.property.PropertyDateTimeList;
import com.filenet.api.property.PropertyFloat64;
import com.filenet.api.property.PropertyFloat64List;
import com.filenet.api.property.PropertyId;
import com.filenet.api.property.PropertyIdList;
import com.filenet.api.property.PropertyInteger32;
import com.filenet.api.property.PropertyInteger32List;
import com.filenet.api.property.PropertyString;
import com.filenet.api.property.PropertyStringList;
import com.filenet.api.util.Id;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core document class, which directly interacts with the core FileNet APIs
 * related to Documents.
 */
public class FnDocument implements IDocument {
  private static final Logger logger =
      Logger.getLogger(FnDocument.class.getName());

  private final Document doc;
  private final Map<String, Property> metas;

  public FnDocument(Document doc) {
    this.doc = doc;
    this.metas = getMetas();
  }

  private Map<String,Property> getMetas() {
    Map<String,Property> propMap = new HashMap<String,Property>();
    Properties props = doc.getProperties();
    Property[] propList = props.toArray();
    for (Property property : propList) {
      propMap.put(property.getPropertyName(), property);
    }
    return propMap;
  }

  @Override
  public Id get_Id() {
    return doc.get_Id();
  }

  @Override
  public Date get_DateLastModified() {
    return doc.get_DateLastModified();
  }

  @Override
  public AccessPermissionList get_Permissions() {
    return doc.get_Permissions();
  }

  @Override
  public String get_Owner() {
    return doc.get_Owner();
  }

  @Override
  public InputStream getContent() {
    InputStream ip = null;
    try {
      List<?> contentList = doc.get_ContentElements();
      ContentTransfer content = (ContentTransfer) contentList.get(0);
      ip = content.accessContentStream();
    } catch (Exception er) {
      logger.log(Level.WARNING, "Unable to retrieve the content of file for "
          + this.doc.get_Id() + " " + er.getLocalizedMessage(), er);
    }
    return ip;
  }

  @Override
  public Double get_ContentSize() {
    return doc.get_ContentSize();
  }

  @Override
  public String get_MimeType() {
    return doc.get_MimeType();
  }

  @Override
  public IVersionSeries getVersionSeries() {
    return new FnVersionSeries(doc.get_VersionSeries());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class EmptyActiveMarkingList extends ArrayList
    implements ActiveMarkingList {}

  @Override
  public ActiveMarkingList get_ActiveMarkings()
      throws RepositoryDocumentException {
    try {
      return doc.get_ActiveMarkings();
    } catch (EngineRuntimeException e) {
      if (e.getExceptionCode() == ExceptionCode.API_PROPERTY_NOT_IN_CACHE) {
        logger.log(Level.FINER, e.getMessage());
        return new EmptyActiveMarkingList();
      } else {
        throw new RepositoryDocumentException(e);
      }
    }
  }

  @Override
  public Folder get_SecurityFolder() {
    try {
      return doc.get_SecurityFolder();
    } catch (EngineRuntimeException e) {
      logger.log(Level.WARNING,
          "Unable to get security folder for document {0}", doc.get_Id());
      return null;
    }
  }

  @Override
  public Set<String> getPropertyNames() {
    return metas.keySet();
  }

  @Override
  public void getProperty(String name, List<Value> list)
      throws RepositoryDocumentException {
    Property prop = metas.get(name);
    if (prop == null) {
      logger.log(Level.FINEST, "Property not found: {0}", name);
      return;
    }
    if (prop instanceof PropertyString ||
        prop instanceof PropertyStringList) {
      logger.log(Level.FINEST, "Getting String property: [{0}]", name);
      getPropertyStringValue(name, list);
    } else if (prop instanceof PropertyBinary ||
        prop instanceof PropertyBinaryList) {
      logger.log(Level.FINEST, "Getting Binary property: [{0}]", name);
      getPropertyBinaryValue(name, list);
    } else if (prop instanceof PropertyBoolean ||
        prop instanceof PropertyBooleanList) {
      logger.log(Level.FINEST, "Getting Boolean property: [{0}]", name);
      getPropertyBooleanValue(name, list);
    } else if (prop instanceof PropertyDateTime ||
        prop instanceof PropertyDateTimeList) {
      logger.log(Level.FINEST, "Getting Date property: [{0}]", name);
      getPropertyDateValue(name, list);
    } else if (prop instanceof PropertyFloat64 ||
        prop instanceof PropertyFloat64List) {
      logger.log(Level.FINEST, "Getting Double/Float property: [{0}]", name);
      getPropertyDoubleValue(name, list);
    } else if (prop instanceof PropertyInteger32 ||
        prop instanceof PropertyInteger32List) {
      logger.log(Level.FINEST, "Getting Integer/Long property: [{0}]", name);
      getPropertyLongValue(name, list);
    } else if (prop instanceof PropertyId ||
        prop instanceof PropertyIdList) {
      logger.log(Level.FINEST, "Getting Id property: [{0}]", name);
      getPropertyGuidValue(name, list);
    } else {
      logger.log(Level.FINEST, "Property type for {0} is not determined: ",
          prop.getClass().getName());
    }
  }

  /**
   * Fetches the String type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @Override
  public void getPropertyStringValue(String propertyName,
      List<Value> valuesList) throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyString) {
      String val = prop.getStringValue();
      if (val != null) {
        valuesList.add(Value.getStringValue(val));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyString] contains NULL value", propertyName);
      }
    } else if (prop instanceof PropertyStringList) {
      StringList slist = prop.getStringListValue();
      Iterator<?> iter = slist.iterator();
      while (iter.hasNext()) {
        String val = (String) iter.next();
        if (val != null) {
          valuesList.add(Value.getStringValue(val));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyStringList] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a String type");
    }
  }

  /**
   * Fetches the GUID type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @VisibleForTesting
  void getPropertyGuidValue(String propertyName, List<Value> valuesList)
      throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyId) {
      Id val = prop.getIdValue();
      if (val != null) {
        String id = val.toString();
        valuesList.add(Value.getStringValue(id.substring(1, id.length() - 1)));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyId] contains NULL value", propertyName);
      }
    } else if (prop instanceof PropertyIdList) {
      IdList idList = prop.getIdListValue();
      Iterator<?> iter = idList.iterator();
      while (iter.hasNext()) {
        Id val = (Id) iter.next();
        if (val != null) {
          // Whenever the ID is retrieved from FileNet, it comes with
          // "{" and "}" surrounded and ID is in between these curly braces.
          // FileNet connector needs ID without curly braces.  Thus removing
          // the curly braces.
          String id = val.toString();
          valuesList.add(
              Value.getStringValue(id.substring(1, id.length() - 1)));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyIdList] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a PropertyId type");
    }
  }

  /**
   * Fetches the Integer type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @VisibleForTesting
  void getPropertyLongValue(String propertyName, List<Value> valuesList)
      throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyInteger32) {
      Integer val = prop.getInteger32Value();
      if (val != null) {
        valuesList.add(Value.getLongValue(val.longValue()));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyInteger32] contains NULL value",
            propertyName);
      }
    } else if (prop instanceof PropertyInteger32List) {
      Integer32List int32List = prop.getInteger32ListValue();
      Iterator<?> iter = int32List.iterator();
      while (iter.hasNext()) {
        Integer val = (Integer) iter.next();
        if (val != null) {
          valuesList.add(Value.getLongValue(val.longValue()));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyInteger32List] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not an Integer32 or Long type");
    }
  }

  /**
   * Fetches the Double/Float type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @VisibleForTesting
  void getPropertyDoubleValue(String propertyName,
      List<Value> valuesList) throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyFloat64) {
      Double val = prop.getFloat64Value();
      if (val != null) {
        valuesList.add(Value.getDoubleValue(val.doubleValue()));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyFloat64] contains NULL value", propertyName);
      }
    } else if (prop instanceof PropertyFloat64List) {
      Float64List float64List = prop.getFloat64ListValue();
      Iterator<?> iter = float64List.iterator();
      while (iter.hasNext()) {
        Double val = (Double) iter.next();
        if (val != null) {
          valuesList.add(Value.getDoubleValue(val.doubleValue()));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyFloat64List] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a Double type");
    }
  }

  /**
   * Fetches the Date type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @Override
  public void getPropertyDateValue(String propertyName, List<Value> valuesList)
      throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyDateTime) {
      Date val = prop.getDateTimeValue();
      if (val != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(val);
        valuesList.add(Value.getDateValue(cal));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyDateTime] contains NULL value",
            propertyName);
      }
    } else if (prop instanceof PropertyDateTimeList) {
      DateTimeList dtList = prop.getDateTimeListValue();
      Iterator<?> iter = dtList.iterator();
      while (iter.hasNext()) {
        Date val = (Date) iter.next();
        if (val != null) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(val);
          valuesList.add(Value.getDateValue(cal));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyDateTimeList] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a Date type");
    }
  }

  /**
   * Fetches the Boolean type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @VisibleForTesting
  void getPropertyBooleanValue(String propertyName,
      List<Value> valuesList) throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyBoolean) {
      Boolean val = prop.getBooleanValue();
      if (val != null) {
        valuesList.add(Value.getBooleanValue(val.booleanValue()));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyBoolean] contains NULL value", propertyName);
      }
    } else if (prop instanceof PropertyBooleanList) {
      BooleanList booleanList = prop.getBooleanListValue();
      Iterator<?> iter = booleanList.iterator();
      while (iter.hasNext()) {
        Boolean val = (Boolean) iter.next();
        if (val != null) {
          valuesList.add(Value.getBooleanValue(val.booleanValue()));
        } else {
          logger.log(Level.FINEST,
              "{0} property [PropertyBooleanList] contains NULL value",
              propertyName);
        }
      }
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a Boolean type");
    }
  }

  /**
   * Fetches the Binary type metadata from FileNet. Responsible for
   * distinguishing between single-valued and multi-valued metadata. If the
   * value fetched from FileNet is of instance type List then it is
   * multi-valued else it is single-valued.
   */
  @VisibleForTesting
  void getPropertyBinaryValue(String propertyName,
      List<Value> valuesList) throws RepositoryDocumentException {
    Property prop = metas.get(propertyName);
    if (prop == null) {
      logger.log(Level.FINEST, "{0} property is null", propertyName);
      return;
    }
    if (prop instanceof PropertyBinary) {
      byte[] val = prop.getBinaryValue();
      if (val.length > 0) {
        valuesList.add(Value.getBinaryValue(val));
      } else {
        logger.log(Level.FINEST,
            "{0} property [PropertyBinary] contains NULL value", propertyName);
      }
    } else if (prop instanceof PropertyBinaryList) {
      logger.log(Level.FINEST, "Binary MultiValued Metadata is currently not "
          + "supported. Binary MultiValued metadata will not be fed to GSA");
    } else {
      throw new RepositoryDocumentException("Invalid data type: "
          + propertyName + " property is not a Binary type");
    }
  }
}
