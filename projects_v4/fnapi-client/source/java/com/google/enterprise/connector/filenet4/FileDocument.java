// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete Document class with all the functionalities of Document
 *
 * @author pankaj_chouhan
 */
public class FileDocument implements Document {
  private static final Logger logger =
      Logger.getLogger(FileDocument.class.getName());

  private String docId;
  private IObjectStore objectStore;
  private IDocument document = null;
  private boolean isPublic = false;
  private String displayUrl;
  private String versionId;
  private Date timeStamp;
  private String vsDocId;
  private Set<String> included_meta = null;
  private Set<String> excluded_meta = null;

  private SpiConstants.ActionType action;

  public FileDocument(String docId, Date timeStamp, IObjectStore objectStore,
          boolean isPublic, String displayUrl, Set<String> included_meta,
          Set<String> excluded_meta, SpiConstants.ActionType action) {
    this.docId = docId;
    this.timeStamp = timeStamp;
    this.objectStore = objectStore;
    this.isPublic = isPublic;
    this.displayUrl = displayUrl;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.action = action;
  }

  public FileDocument(String docId, String commonVersionId, Date timeStamp,
          IObjectStore objectStore, boolean isPublic, String displayUrl,
          Set<String> included_meta, Set<String> excluded_meta,
          SpiConstants.ActionType action) {
    this.docId = docId;
    this.versionId = commonVersionId;
    this.timeStamp = timeStamp;
    this.objectStore = objectStore;
    this.isPublic = isPublic;
    this.displayUrl = displayUrl;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.action = action;
  }

  private void fetch() throws RepositoryDocumentException {
    if (document != null) {
      return;
    }
    document = (IDocument) objectStore.getObject(ClassNames.DOCUMENT, docId);
    document.fetch(included_meta);
    logger.log(Level.FINE, "Fetch document for DocId " + docId);
    this.vsDocId = document.getVersionSeries().getId(action);
    logger.log(Level.FINE, "VersionSeriesID for document is : "
            + this.vsDocId);
  }

  public Property findProperty(String name)
          throws RepositoryDocumentException {
    LinkedList<Value> list = new LinkedList<Value>();

    if (SpiConstants.ActionType.ADD.equals(action)) {
      fetch();
      if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getBinaryValue(document.getContent()));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getStringValue(this.displayUrl + vsDocId));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getBooleanValue(this.isPublic));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        this.document.getPropertyDateValue("DateLastModified", list);
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
        document.getPropertyStringValue("MimeType", list);
        logger.log(Level.FINEST, "Getting property: " + name);
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
        return null;
      } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getStringValue(vsDocId));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
        list.add(Value.getStringValue(action.toString()));
        logger.fine("Getting Property " + name + " : "
                + action.toString());
        return new SimpleProperty(list);
      }

      String type = document.getPropertyType(name);
      if (type == null) // unknows property name
        return null;

      if (type.equalsIgnoreCase("Binary")) {
        logger.log(Level.FINEST, "Getting Binary property: [" + name
                + "]");
        document.getPropertyBinaryValue(name, list);
      } else if (type.equalsIgnoreCase("Boolean")) {
        logger.log(Level.FINEST, "Getting Boolean property: [" + name
                + "]");
        document.getPropertyBooleanValue(name, list);
      } else if (type.equalsIgnoreCase("Date")) {
        logger.log(Level.FINEST, "Getting Date property: [" + name
                + "]");
        document.getPropertyDateValue(name, list);
      } else if (type.equalsIgnoreCase("Double")
              || type.equalsIgnoreCase("Float")) {
        logger.log(Level.FINEST, "Getting Double/Float property: ["
                + name + "]");
        document.getPropertyDoubleValue(name, list);
      } else if (type.equalsIgnoreCase("String")) {
        logger.info("Getting String property: [" + name + "]");
        document.getPropertyStringValue(name, list);
      } else if (type.equalsIgnoreCase("guid")) {
        logger.log(Level.FINEST, "Getting GUID property: [" + name
                + "]");
        document.getPropertyGuidValue(name, list);
      } else if (type.equalsIgnoreCase("Long")
              || type.equalsIgnoreCase("Integer")) {
        logger.log(Level.FINEST, "Getting Long property: [" + name
                + "]");
        document.getPropertyLongValue(name, list);
      }
    } else {
      if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.setTime(timeStamp);
        list.add(Value.getDateValue(tmpCal));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getStringValue(action.toString()));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getStringValue(versionId));
        return new SimpleProperty(list);
      }
    }
    return new SimpleProperty(list);
  }

  public Set<String> getPropertyNames() throws RepositoryDocumentException {
    fetch();
    Set<String> properties = new HashSet<String>();
    Set<String> documentProperties = document.getPropertyName();
    for (String property : documentProperties) {
      if (property != null) {
        if (included_meta.size() != 0) {
          // includeMeta - excludeMeta
          logger.log(Level.FINE, "Metadata set will be (includeMeta - exludeMeta)");
          if ((!excluded_meta.contains(property) && included_meta.contains(property))) {
            properties.add(property);
          }
        } else {
          // superSet - excludeMeta
          logger.log(Level.FINE, "Metadata set will be (superSet - exludeMeta)");
          if ((!excluded_meta.contains(property) || included_meta.contains(property))) {
            properties.add(property);
          }
        }
      }
    }

    return properties;
  }
}
