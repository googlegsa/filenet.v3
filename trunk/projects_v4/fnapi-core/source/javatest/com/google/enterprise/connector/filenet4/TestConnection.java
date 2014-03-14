// Copyright 2009 Google Inc.
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

import com.filenet.api.constants.PropertyNames;

import java.util.HashSet;
import java.util.Properties;

public class TestConnection {
  public static final int batchSize;
  public static final String domain;
  public static final String adminUsername;
  public static final String adminPassword;
  public static final String username;
  public static final String password;
  public static final String wrongPassword;
  public static final String uri;
  public static final String objectStore;
  public static final String objectFactory;
  public static final String displayURL;
  public static final String incorrectDisplayURL;
  public static final String property_wasp_location;
  public static final String wsi_path;
  public static final String repeatedSlashContentEngineURL;
  public static final String currentUserContext;
  public static final String wrong_additional_where_clause;
  public static final String additional_where_clause;
  public static final String additional_delete_where_clause;
  public static final String where_clause_error_message;
  public static final String workplace_url__error_message;

  // docId1 is Doc1 available for Administrator only
  public static final String docId1;

  // docVsId1
  public static final String docVsId1;

  // docId2 is TestAuthentication available for Administrator and JPasquon
  // user
  public static final String docId2;

  // docVsId1
  public static final String docVsId2;

  // docId3 is Herb Alpert & The Tijuana Brass - Greatest Hits - 08 - Tijuana
  // Taxi.mp3 available for all user
  public static final String docId3;

  // docVsId1
  public static final String docVsId3;

  // docId4 isAuthentication in P8 4.0.pdf available for all user
  public static final String docId4;

  // docVsId1
  public static final String docVsId4;

  // CHeckpoint of the last modified document TestAuthentication.pdf
  public static final String checkpoint1;
  public static final String checkpoint2;

  static {
    Properties props = System.getProperties();
    batchSize = Integer.parseInt(props.getProperty("batchSize"));
    domain = props.getProperty("domain");
    adminUsername = props.getProperty("adminUsername");
    adminPassword = props.getProperty("adminPassword");
    username = props.getProperty("username");
    password = props.getProperty("password");
    wrongPassword = props.getProperty("wrongPassword");
    uri = props.getProperty("wsUri");
    objectStore = props.getProperty("objectStoreName");
    objectFactory = props.getProperty("objectFactoryClass");
    displayURL = props.getProperty("displayUrl");
    incorrectDisplayURL = props.getProperty("incorrectDisplayUrl");
    property_wasp_location = props.getProperty("waspLocation");
    wsi_path = props.getProperty("wsiPath");
    repeatedSlashContentEngineURL = 
        props.getProperty("repeatedSlashContentEngineURL");
    currentUserContext = props.getProperty("currentUserContext");

    wrong_additional_where_clause = 
        props.getProperty("wrong_additional_where_clause");
    additional_where_clause = props.getProperty("additional_where_clause");
    additional_delete_where_clause =
        props.getProperty("additional_delete_where_clause");
    where_clause_error_message = 
        props.getProperty("where_clause_error_message");
    workplace_url__error_message = 
        props.getProperty("workplace_url__error_message");

    docId1 = props.getProperty("docId1");
    docId2 = props.getProperty("docId2");
    docId3 = props.getProperty("docId3");
    docId4 = props.getProperty("docId4");
    docVsId1 = props.getProperty("docVsId1");
    docVsId2 = props.getProperty("docVsId2");
    docVsId3 = props.getProperty("docVsId3");
    docVsId4 = props.getProperty("docVsId4");
    checkpoint1 = props.getProperty("checkpoint1");
    checkpoint2 = props.getProperty("checkpoint2");
  }
  
  public static HashSet<String> included_meta = null;
  static {
    included_meta = new HashSet<String>();
    included_meta.add("ClassificationStatus");
    included_meta.add("ContentRetentionDate");
    included_meta.add("ContentSize");
    included_meta.add("CurrentState");
    included_meta.add("DateCreated");
    included_meta.add("DocumentTitle");
    included_meta.add("IsCurrentVersion");
    included_meta.add("IsFrozenVersion");
    included_meta.add("IsReserved");
    included_meta.add("LastModifier");
    included_meta.add("LockTimeout");
    included_meta.add("LockToken");
    included_meta.add("MajorVersionNumber");
    included_meta.add("MinorVersionNumber");
    included_meta.add("Name");
    included_meta.add("Owner");
    included_meta.add("StorageLocation");
    included_meta.add("VersionStatus");
    included_meta.add(PropertyNames.PERMISSIONS);
  }

  public static HashSet<String> excluded_meta = null;

