// Copyright 2007-2008 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileNetTestCase;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IActiveMarkingList;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IUser;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.filenet4.mock.MockUtil;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * JUnit TestCases related to Core Document class.
 *
 * @author pankaj_chouhan
 */
public class FnDocumentTest extends FileNetTestCase {
  FileSession fs;
  IObjectStore ios;
  IConnection conn;
  IObjectFactory iof;
  IDocument fd, fd2;
  IVersionSeries vs;
  IUserContext uc;
  IUser user;

  protected void setUp() throws RepositoryLoginException,
          RepositoryException, InstantiationException,
          IllegalAccessException, ClassNotFoundException {
    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession) connec.login();

    iof = (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
    IConnection conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    // Domain domain = Factory.Domain.getInstance(conn.getConnection(),
    // null);
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.adminUsername, TestConnection.adminPassword);

    fd = (IDocument) ios.fetchObject(ClassNames.DOCUMENT, TestConnection.docId1,
        FileUtil.getDocumentPropertyFilter(TestConnection.included_meta));

    uc = new FnUserContext(conn);
    user = uc.authenticate(TestConnection.username, TestConnection.password);
    vs = fd.getVersionSeries();
    fd2 = vs.getReleasedVersion();
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyName()'
   */
  public void testGetPropertyNames() throws RepositoryException {
    Set<String> propNames = fd.getPropertyNames();
    assertNotNull(propNames);
    for (String includedMeta : TestConnection.included_meta) {
      assertTrue(includedMeta + " is not included",
          propNames.contains(includedMeta));
    }
  }

  /*
   * Test expected data type of the property value computed by the
   * IDocument.getProperty(String, List) method.
   */
  public void testGetPropertyType() throws RepositoryException {
    String[][] typeArray = TestConnection.type;

    Set<String> meta = TestConnection.included_meta;
    meta.add("Id");
    meta.add("ClassDescription");
    meta.add("ContentElements");
    meta.add("DateLastModified");
    meta.add("MimeType");
    meta.add("VersionSeries");
    for (String property : meta) {
      List<Value> valueList = new LinkedList<Value>();
      fd.getProperty(property, valueList);
      // Skip null or empty value
      if (valueList == null || valueList.size() == 0) {
        continue;
      }
      String typeSet = null;
      for (int i = 0; i < typeArray.length; i++) {
        if (typeArray[i][0] == property) {
          typeSet = typeArray[i][1];
          break;
        }
      }
      assertNotNull(typeSet);
      if ("GUID".equals(typeSet)) {
        // GUID is stored as StringValue type
        typeSet = "STRING";
      }
      Value value = valueList.get(0);
      assertTrue(value.getClass().getName().toUpperCase().contains(typeSet));
    }
  }

  public void testGetVersionSeries() throws RepositoryException {
    IVersionSeries vs = fd.getVersionSeries();
    assertEquals("{" + TestConnection.docVsId1 + "}", vs.getId());
  }

  public void testGetId() throws RepositoryException {
    assertEquals("{" + TestConnection.docId1 + "}", fd.getId());
  }

  public void testGetPermissions() throws RepositoryException {
    IPermissions perms = fd.getPermissions();
    assertNotNull(perms);
    boolean authorized = perms.authorize(MockUtil.createAdministratorUser());
    assertTrue("User is not authorized", authorized);
  }

  public void testMarkingPermissions() throws RepositoryException {
    IVersionSeries versionSeries =
        (IVersionSeries) ios.getObject(ClassNames.VERSION_SERIES,
            TestConnection.docVsId1);
    IDocument doc = versionSeries.getReleasedVersion();
    IActiveMarkingList activeMarkingList = doc.getActiveMarkings();
    assertNotNull("Active marking is null", activeMarkingList);
    assertTrue(user.getName() + " is not authorized by document's marking",
        activeMarkingList.authorize(user));
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getContent()'
   */
  public void testGetContent() throws RepositoryException {
    uc.authenticate(TestConnection.adminUsername, TestConnection.adminPassword);
    InputStream is = fd.getContent();
    assertNotNull(is);
    assertTrue(is instanceof InputStream);
  }

  /* Helper method to compute field names by type */
  private Set<String> getFieldNames(String type) {
    Set<String> fieldNames = new HashSet<String>();
    for (int i = 0; i < TestConnection.type.length; i++) {
      if (type.equalsIgnoreCase(TestConnection.type[i][1])) {
        fieldNames.add(TestConnection.type[i][0]);
      }
    }
    return fieldNames;
  }

  /* Test FnDocument.getPropertyStringValue method */
  public void testGetPropertyStringValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("STRING");
    assertFalse(fieldNames.isEmpty());

    // Remove empty fields
    fieldNames.remove("ComponentBindingLabel");
    fieldNames.remove("Creator");
    fieldNames.remove("CurrentState");
    fieldNames.remove("EntryTemplateLaunchedWorkflowNumber");
    fieldNames.remove("EntryTemplateObjectStoreName");
    fieldNames.remove("LockOwner");
    fieldNames.remove("StorageLocation");
    fieldNames.remove("ContentElementsPresent");

    // Test non-empty fields
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyStringValue(fieldName, list);
      assertFalse(fieldName + " string value is empty", list.isEmpty());
    }
  }

  public void testGetPropertyGuidValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("GUID");
    assertFalse(fieldNames.isEmpty());

    // Remove empty fields
    fieldNames.remove("IndexationId");
    fieldNames.remove("EntryTemplateId");
    fieldNames.remove("LockToken");

    // Test non-empty fields
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyGuidValue(fieldName, list);
      assertFalse(fieldName + " guid value is null", list.isEmpty());
    }
  }

  public void testGetPropertyLongValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("LONG");
    assertFalse(fieldNames.isEmpty());

    // Remove empty fields
    fieldNames.remove("CompoundDocumentState");
    fieldNames.remove("LockTimeout");
    fieldNames.remove("ReservationType");

    // Test non-empty fields
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyLongValue(fieldName, list);
      assertFalse(fieldName + " long value is empty", list.isEmpty());
    }
  }

  public void testGetPropertyDoubleValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("DOUBLE");
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyDoubleValue(fieldName, list);
      assertFalse(fieldName + " double value is empty", list.isEmpty());
    }
  }

  public void testGetPropertyDateValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("DATE");
    assertFalse(fieldNames.isEmpty());

    // Remove empty fields
    fieldNames.remove("ContentRetentionDate");
    fieldNames.remove("DateContentLastAccessed");

    // Test non-empty fields
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyDateValue(fieldName, list);
      assertFalse(fieldName + " date value is empty", list.isEmpty());
    }
  }

  public void testGetPropertyBooleanValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("BOOLEAN");
    assertFalse(fieldNames.isEmpty());

    // Remove empty fields
    fieldNames.remove("IsInExceptionState");
    fieldNames.remove("IsVersioningEnabled");
    fieldNames.remove("IgnoreRedirect");

    // Test non-empty fields
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyBooleanValue(fieldName, list);
      assertFalse(fieldName + " boolean value is empty", list.isEmpty());
    }
  }

  public void testGetPropertyBinaryValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("BINARY");
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyBinaryValue(fieldName, list);
      assertFalse(fieldName + " binary value is empty", list.isEmpty());
    }
  }
}
