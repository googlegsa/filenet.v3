package com.google.enterprise.connector.file;

public class FileInstantiator {

	public static boolean isFileJavaAPIavailable = true;
	public static String QUERY_STRING_UNBOUNDED_DEFAULT = " <?xml version=\"1.0\" ?><request>"+
	   " <objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>"+
	   "<querystatement>SELECT id, DateLastModified  FROM Document WHERE IsCurrentVersion=true ORDER BY DateLastModified;</querystatement>"+
	   //"<options maxrecords=\"10000\"/>"+
	   "</request> ";
	public static String QUERY_STRING_BOUNDED_DEFAULT = " <?xml version=\"1.0\" ?><request>"+
	   " <objectstores mergeoption=\"none\"><objectstore id=\"{0}\"/></objectstores>"+
	   "<querystatement>SELECT id, DateLastModified  FROM Document WHERE DateLastModified > {1} ORDER BY DateLastModified;</querystatement>"+
	   //"<options maxrecords=\"10000\"/>"+
	   "</request> ";
	

}
