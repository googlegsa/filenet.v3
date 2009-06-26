package com.google.enterprise.connector.file;

import java.util.HashSet;

public class FnMockConnection {

	public static final String userName = "mark";

	public static final String password = "mark";

	public static final String objectStoreName = "SwordEventLog.txt";

	public static final String credTag = "CLEAR";

	public static final String displayUrl = "";

	public static final String objectFactory = "com.google.enterprise.connector.file.filemockwrap.MockFnObjectFactory";

	// public static final String pathToWcmApiConfig =
	// "C:\\_dev\\google\\workspacev3.5\\connector-file\\projects\\testdata\\mocktestdata\\MockRepositoryEventLog7.txt";
	public static final String completePathToWcmApiConfig = "C:\\_dev\\google\\connector\\connector-file-complete\\testdata\\mocktestdata\\SwordEventLog.txt";

	public static final String pathToWcmApiConfig = "SwordEventLog.txt";

	public static final String FN_CHECKPOINT_QUERY_STRING = " AND ((DateLastModified=1970-01-01 01:00:00.020 AND (doc2>id)) OR DateLastModified>1970-01-01 01:00:00.020)";

	public static final String FN_QUERY_STRING_ENABLE = "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

	public static final String additionalWhereClause = "";

	public static String FN_LOGIN_OK1 = "joe";

	public static String FN_LOGIN_OK2 = "mary";

	public static String FN_LOGIN_OK3 = "user1";

	public static String FN_LOGIN_OK4 = "mark";

	public static String FN_LOGIN_OK5 = "bill";

	public static String FN_LOGIN_KO = "machinchouette";

	public static String FN_PWD_OK1 = "joe";

	public static String FN_PWD_OK2 = "mary";

	public static String FN_PWD_OK3 = "user1";

	public static String FN_PWD_OK4 = "mark";

	public static String FN_PWD_OK5 = "bill";

	public static String FN_PWD_KO = "wdfshsgdh";

	public static String FN_ID1 = "users";

	public static String FN_ID2 = "doc2";

	public static String FN_ID3 = "doc3";

	public static String FN_ID4 = "doc10";

	public static String FN_ID5 = "doc26";

	public static HashSet included_meta = null;
	static {
		included_meta = new HashSet();
		included_meta.add("ClassificationStatus");
		included_meta.add("ContentRetentionDate");
		included_meta.add("ContentSize");
		included_meta.add("CurrentState");
		included_meta.add("DateContentLastAccessed");
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
