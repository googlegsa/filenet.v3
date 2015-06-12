// Copyright 2014 Google Inc. All Rights Reserved.
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

import static com.google.enterprise.connector.filenet4.ObjectMocks.mockDocument;
import static com.google.enterprise.connector.filenet4.ObjectMocks.newObjectStore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.google.enterprise.connector.filenet4.EngineSetMocks.IndependentObjectSetMock;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.PrincipalValue;

import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.security.AccessPermission;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AclDocumentTest {
  private static final int VIEW_ACCESS_RIGHTS =
      AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT;

  private static final String expectedId =
      "{AAAAAAAA-0000-0000-0000-000000000000}";
  private static final String timeStr = "2014-02-11T08:15:30.129";

  private FileConnector connector;
  private List<AccessPermission> directAces;
  private List<AccessPermission> defaultAces;
  private List<AccessPermission> templateAces;
  private List<AccessPermission> parentAces;

  @Before
  public void setUp() throws Exception {
    directAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_DIRECT);
    defaultAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_DEFAULT);
    templateAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_TEMPLATE);
    parentAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_PARENT);

    connector = TestObjectFactory.newFileConnector();
  }

  @SafeVarargs
  private final DocumentList getDocumentList(String[][] entries,
      List<AccessPermission>... perms) throws Exception {
    MockObjectStore objectStore = newObjectStore(DatabaseType.MSSQL);
    IndependentObjectSetMock objectSet =
        getObjectSet(objectStore, entries, perms);
    return new FileDocumentList(objectSet, new EmptyObjectSet(),
        new EmptyObjectSet(), objectStore, connector,
        new SimpleTraversalContext(), null);
  }

  @SafeVarargs
  private final IndependentObjectSetMock getObjectSet(
      MockObjectStore objectStore, String[][] entries,
      List<AccessPermission>... perms) {
    List<IndependentObject> objectList = new ArrayList<>(entries.length);
    for (String[] entry : entries) {
      objectList.add(mockDocument(objectStore, entry[0], entry[1], true,
              TestObjectFactory.newPermissionList(perms)));
    }
    return new IndependentObjectSetMock(objectList);
  }

  /**
   * Tests document containing no parent ACL document, no inherit-from field.
   */
  @Test
  public void documentContainsDirectDefaultAcls() throws Exception {
    String[][] data = {{expectedId, timeStr}};
    @SuppressWarnings("unchecked") DocumentList doclist =
        getDocumentList(data, directAces, defaultAces);

    Document doc = doclist.nextDocument();
    assertTrue(doc instanceof FileDocument);
    Property inheritFrom =
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID);
    assertNull(inheritFrom);
    assertDocContainsDirectDefaultAces(doc, 0);

    assertNull("Document should not have TMPL or FLDR ACL document",
        doclist.nextDocument());
  }

  /**
   * Tests document inheriting from TMPL docid and TMPL-Parent-ACL document
   * existed.
   */
  @Test
  public void documentContainsDirectDefaultTemplateAcls() throws Exception {
    String[][] data = {{expectedId, timeStr}};
    @SuppressWarnings("unchecked") DocumentList doclist =
        getDocumentList(data, directAces, defaultAces, templateAces);

    Document doc = doclist.nextDocument();
    assertTrue(doc instanceof FileDocument);
    assertDocInheritFrom(doc, expectedId + AclDocument.SEC_POLICY_POSTFIX);
    assertDocContainsDirectDefaultAces(doc, 0);

    Document acl = doclist.nextDocument();
    assertTrue(acl instanceof AclDocument);
    assertEquals(expectedId + AclDocument.SEC_POLICY_POSTFIX,
        Value.getSingleValueString(acl, SpiConstants.PROPNAME_DOCID));
    assertAclContainsAces(acl, PermissionSource.SOURCE_TEMPLATE, 0);

    assertNull(doclist.nextDocument());
  }

  /**
   * Tests document inheriting from FLDR docid and FLDR-Parent-ACL document
   * existed.
   */
  @Test
  public void documentContainsDirectDefaultParentAcls() throws Exception {
    String[][] data = {{expectedId, timeStr}};
    @SuppressWarnings("unchecked") DocumentList doclist =
        getDocumentList(data, directAces, defaultAces, parentAces);

    Document doc = doclist.nextDocument();
    assertTrue(doc instanceof FileDocument);
    assertDocInheritFrom(doc, expectedId + AclDocument.SEC_FOLDER_POSTFIX);
    assertDocContainsDirectDefaultAces(doc, 0);

    Document acl = doclist.nextDocument();
    assertTrue(acl instanceof AclDocument);
    assertEquals(expectedId + AclDocument.SEC_FOLDER_POSTFIX,
        Value.getSingleValueString(acl, SpiConstants.PROPNAME_DOCID));
    assertAclContainsAces(acl, PermissionSource.SOURCE_PARENT, 0);

    assertNull(doclist.nextDocument());
  }

  /**
   * Tests document inheriting from TMPL docid, TMPL-Parent-ACL document
   * inheriting from FLDR-Parent-ACL document and FLDR-Parent-Document existed.
   */
  @Test
  public void documentContainsDirectDefaultTemplateParentAcls()
      throws Exception {
    String[][] data = {{expectedId, timeStr}};
    @SuppressWarnings("unchecked") DocumentList doclist =
        getDocumentList(data, directAces, defaultAces, templateAces,
            parentAces);

    Document doc = doclist.nextDocument();
    assertTrue(doc instanceof FileDocument);
    assertDocInheritFrom(doc, expectedId + AclDocument.SEC_POLICY_POSTFIX);
    assertDocContainsDirectDefaultAces(doc, 0);

    Document fldrAcl = doclist.nextDocument();
    assertTrue(fldrAcl instanceof AclDocument);
    assertAclContainsAces(fldrAcl, PermissionSource.SOURCE_PARENT, 0);

    Document tmplAcl = doclist.nextDocument();
    assertTrue(tmplAcl instanceof AclDocument);
    assertDocInheritFrom(tmplAcl, expectedId + AclDocument.SEC_FOLDER_POSTFIX);
    assertAclContainsAces(tmplAcl, PermissionSource.SOURCE_TEMPLATE, 0);

    assertNull(doclist.nextDocument());
  }

  private void assertDocInheritFrom(Document doc, String expectedDocId)
      throws Exception {
    assertEquals(expectedDocId, Value.getSingleValueString(doc,
            SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
  }

  private void assertDocContainsDirectDefaultAces(Document doc, int index)
      throws Exception {
    String prefixDirect =
        PermissionSource.SOURCE_DIRECT.toString();
    String prefixDefault =
        PermissionSource.SOURCE_DEFAULT.toString();

    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLUSERS,
        prefixDirect + "_allow_user_" + index,
        prefixDefault + "_allow_user_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLDENYUSERS,
        prefixDirect + "_deny_user_" + index,
        prefixDefault + "_deny_user_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLGROUPS,
        prefixDirect + "_allow_group_" + index,
        prefixDefault + "_allow_group_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLDENYGROUPS,
        prefixDirect + "_deny_group_" + index,
        prefixDefault + "_deny_group_" + index);
  }

  private void assertAclContainsAces(Document doc, PermissionSource permSrc,
      int index) throws Exception {
    String prefix = permSrc.toString();
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLUSERS,
        prefix + "_allow_user_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLDENYUSERS,
        prefix + "_deny_user_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLGROUPS,
        prefix + "_allow_group_" + index);
    assertDocContainsAce(doc, SpiConstants.PROPNAME_ACLDENYGROUPS,
        prefix + "_deny_group_" + index);
  }

  private void assertDocContainsAce(Document doc, String propName,
      String... grantees) throws Exception {
    Set<String> propValues = getPropertyValues(doc.findProperty(propName));
    assertEquals(Sets.newHashSet(grantees), propValues);
  }

  private Set<String> getPropertyValues(Property prop) throws Exception {
    Set<String> values = Sets.newHashSet();
    PrincipalValue v;
    while ((v = (PrincipalValue) prop.nextValue()) != null) {
      values.add(v.getPrincipal().getName());
    }
    return values;
  }
}
