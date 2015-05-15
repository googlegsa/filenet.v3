// Copyright 2015 Google Inc. All Rights Reserved.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterators;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.filejavawrap.FnId;
import com.google.enterprise.connector.filenet4.filejavawrap.FnObjectList;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IFolder;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.PrincipalValue;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.AccessPermission;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SecurityFolderTraverserTest {
  private static final Date Jan_1_1970 = new Date(72000000L);

  private static final String[][] FOLDERS = {
    {"{FFFFFFFF-0000-0000-0000-000000000001}", "2015-04-01T10:00:00.100-0700"},
    {"{FFFFFFFF-0000-0000-0000-000000000002}", "2015-04-01T11:05:10.200-0700"},
    {"{FFFFFFFF-0000-0000-0000-000000000003}", "2015-04-01T12:10:20.300-0700"},
    {"{FFFFFFFF-0000-0000-0000-000000000004}", "2015-04-01T13:15:30.400-0700"},
    {"{FFFFFFFF-0000-0000-0000-000000000005}", "2015-04-01T14:20:40.500-0700"}
  };

  private static int VIEW_ACCESS_RIGHTS =
      AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT;
  private static final SimpleDateFormat DATE_PARSER =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private FileConnector connector;

  @Before
  public void setUp() throws Exception {
    this.connector = TestObjectFactory.newFileConnector();
  }

  private DocumentList getDocumentList_Live(Checkpoint checkpoint)
      throws RepositoryException {
    FileSession session = (FileSession) connector.login();
    Traverser traverser = session.getSecurityFolderTraverser();
    traverser.setBatchHint(TestConnection.batchSize);
    return traverser.getDocumentList(checkpoint);
  }

  @Test
  public void startTraversal_Live() throws RepositoryException {
    assertNull(getDocumentList_Live(new Checkpoint()));
  }

  @Test
  public void resumeTraversal_Live()
      throws RepositoryException, ParseException {
    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setTimeAndUuid(Checkpoint.JsonField.LAST_FOLDER_TIME, Jan_1_1970,
        Checkpoint.JsonField.UUID_FOLDER, new FnId(FOLDERS[0][0]));
    DocumentList docList = getDocumentList_Live(checkpoint);
    assertNotNull(docList);

    int count = 0;
    Date prevLastModified = Jan_1_1970;
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      assertIsFolderAcl(doc);
      Checkpoint cp = new Checkpoint(docList.checkpoint());
      Date lastModified = DATE_PARSER.parse(
          cp.getString(Checkpoint.JsonField.LAST_FOLDER_TIME));
      assertTrue(lastModified.getTime() >= prevLastModified.getTime());
      prevLastModified = lastModified;
      count++;
    }
    assertTrue(count > 1);
  }

  private IFolder createFolder(String id, IObjectSet docSet, Date lastModified)
      throws RepositoryException {
    IFolder folder = createNiceMock(IFolder.class);
    expect(folder.get_Id()).andReturn(new FnId(id));
    expect(folder.get_FolderName()).andReturn(id);
    expect(folder.getModifyDate()).andReturn(lastModified);
    expect(folder.get_ContainedDocuments()).andReturn(docSet);
    return folder;
  }

  private IDocument createDocument(String id, AccessPermissionList acl)
      throws RepositoryException {
    IId iid = new FnId(id);
    IDocument doc = createNiceMock(IDocument.class);
    expect(doc.get_Id()).andReturn(iid);
    expect(doc.get_Permissions()).andReturn(acl);
    replay(doc);
    return doc;
  }

  private AccessPermissionList createACL(Iterator<AccessPermission> aceIter) {
    AccessPermissionList acl = createMock(AccessPermissionList.class);
    expect(acl.iterator()).andReturn(aceIter);
    replay(acl);
    return acl;
  }

  private AccessPermission createACE(int accessMask, AccessType acccessType,
      PermissionSource permSrc, SecurityPrincipalType granteeType,
      String grantee) {
    AccessPermission ace = createMock(AccessPermission.class);
    expect(ace.get_AccessMask()).andReturn(accessMask);
    expect(ace.get_AccessType()).andReturn(acccessType);
    expect(ace.get_PermissionSource()).andReturn(permSrc);
    expect(ace.get_GranteeType()).andReturn(granteeType);
    expect(ace.get_GranteeName()).andReturn(grantee);
    replay(ace);
    return ace;
  }

  private AccessPermission[] createACEs() {
    AccessPermission directAllowUser = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_DIRECT,
        SecurityPrincipalType.USER, "Direct Allow User");
    AccessPermission directDenyUser = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.DENY, PermissionSource.SOURCE_DIRECT,
        SecurityPrincipalType.USER, "Direct Deny User");

    AccessPermission parentAllowUser1 = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.USER, "Parent Allow User 1");
    AccessPermission parentAllowUser2 = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.USER, "Parent Allow User 2");
    AccessPermission parentDenyUser = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.DENY, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.USER, "Parent Deny User");

    AccessPermission parentAllowGroup1 = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.GROUP, "Parent Allow Group 1");
    AccessPermission parentAllowGroup2 = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.GROUP, "Parent Allow Group 2");
    AccessPermission parentDenyGroup = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.DENY, PermissionSource.SOURCE_PARENT,
        SecurityPrincipalType.GROUP, "Parent Deny Group");

    AccessPermission templateAllowGroup = createACE(VIEW_ACCESS_RIGHTS,
        AccessType.ALLOW, PermissionSource.SOURCE_TEMPLATE,
        SecurityPrincipalType.GROUP, "Template Allow Group");

    return new AccessPermission[] {
        directAllowUser, directDenyUser,
        parentAllowUser1, parentAllowUser2, parentDenyUser,
        parentAllowGroup1, parentAllowGroup2, parentDenyGroup,
        templateAllowGroup};
  }

  private List<IDocument> getDocuments(int count) throws RepositoryException {
    List<IDocument> docs = new ArrayList<IDocument>();
    for (int i = 1; i <= count; i++) {
      String id = String.format("AAAAAAAA-0000-0000-0000-%012d", i);
      docs.add(createDocument(id, createACL(Iterators.forArray(createACEs()))));
    }
    return docs;
  }

  private SecurityFolderTraverser getObjectUnderTest(IObjectSet folderSet)
      throws RepositoryException {
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(isA(IObjectStore.class)))
        .andReturn(searcher);
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).anyTimes();
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(folderSet).times(1);
    replay(os, searcher, objectFactory);

    return new SecurityFolderTraverser(objectFactory, os, connector);
  }

  @Test
  public void countDocumentsInFolder() throws Exception {
    int docCount = 5;
    List<IDocument> docs = getDocuments(docCount);
    IObjectSet docSet = new FnObjectList(docs);
    IFolder folder = createFolder(FOLDERS[0][0], docSet, new Date());
    IObjectSet folderSet = new FnObjectList(Collections.singletonList(folder));
    replay(folder);

    SecurityFolderTraverser traverser = getObjectUnderTest(folderSet);

    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNotNull(docList);
    int count = 0;
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      testAclDocument(doc);
      count++;
    }
    assertEquals(docCount, count);
  }

  private void assertIsFolderAcl(Document doc) throws RepositoryException {
    assertTrue(doc.getClass().toString(), doc instanceof AclDocument);

    String docid = Value.getSingleValueString(doc, SpiConstants.PROPNAME_DOCID);
    assertTrue(docid + " is not ended with " + AclDocument.SEC_FOLDER_POSTFIX,
        docid.endsWith(AclDocument.SEC_FOLDER_POSTFIX));

    String inheritanceType = Value.getSingleValueString(doc,
        SpiConstants.PROPNAME_ACLINHERITANCETYPE);
    assertEquals(SpiConstants.AclInheritanceType.CHILD_OVERRIDES.toString(),
        inheritanceType);

    String inheritFrom = Value.getSingleValueString(doc,
        SpiConstants.PROPNAME_ACLINHERITFROM);
    assertEquals(null, inheritFrom);

    String inheritFromDocid = Value.getSingleValueString(doc,
        SpiConstants.PROPNAME_ACLINHERITFROM_DOCID);
    assertEquals(null, inheritFromDocid);
  }

  private void testAclDocument(Document doc) throws RepositoryException {
    assertIsFolderAcl(doc);

    testACLs(doc, SpiConstants.PROPNAME_ACLUSERS, 2, "Parent Allow User");
    testACLs(doc, SpiConstants.PROPNAME_ACLDENYUSERS, 1, "Parent Deny User");
    testACLs(doc, SpiConstants.PROPNAME_ACLGROUPS, 2, "Parent Allow Group");
    testACLs(doc, SpiConstants.PROPNAME_ACLDENYGROUPS, 1, "Parent Deny Group");
  }

  private void testACLs(Document doc, String propName, int expectedCount,
      String expectedPrefix) throws RepositoryException {
    Property propAllowUsers = doc.findProperty(propName);
    int count = 0;
    Value val;
    while ((val = propAllowUsers.nextValue()) != null) {
      count++;
      assertTrue(val.getClass().toString(), val instanceof PrincipalValue);
      String name =
          ((PrincipalValue) val).getPrincipal().getName();
      assertTrue(name + " does not start with " + expectedPrefix,
          name.startsWith(expectedPrefix));
    }
    assertEquals(expectedCount, count);
  }

  @Test
  public void testCheckpoint() throws Exception {
    IObjectSet folderSet = getFolderSet(1);

    SecurityFolderTraverser traverser = getObjectUnderTest(folderSet);

    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    int index = 0;
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      Checkpoint checkpoint = new Checkpoint(docList.checkpoint());
      assertEquals(FOLDERS[index++][1],
          checkpoint.getString(JsonField.LAST_FOLDER_TIME));
    }
    assertEquals(folderSet.getSize(), index);
  }

  private IObjectSet getFolderSet(int docsPerFolder) throws Exception {
    List<IFolder> folders = new ArrayList<IFolder>();
    for (int i = 0; i < FOLDERS.length; i++) {
      List<IDocument> docs = getDocuments(docsPerFolder);
      IObjectSet docSet = new FnObjectList(docs);
      IFolder folder =
          createFolder(FOLDERS[i][0], docSet, DATE_PARSER.parse(FOLDERS[i][1]));
      folders.add(folder);
      replay(folder);
    }
    return new FnObjectList(folders);
  }
}