  static {
    excluded_meta = new HashSet<String>();
    excluded_meta.add("ActiveMarkings");
    excluded_meta.add("Annotations");
    excluded_meta.add("AuditedEvents");
    excluded_meta.add("ContentElementsPresent");
    excluded_meta.add("CurrentVersion");
    excluded_meta.add("DateContentLastAccessed");
    excluded_meta.add("DestinationDocuments");
    excluded_meta.add("DocumentLifecyclePolicy");
    excluded_meta.add("EntryTemplateId");
    excluded_meta.add("EntryTemplateLaunchedWorkflowNumber");
    excluded_meta.add("EntryTemplateObjectStoreName");
    excluded_meta.add("FoldersFiledIn");
    excluded_meta.add("IsInExceptionState");
    excluded_meta.add("IsVersioningEnabled");
    excluded_meta.add("LockOwner");
    excluded_meta.add("OwnerDocument");
    excluded_meta.add("PublicationInfo");
    excluded_meta.add("ReleasedVersion");
    excluded_meta.add("Reservation");
    excluded_meta.add("ReservationType");
    excluded_meta.add("SecurityParent");
    excluded_meta.add("SecurityPolicy");
    excluded_meta.add("StoragePolicy");
    excluded_meta.add("WorkflowSubscriptions");
    excluded_meta.add("CompoundDocumentState");
    excluded_meta.add("ComponentBindingLabel");
    excluded_meta.add("StorageArea");
    excluded_meta.add("ChildRelationships");
    excluded_meta.add("This");
    excluded_meta.add("IgnoreRedirect");
    excluded_meta.add("IndexationId");
    excluded_meta.add("DependentDocuments");
    excluded_meta.add("Permissions");
    excluded_meta.add("Versions");
    excluded_meta.add("ParentDocuments");
    excluded_meta.add("ParentRelationships");
    excluded_meta.add("Containers");
    excluded_meta.add("ChildDocuments");
    excluded_meta.add("Creator");
    excluded_meta.add("PublishingSubsidiaryFolder");
  }

  public static String[][] type = {
          { "ClassificationStatus", "LONG" },
          { "ContentRetentionDate", "DATE" },
          { "ContentSize", "DOUBLE" },
          { "CurrentState", "STRING" },
          { "DateCreated", "DATE" },
          { "DocumentTitle", "STRING" },
          { "IsCurrentVersion", "BOOLEAN" },
          { "IsFrozenVersion", "BOOLEAN" },
          { "IsReserved", "BOOLEAN" },
          { "LastModifier", "STRING" },
          { "LockTimeout", "LONG" },
          { "LockToken", "GUID" },
          { "MajorVersionNumber", "LONG" },
          { "MinorVersionNumber", "LONG" },
          { "Name", "STRING" },
          { "Owner", "STRING" },
          { "StorageLocation", "STRING" },
          { "VersionStatus", "LONG" },
          { "Id", "GUID" },
          { "ClassDescription", "OBJECT" },
          { "ContentElements", "OBJECT" },
          { "DateLastModified", "DATE" },
          { "MimeType", "STRING" },
          { "VersionSeries", "OBJECT" },
          { "ActiveMarkings", "OBJECT" },
          { "Annotations", "OBJECT" },
          { "AuditedEvents", "OBJECT" },
          { "ContentElementsPresent", "STRING" },
          { "CurrentVersion", "OBJECT" },
          { "DateContentLastAccessed", "DATE" },
          { "DestinationDocuments", "null" },
          { "DocumentLifecyclePolicy", "OBJECT" },
          { "EntryTemplateId", "GUID" },
          { "EntryTemplateLaunchedWorkflowNumber", "STRING" },
          { "EntryTemplateObjectStoreName", "STRING" },
          { "FoldersFiledIn", "OBJECT" },
          { "IsInExceptionState", "BOOLEAN" },
          { "IsVersioningEnabled", "BOOLEAN" },
          { "LockOwner", "STRING" },
          { "OwnerDocument", "OBJECT" },
          { "PublicationInfo", "BINARY" },
          { "ReleasedVersion", "OBJECT" },
          { "Reservation", "OBJECT" },
          { "ReservationType", "LONG" },
          { "SecurityParent", "OBJECT" },
          { "SecurityPolicy", "OBJECT" },
          { "StoragePolicy", "OBJECT" },
          { "WorkflowSubscriptions", "OBJECT" },
          { "CompoundDocumentState", "LONG" },
          { "ComponentBindingLabel", "STRING" },
          { "StorageArea", "OBJECT" },
          { "ChildRelationships", "OBJECT" },
          { "This", "OBJECT" },
          { "IgnoreRedirect", "BOOLEAN" },
          { "IndexationId", "GUID" },
          { "DependentDocuments", "OBJECT" },
          { "Permissions", "OBJECT" },
          { "Versions", "OBJECT" },
          { "ParentDocuments", "OBJECT" },
          { "ParentRelationships", "OBJECT" },
          { "Containers", "OBJECT" },
          { "ChildDocuments", "OBJECT" },
          { "Creator", "STRING" },
          { "PublishingSubsidiaryFolder", "OBJECT" },
          };

}
