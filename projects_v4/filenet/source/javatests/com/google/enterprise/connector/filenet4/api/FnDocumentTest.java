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

package com.google.enterprise.connector.filenet4.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.MarkingPermissions;
import com.google.enterprise.connector.filenet4.Permissions;
import com.google.enterprise.connector.filenet4.SecurityPrincipalMocks;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.security.User;
import com.filenet.api.util.Id;

import org.junit.Before;
import org.junit.Test;

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
public class FnDocumentTest {
  IObjectStore ios;
  FnDocument fd;
  IUserContext uc;
  User user;

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());

    IObjectFactory iof = (IObjectFactory) Class.forName(
        TestConnection.objectFactory).newInstance();
    IConnection conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.adminUsername, TestConnection.adminPassword);

    fd = (FnDocument) ios.fetchObject(ClassNames.DOCUMENT,
        new Id(TestConnection.docId1),
        FileUtil.getDocumentPropertyFilter(TestConnection.included_meta));

    uc = new FnUserContext(conn);
    user = uc.authenticate(TestConnection.username, TestConnection.password);
  }

  @Test
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
  @Test
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

  @Test
  public void testGetVersionSeries() throws RepositoryException {
    IVersionSeries vs = fd.getVersionSeries();
    assertEquals("{" + TestConnection.docVsId1 + "}", vs.get_Id().toString());
  }

  @Test
  public void testGetId() throws RepositoryException {
    assertEquals("{" + TestConnection.docId1 + "}", fd.get_Id().toString());
  }

  @Test
  public void testGetPermissions() throws RepositoryException {
    Permissions perms = new Permissions(fd.get_Permissions());
    assertNotNull(perms);
    boolean authorized =
        perms.authorize(SecurityPrincipalMocks.createAdministratorUser());
    assertTrue("User is not authorized", authorized);
  }

  @Test
  public void testMarkingPermissions() throws RepositoryException {
    IVersionSeries versionSeries =
        (IVersionSeries) ios.getObject(ClassNames.VERSION_SERIES,
            TestConnection.docVsId1);
    IDocument doc = versionSeries.get_ReleasedVersion();
    ActiveMarkingList activeMarkingList = doc.get_ActiveMarkings();
    assertNotNull("Active marking is null", activeMarkingList);
    assertTrue(user.get_Name() + " is not authorized by document's marking",
        new MarkingPermissions(activeMarkingList, Permissions.getFactory())
        .authorize(user));
  }

  @Test
  public void testGetContent() throws RepositoryException {
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testGetPropertyDoubleValue() throws RepositoryException {
    Set<String> fieldNames = getFieldNames("DOUBLE");
    assertFalse(fieldNames.isEmpty());
    for (String fieldName : fieldNames) {
      LinkedList<Value> list = new LinkedList<Value>();
      fd.getPropertyDoubleValue(fieldName, list);
      assertFalse(fieldName + " double value is empty", list.isEmpty());
    }
  }

  @Test
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

  @Test
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

  @Test
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
