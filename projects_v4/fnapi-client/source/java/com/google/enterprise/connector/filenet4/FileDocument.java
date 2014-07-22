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
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;

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

  private final IId docId;
  private final IObjectStore objectStore;
  private final FileConnector connector;

  private IDocument document = null;
  private String vsDocId;
  private Permissions permissions;

  public FileDocument(IId docId, IObjectStore objectStore,
      FileConnector connector) {
    this.docId = docId;
    this.objectStore = objectStore;
    this.connector = connector;
  }

  private void fetch() throws RepositoryException {
    if (document != null) {
      return;
    }
    document = (IDocument) objectStore.fetchObject(ClassNames.DOCUMENT, docId,
        FileUtil.getDocumentPropertyFilter(connector.getIncludedMeta()));
    logger.log(Level.FINE, "Fetch document for DocId {0}", docId);
    vsDocId = document.getVersionSeries().get_Id().toString();
    logger.log(Level.FINE, "VersionSeriesID for document is: {0}", vsDocId);
    permissions =
        new Permissions(document.get_Permissions(), document.get_Owner());
  }

  @Override
  public Property findProperty(String name) throws RepositoryException {
    LinkedList<Value> list = new LinkedList<Value>();

    fetch();
    if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getBinaryValue(document.getContent()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getStringValue(connector.getWorkplaceDisplayUrl()
              + vsDocId));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getBooleanValue(connector.isPublic()));
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
      list.add(Value.getStringValue(SpiConstants.ActionType.ADD.toString()));
      logger.fine("Getting Property " + name + " : "
          + SpiConstants.ActionType.ADD.toString());
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
      addPrincipals(list, name, permissions.getAllowUsers());
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLDENYUSERS.equals(name)) {
      addPrincipals(list, name, permissions.getDenyUsers());
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLGROUPS.equals(name)) {
      addPrincipals(list, name, permissions.getAllowGroups());
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLDENYGROUPS.equals(name)) {
      addPrincipals(list, name, permissions.getDenyGroups());
      return new SimpleProperty(list);
    } else if (name.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX)) {
      return null;
    } else {
      document.getProperty(name, list);
      return new SimpleProperty(list);
    }
  }

  /*
   * Helper method to add allow/deny users and groups to property using default
   * principal type, namespace and case sensitivity.
   */
  private void addPrincipals(List<Value> list, String propName,
      Set<String> names) {
    FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
        connector.getGoogleGlobalNamespace(), names,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    logger.log(Level.FINEST, "Getting {0}: {1}",
        new Object[] { propName, names});
  }

  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    Set<String> properties = new HashSet<String>();

    properties.add(SpiConstants.PROPNAME_ACLUSERS);
    properties.add(SpiConstants.PROPNAME_ACLDENYUSERS);
    properties.add(SpiConstants.PROPNAME_ACLGROUPS);
    properties.add(SpiConstants.PROPNAME_ACLDENYGROUPS);

    fetch();
    Set<String> documentProperties = document.getPropertyNames();
    for (String property : documentProperties) {
      if (property != null) {
        if (connector.getIncludedMeta().size() != 0) {
          // includeMeta - excludeMeta
          if ((!connector.getExcludedMeta().contains(property)
              && connector.getIncludedMeta().contains(property))) {
            properties.add(property);
          }
        } else {
          // superSet - excludeMeta
          if ((!connector.getExcludedMeta().contains(property))) {
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
