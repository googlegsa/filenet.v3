package com.google.enterprise.connector.fileWhereClause;

import java.util.HashSet;

public class FileNETConnection {

	public static String userName = "FNCE_gdc02";

	public static String password = "Admin1234";

	public static String objectStoreName = "DemoObjectStore";//"GSA_Filenet";

	public static String appId = "FnConnector";

	public static String credTag = "Clear";

	public static String displayUrl = "http://gdc02.persistent.co.in:8080//Workplace/getContent";

	public static String objectFactory = "com.google.enterprise.connector.file.filejavawrap.FnObjectFactory";

	public static String pathToWcmApiConfig = "WcmApiConfig.properties";

	public static String completePathToWcmApiConfig = "C:\\Program Files\\GoogleConnectors\\FileNET1\\Tomcat\\webapps\\connector-manager\\WEB-INF\\WcmApiConfig.properties";

	public static String wrongObjectStoreName = "GSA_Filen";

	public static String additionalWhereClause = "and Document.This INSUBFOLDER '/testdata'";

	
	public static HashSet included_meta = null;
	static {
		included_meta = new HashSet();
		included_meta.add("ClassificationStatus");
		included_meta.add("ContentRetentionDate");
		included_meta.add("ContentSize");
		included_meta.add("CurrentState");
		included_meta.add("DateCreated");
		included_meta.add("DateLastModified");
		included_meta.add("DocumentTitle");
		included_meta.add("Id");
		included_meta.add("IsCurrentVersion");
		included_meta.add("IsFrozenVersion");
		included_meta.add("IsReserved");
		included_meta.add("LastModifier");
		included_meta.add("LockTimeout");
		included_meta.add("LockToken");
		included_meta.add("MajorVersionNumber");
		included_meta.add("MimeType");
		included_meta.add("MinorVersionNumber");
		included_meta.add("Name");
		included_meta.add("Owner");
		included_meta.add("StorageLocation");
		included_meta.add("VersionStatus");

	}

	public static HashSet excluded_meta = null;

	static {
		excluded_meta = new HashSet();
		excluded_meta.add("AccessMask");
		excluded_meta.add("ActiveMarkings");
		excluded_meta.add("Annotations");
		excluded_meta.add("AuditedEvents");
		excluded_meta.add("ClassDescription");
		excluded_meta.add("ContentElements");
		excluded_meta.add("ContentElementsPresent");
		excluded_meta.add("CreatePending");
		excluded_meta.add("CurrentVersion");
		excluded_meta.add("DateContentLastAccessed");
		excluded_meta.add("DeletePending");
		excluded_meta.add("DestinationDocuments");
		excluded_meta.add("DocumentLifecyclePolicy");
		excluded_meta.add("EntryTemplateId");
		excluded_meta.add("EntryTemplateLaunchedWorkflowNumber");
		excluded_meta.add("EntryTemplateObjectStoreName");
		excluded_meta.add("FoldersFiledIn");
		excluded_meta.add("IsInExceptionState");
		excluded_meta.add("IsVersioningEnabled");
		excluded_meta.add("LockOwner");
		excluded_meta.add("ObjectStore");
		excluded_meta.add("ObjectType");
		excluded_meta.add("OIID");
		excluded_meta.add("OwnerDocument");
		excluded_meta.add("PendingOperation");
		excluded_meta.add("Properties");
		excluded_meta.add("PublicationInfo");
		excluded_meta.add("ReleasedVersion");
		excluded_meta.add("Reservation");
		excluded_meta.add("ReservationType");
		excluded_meta.add("SecurityParent");
		excluded_meta.add("SecurityPolicy");
		excluded_meta.add("SourceDocument");
		excluded_meta.add("StoragePolicy");
		excluded_meta.add("UpdatePending");
		excluded_meta.add("VersionSeries");
		excluded_meta.add("WorkflowSubscriptions");
	}

}
