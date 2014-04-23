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

package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete Document class with all the functionalities of Document
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
  private final String globalNamespace;

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
    this.globalNamespace = null;
  }

  public FileDocument(String docId, String commonVersionId, Date timeStamp,
          IObjectStore objectStore, boolean isPublic, String displayUrl,
          Set<String> included_meta, Set<String> excluded_meta,
          SpiConstants.ActionType action, String globalNamespace) {
    this.docId = docId;
    this.versionId = commonVersionId;
    this.timeStamp = timeStamp;
    this.objectStore = objectStore;
    this.isPublic = isPublic;
    this.displayUrl = displayUrl;
    this.included_meta = included_meta;
    this.excluded_meta = excluded_meta;
    this.action = action;
    this.globalNamespace = globalNamespace;
  }

  private void fetch() throws RepositoryDocumentException {
    if (document != null) {
      return;
    }
    document = (IDocument) objectStore.fetchObject(ClassNames.DOCUMENT, docId,
        FileUtil.getDocumentPropertyFilter(included_meta));
    logger.log(Level.FINE, "Fetch document for DocId {0}", docId);
    vsDocId = document.getVersionSeries().getId();
    logger.log(Level.FINE, "VersionSeriesID for document is: {0}", vsDocId);
  }

  public Property findProperty(String name) throws RepositoryException {
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
      } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
        logger.log(Level.FINEST, "Getting property: " + name);
        list.add(Value.getStringValue(vsDocId));
        return new SimpleProperty(list);
      } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
        list.add(Value.getStringValue(action.toString()));
        logger.fine("Getting Property " + name + " : "
                + action.toString());
        return new SimpleProperty(list);
      // Disable ACLs for the 3.2.4 release
      // } else if (SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
      //   addPrincipals(list, name, document.getPermissions().getAllowUsers());
      //   return new SimpleProperty(list);
      // } else if (SpiConstants.PROPNAME_ACLDENYUSERS.equals(name)) {
      //   addPrincipals(list, name, document.getPermissions().getDenyUsers());
      //   return new SimpleProperty(list);
      // } else if (SpiConstants.PROPNAME_ACLGROUPS.equals(name)) {
      //   addPrincipals(list, name, document.getPermissions().getAllowGroups());
      //        return new SimpleProperty(list);
      // } else if (SpiConstants.PROPNAME_ACLDENYGROUPS.equals(name)) {
      //   addPrincipals(list, name, document.getPermissions().getDenyGroups());
      //   return new SimpleProperty(list);
      } else if (name.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX)) {
        return null;
      } else {
        document.getProperty(name, list);
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

  /*
   * Helper method to add allow/deny users and groups to property using default
   * principal type, namespace and case sensitivity.
   */
  private void addPrincipals(List<Value> list, String propName,
      Set<String> names) {
    FileUtil.addPrincipals(list, PrincipalType.UNKNOWN, globalNamespace, names,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    logger.log(Level.FINEST, "Getting {0}: {1}",
        new Object[] { propName, names});
  }

  public Set<String> getPropertyNames() throws RepositoryDocumentException {
    Set<String> properties = new HashSet<String>();
    if (SpiConstants.ActionType.DELETE.equals(action)) {
      // return empty set of property names
      return properties;
    }
    // Disable ACLs for the 3.2.4 release
    // properties.add(SpiConstants.PROPNAME_ACLUSERS);
    // properties.add(SpiConstants.PROPNAME_ACLDENYUSERS);
    // properties.add(SpiConstants.PROPNAME_ACLGROUPS);
    // properties.add(SpiConstants.PROPNAME_ACLDENYGROUPS);

    fetch();
    Set<String> documentProperties = document.getPropertyNames();
    for (String property : documentProperties) {
      if (property != null) {
        if (included_meta.size() != 0) {
          // includeMeta - excludeMeta
          if ((!excluded_meta.contains(property) && included_meta.contains(property))) {
            properties.add(property);
          }
        } else {
          // superSet - excludeMeta
          if ((!excluded_meta.contains(property) || included_meta.contains(property))) {
            properties.add(property);
          }
        }
      }
    }
    // TODO(jlacey): Add logging for property names in Connector Manager.
    logger.log(Level.FINEST, "Property names: {0}", properties);

    return properties;
  }
}
