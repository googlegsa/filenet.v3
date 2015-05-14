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

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.AccessPermission;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FolderTraverserTest {
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
    Traverser traverser = session.getFolderTraverser();
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
    DocumentList doclist = getDocumentList_Live(checkpoint);
    assertNotNull(doclist);

    int count = 0;
    Date prevLastModified = Jan_1_1970;
    Document doc = doclist.nextDocument();
    while (doc != null) {
      Checkpoint cp = new Checkpoint(doclist.checkpoint());
      Date lastModified = DATE_PARSER.parse(
          cp.getString(Checkpoint.JsonField.LAST_FOLDER_TIME));
      assertTrue(lastModified.getTime() >= prevLastModified.getTime());
      prevLastModified = lastModified;
      doc = doclist.nextDocument();
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

  @SuppressWarnings("unchecked")
  private <E> Iterator<E> getIterator(E...objects) {
    return Iterators.forArray(objects);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private IObjectSet getObjectSet(Iterator iterator, int size) {
    IObjectSet objectSet = createMock(IObjectSet.class);
    expect(objectSet.getIterator()).andReturn(iterator).anyTimes();
    expect(objectSet.getSize()).andReturn(size);
    return objectSet;
  }

  private List<IDocument> getDocuments(int count) throws RepositoryException {
    String prefix = "AAAAAAAA-0000-0000-0000-000000000000";
    List<IDocument> docs = new ArrayList<IDocument>();
    for (int i = 1; i <= count; i++) {
      String s = String.valueOf(i);
      String id = prefix.substring(0, prefix.length() - s.length()) + s;
      docs.add(createDocument(id, createACL(getIterator(createACEs()))));
    }
    return docs;
  }

  @Test
  public void countDocumentsInFolder() throws Exception {
    IObjectStore os = createNiceMock(IObjectStore.class);

    int docCount = 5;
    List<IDocument> docs = getDocuments(docCount);
    IObjectSet docSet = getObjectSet(docs.iterator(), docCount);
    IFolder folder = createFolder(FOLDERS[0][0], docSet, new Date());
    IObjectSet folderSet = getObjectSet(getIterator(folder), 1);
    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(isA(IObjectStore.class)))
        .andReturn(searcher);
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).anyTimes();
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(folderSet).times(1);

    replay(os, docSet, folder, folderSet, searcher, objectFactory);

    FolderTraverser traverser =
        new FolderTraverser(objectFactory, os, connector);
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNotNull(docList);
    int count = 0;
    Document doc = docList.nextDocument();
    while (doc != null) {
      testAclDocument(doc);
      count++;
      doc = docList.nextDocument();
    }
    assertEquals(docCount, count);
  }

  private void testAclDocument(Document doc) throws RepositoryException {
    assertTrue(doc instanceof AclDocument);

    Property propId = doc.findProperty(SpiConstants.PROPNAME_DOCID);
    String id = propId.nextValue().toString();
    assertTrue(id + " is not ended with " + AclDocument.SEC_FOLDER_POSTFIX,
        id.endsWith(AclDocument.SEC_FOLDER_POSTFIX));

    testACLs(doc, SpiConstants.PROPNAME_ACLUSERS, 2, "Parent Allow User");
    testACLs(doc, SpiConstants.PROPNAME_ACLDENYUSERS, 1, "Parent Deny User");
    testACLs(doc, SpiConstants.PROPNAME_ACLGROUPS, 2, "Parent Allow Group");
    testACLs(doc, SpiConstants.PROPNAME_ACLDENYGROUPS, 1, "Parent Deny Group");
  }

  private void testACLs(Document doc, String propName, int expectedCount,
      String expectedPrefix) throws RepositoryException {
    Property propAllowUsers = doc.findProperty(propName);
    int count = 0;
    Value val = propAllowUsers.nextValue();
    while (val != null) {
      count++;
      String name = val.toString();
      assertTrue("Unexpected grantee's name: " + name,
          name.indexOf(expectedPrefix) > 0);
      val = propAllowUsers.nextValue();
    }
    assertEquals(expectedCount, count);
  }

  @Test
  public void testCheckpoint() throws Exception {
    IObjectStore os = createNiceMock(IObjectStore.class);
    IObjectSet folderSet = getFolderSet(1);
    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(isA(IObjectStore.class)))
        .andReturn(searcher);
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).anyTimes();
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(folderSet).times(1);
    replay(os, folderSet, searcher, objectFactory);

    FolderTraverser traverser =
        new FolderTraverser(objectFactory, os, connector);
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    Document doc = docList.nextDocument();
    int index = 0;
    while (doc != null) {
      Checkpoint checkpoint = new Checkpoint(docList.checkpoint());
      assertEquals(FOLDERS[index++][1],
          checkpoint.getString(JsonField.LAST_FOLDER_TIME));
      doc = docList.nextDocument();
    }
    assertEquals(folderSet.getSize(), index);
  }

  private IObjectSet getFolderSet(int docsPerFolder) throws Exception {
    List<IFolder> folders = new ArrayList<IFolder>();
    for (int i = 0; i < FOLDERS.length; i++) {
      List<IDocument> docs = getDocuments(docsPerFolder);
      IObjectSet docSet = getObjectSet(docs.iterator(), docs.size());
      IFolder folder =
          createFolder(FOLDERS[i][0], docSet, DATE_PARSER.parse(FOLDERS[i][1]));
      folders.add(folder);
      replay(folder, docSet);
    }
    return getObjectSet(folders.iterator(), folders.size());
  }
}
