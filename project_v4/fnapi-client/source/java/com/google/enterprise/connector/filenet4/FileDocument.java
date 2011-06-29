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
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;

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

    private String docId;
    private IObjectStore objectStore;
    private IDocument document = null;
    private boolean isPublic = false;
    private String displayUrl;
    private String versionId;
    private Date timeStamp;
    private String vsDocId;
    private HashSet included_meta = null;
    private HashSet excluded_meta = null;
    private static Logger logger = null;
    {
        logger = Logger.getLogger(FileDocument.class.getName());
    }
    private SpiConstants.ActionType action;

    public FileDocument(String docId, Date timeStamp, IObjectStore objectStore,
            boolean isPublic, String displayUrl, HashSet included_meta,
            HashSet excluded_meta, SpiConstants.ActionType action) {
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
            HashSet included_meta, HashSet excluded_meta,
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
        logger.log(Level.FINE, "Fetch document for docId " + docId);
        this.vsDocId = document.getVersionSeries().getId(action);
        logger.log(Level.FINE, "VersionSeriesID for document is : "
                + this.vsDocId);
    }

    public Property findProperty(String name)
            throws RepositoryDocumentException {
        LinkedList list = new LinkedList();

        if (SpiConstants.ActionType.ADD.equals(action)) {
            fetch();
            if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(new BinaryValue(document.getContent()));
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(new StringValue(this.displayUrl + vsDocId));
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(BooleanValue.makeBooleanValue(this.isPublic ? true
                        : false));
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                this.document.getPropertyDateValue("DateLastModified", list);
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
                document.getPropertyStringValue("MimeType", list);
                logger.log(Level.FINEST, "Getting property: " + name);
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
                return null;
            } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(new StringValue(vsDocId));
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
                list.add(new StringValue(action.toString()));
                logger.fine("Getting Property " + name + " : "
                        + action.toString());
                return new FileDocumentProperty(name, list);
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

                long timeDateMod = timeStamp.getTime();
                timeStamp.setTime(timeDateMod + 1000);
                tmpCal.setTime(timeStamp);

                DateValue tmpDtVal = new DateValue(tmpCal);
                list.add(tmpDtVal);
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(new StringValue(action.toString()));
                return new FileDocumentProperty(name, list);
            } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
                logger.log(Level.FINEST, "Getting property: " + name);
                list.add(new StringValue(versionId));
                // logger.fine("versionId : " + versionId);
                return new FileDocumentProperty(name, list);
            }
        }
        return new FileDocumentProperty(name, list);
    }

    public Set getPropertyNames() throws RepositoryDocumentException {
        fetch();
        HashSet properties = new HashSet();
        Set documentProperties = this.document.getPropertyName();
        String property;
        for (Iterator iter = documentProperties.iterator(); iter.hasNext();) {
            property = (String) iter.next();
            if (property != null) {
                if (included_meta.size() != 0) {
                    // includeMeta - exludeMeta
                    logger.log(Level.FINE, "Metadata set will be (includeMeta - exludeMeta)");
                    if ((!excluded_meta.contains(property) && included_meta.contains(property))) {
                        properties.add(property);
                    }
                } else {
                    // superSet - exludeMeta
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
