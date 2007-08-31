package com.google.enterprise.connector.file;

import java.util.HashSet;

public class FnConnection {

	public static String userName = "P8Admin";

	public static String password = "UnDeuxTrois456";

	public static String objectStoreName = "GSA_Filenet";

	public static String appId = "file-connector";

	public static String credTag = "Clear";

	public static String displayUrl = "http://swp-vm-fnet352:8080/Workplace/";

	public static String objectFactory = "com.google.enterprise.connector.file.filejavawrap.FnObjectFactory";

	public static String pathToWcmApiConfig = "C:\\_dev\\google\\workspacev3.5\\connector-file\\projects\\webapps\\connector-manager\\WEB-INF\\WcmApiConfig.properties";

	public static String wrongObjectStoreName = "GSA_Filen";

	public static String docId = "{97E02C4F-7E0D-4874-894E-42CDD456AA97}";

	public static String docVsId = "{56042FFC-976E-4F61-8B32-B789218B9324}";

	public static String docId2 = "{B02DE923-DD23-4EB5-B5B2-3EFE9A20EF2F}";

	public static String docId3 = "{F06AA9BB-368C-463C-873E-47EA695BA879}";

	public static String date = "2007-06-05T14:46:19.019";

	public static String dateForResume = "2007-06-25T06:29:27.000";

	public static String docIdTitle = "second02.doc";

	public static String userLambda1 = "P8TestUser@SWORD.FR";

	public static String userLambda2 = "P8TestUser2";

	public static String userLambda3 = "ebouvier";

	public static String userLambdaPassword1 = "p@ssw0rd";

	public static String userLambdaPassword2 = "p@ssw0rd";

	public static String mimeType = "application/msword";

	public static String checkpoint = "{\"uuid\":\"{F06AA9BB-368C-463C-873E-47EA695BA879}\",\"lastModified\":\"2007-06-25T06:29:27.000\"}";

	public static String checkpoint2 = "{\"uuid\":\"{6AC92243-CE5A-430C-A615-868B088202CD}\",\"lastModified\":\"2007-05-07T12:52:17.000\"}";

	public static String additionalWhereClause = "and Document.This INSUBFOLDER '/testdata'";

	public static final String DM_CHECKPOINT_QUERY_STRING = " AND ((DateLastModified>=2007-06-05T14:46:19.019 AND ({97E02C4F-7E0D-4874-894E-42CDD456AA97}>id)) OR DateLastModified>=2007-06-05T14:46:19.019)";

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
