package com.google.enterprise.connector.file;

public class FnMockConnection {

	public static final String userName = "mark";
	public static final String password = "mark";
	public static final String objectStoreName = "SwordEventLog.txt";
	public static final String credTag = "CLEAR";
	public static final String displayUrl = "";
	public static final String objectFactory = "com.google.enterprise.connector.file.filemockwrap.MockFnObjectFactory";
	public static final String pathToWcmApiConfig = "C:\\_dev\\google\\connector\\connector-file\\projects\\testdata\\mocktestdata\\MockRepositoryEventLog7.txt";
	public static final String FN_CHECKPOINT_QUERY_STRING = " AND DateLastModified >= 1970-01-01 01:00:00.020 AND Id > doc2";

	public static final String FN_QUERY_STRING_ENABLE = "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";
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
	

}
