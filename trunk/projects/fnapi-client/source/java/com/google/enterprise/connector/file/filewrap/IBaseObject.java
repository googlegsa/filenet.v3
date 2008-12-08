package com.google.enterprise.connector.file.filewrap;

import com.google.enterprise.connector.spi.RepositoryDocumentException;

public interface IBaseObject {

	public static final int TYPE_PROPERTYDESCRIPTION = 24;

	public static final int TYPE_MULTIPLEVALUES = 1066;

	public static final int TYPE_OBJECTSET = 14;

	public static final int TYPE_PROPERTYDESCRIPTIONS = 1142;

	public static final int TYPE_CLASSDESCRIPTION = 25;

	public static final int TYPE_OBJECT_STORE = 4;

	public static final int TYPE_DOCUMENT = 1;

	public static final int TYPE_VERSIONSERIES = 1140;

	public static final int TYPE_FOLDER = 2;

	public static final int TYPE_REFERENTIAL_CONTAINMENT_RELATIONSHIP = 1124;

	public static final int TYPE_CONTENT_TRANSFER = 1038;

	public static final int TYPE_CONTENT_REFERENCE = 1035;

	public static final int TYPE_CUSTOMOBJECT = 15;

	public static final int TYPE_TRANSIENT = 1137;

	public static final int TYPE_CLASSDEFINITION = 1027;

	public static final int TYPE_PROPERTY_DEFINITION = 1077;

	public static final int TYPE_LOCALIZED_STRING = 1075;

	public static final int TYPE_TABLE_DEFINITION = 1135;

	public static final int TYPE_COLUMN_DEFINITION = 1028;

	public static final int TYPE_CHOICELIST = 1026;

	public static final int TYPE_CHOICE = 1025;

	public static final int TYPE_DYNAMIC_REFERENTIAL_CONTAINMENT_RELATIONSHIP = 1050;

	public static final int TYPE_ANNOTATION = 3;

	public static final int TYPE_SUBSCRIPTION = 1144;

	public static final int TYPE_DOCUMENT_LIFECYCLE_POLICY = 1148;

	public static final int TYPE_DOCUMENTSTATE = 1152;

	public static final int TYPE_DOCUMENT_LIFECYCLE_ACTION = 1154;

	public static final int TYPE_USER = 2000;

	public static final int TYPE_GROUP = 2001;

	public static final int TYPE_READONLY_OBJECT_SET = 2002;

	public static final int TYPE_COMPUTER = 2003;

	public static final int TYPE_LINK = 1156;

	public static final int TYPE_INSTANCE_SUBSCRIPTION = 1165;

	public static final int TYPE_CLASS_SUBSCRIPTION = 1166;

	public static final int TYPE_STORAGE_POLICY = 1037;

	public static final int TYPE_WORKFLOW_INSTANCE_SUBSCRIPTION = 1165;

	public static final int TYPE_WORKFLOW_CLASS_SUBSCRIPTION = 1166;

	public static final int TYPE_EVENT_ACTION = 1143;

	public static final int TYPE_WORKFLOWDEFINITION = 1160;

	public static final int TYPE_DOCUMENT_CLASSIFICATION_ACTION = 1161;

	public static final int TYPE_XML_PROPERTY_MAPPING_SCRIPT = 1162;

	public static final int TYPE_SUBSCRIBED_EVENT = 1204;

	public static final int TYPE_ANY = 0;

	public static final int TYPE_SECURITY_POLICY = 1170;

	public static final int TYPE_SECURITY_TEMPLATE = 1175;

	public static final int TYPE_ENTIRENETWORK = 2004;

	public static final int TYPE_DOMAIN = 2005;

	public static final int TYPE_REALM = 2006;

	public static final int TYPE_OBJSTORE_SERVICE = 2007;

	public static final int TYPE_FILESTORE = 2008;

	public static final int TYPE_CBR_ENGINE_TYPE = 2009;

	public static final int TYPE_CONTENT_MGR_SERVICE = 2010;

	public static final int TYPE_CONTENT_CACHE_SERVICE = 2011;

	public static final int TYPE_PERMISSION = 2012;

	public static final int TYPE_EVENT = 1180;

	public static final int TYPE_FEATURE_ADD_ON = 1202;

	public static final int TYPE_MARKING_SET = 1200;

	public static final int TYPE_ACTIVE_MARKING = 1210;

	public static final int TYPE_AUDIT_DEFINITION = 1181;

	public static final int TYPE_STORED_SEARCH = -100;

	public static final int TYPE_PUBLISH_TEMPLATE = -101;

	public static final int TYPE_PUBLISH_REQUEST = -102;

	public static final int TYPE_STYLE_TEMPLATE = -103;

	public static final int IMPORT_DEFAULT = 0;

	public static final int IMPORT_OBJECT_ID = 1;

	public static final int IMPORT_PERMISSIONS = 2;

	public static final int IMPORT_OWNER = 8;

	public static final int EXPORT_DEFAULT = 0;

	public static final int EXPORT_OBJECT_ID = 1;

	public static final int EXPORT_OBJECT_SECURITY = 2;

	public static final int EXPORT_ENCODED_CONTENT = 16;

	public static final int EXPORT_FULL = 128;

	public String getId() throws RepositoryDocumentException;

}
