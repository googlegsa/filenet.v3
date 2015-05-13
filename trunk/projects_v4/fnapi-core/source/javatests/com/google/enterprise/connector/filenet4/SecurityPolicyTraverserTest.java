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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
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
import com.google.enterprise.connector.filenet4.filewrap.ISecurityPolicy;
import com.google.enterprise.connector.filenet4.filewrap.ISecurityTemplate;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.VersionStatusId;
import com.filenet.api.security.AccessPermission;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SecurityPolicyTraverserTest {
  private static final int VIEW_ACCESS_RIGHTS =
      AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT;

  private static final String secPolicyId =
      "{AAAAAAAA-0000-0000-0000-000000000000}";
  private static final String docId =
      "{AAAAAAAA-AAAA-0000-0000-000000000001}";
  private static final String folderId =
      "{FFFFFFFF-FFFF-0000-0000-000000000001}";
  private static final Date Jan_1_1970 = new Date(72000000L);
  private static final SimpleDateFormat DATE_PARSER =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private FileConnector connector;
  private List<AccessPermission> directAces;

  @Before
  public void setUp() throws Exception {
    this.directAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_DIRECT);
    this.connector = TestObjectFactory.newFileConnector();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private IObjectSet getObjectSet(Iterator iterator) {
    IObjectSet objectSet = createMock(IObjectSet.class);
    expect(objectSet.getIterator()).andReturn(iterator);
    return objectSet;
  }

  private ISecurityPolicy getSecurityPolicy(String id, Date lastModified,
      Iterable<ISecurityTemplate> securityTemplates)
          throws RepositoryException {
    IId iid = new FnId(id);
    ISecurityPolicy secPolicy = createNiceMock(ISecurityPolicy.class);
    expect(secPolicy.get_Id()).andReturn(iid).anyTimes();
    expect(secPolicy.get_Name()).andReturn("Mock security policy");
    expect(secPolicy.getModifyDate()).andReturn(lastModified).anyTimes();
    expect(secPolicy.getSecurityTemplates()).andReturn(securityTemplates);
    return secPolicy;
  }

  private ISecurityTemplate getSecurityTemplate(Permissions perms)
      throws RepositoryException {
    ISecurityTemplate secTemplate = createNiceMock(ISecurityTemplate.class);
    expect(secTemplate.get_TemplatePermissions()).andReturn(perms);
    return secTemplate;
  }

  private <E> Iterator<E> getIterator(E... objects) {
    return Iterators.forArray(objects);
  }

  private IDocument getDocument(String id, IFolder folder)
      throws RepositoryException {
    IId iid = new FnId(id);
    IDocument doc = createNiceMock(IDocument.class);
    expect(doc.get_Id()).andReturn(iid).anyTimes();
    expect(doc.get_SecurityFolder()).andReturn(folder);
    return doc;
  }

  @Test
  public void startTraversal_WithoutUpdatedSecPolicy() throws Exception {
    IObjectSet objectSet = getObjectSet(createNiceMock(Iterator.class));
    IObjectStore os = createNiceMock(IObjectStore.class);

    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).anyTimes();
    expect(objectFactory.getSearch(isA(IObjectStore.class))).andReturn(
        searcher);
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(objectSet);

    replay(objectSet, objectFactory, os, searcher);
    Traverser traverser =
        new SecurityPolicyTraverser(objectFactory, os, connector);
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList acls = traverser.getDocumentList(new Checkpoint());
    assertNull(acls);
    verify(objectSet, objectFactory, os);
  }

  private DocumentList getDocumentList_Live(Checkpoint checkpoint)
      throws RepositoryException {
    FileSession session = (FileSession) connector.login();
    Traverser traverser = session.getSecurityPolicyTraverser();
    traverser.setBatchHint(TestConnection.batchSize);
    return traverser.getDocumentList(checkpoint);
  }

  @Test
  public void startTraversal_WithUpdatedSecPolicies_Live()
      throws RepositoryException {
    DocumentList doclist = getDocumentList_Live(new Checkpoint());
    assertNull(doclist);
  }

  @Test
  public void resumeTraversal_WithUpdatedSecPolicies_Live()
      throws RepositoryException {
    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setTimeAndUuid(JsonField.LAST_SECURITY_POLICY_TIME, Jan_1_1970,
        JsonField.UUID_SECURITY_POLICY, new FnId(secPolicyId));
    DocumentList doclist = getDocumentList_Live(checkpoint);
    assertNotNull(doclist);
  }

  @Test
  public void resumeTraversal_Checkpointing_Live()
      throws RepositoryException, ParseException {
    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setTimeAndUuid(JsonField.LAST_SECURITY_POLICY_TIME, Jan_1_1970,
        JsonField.UUID_SECURITY_POLICY, new FnId(secPolicyId));
    DocumentList doclist = getDocumentList_Live(checkpoint);

    // The checkpoint holds the last modified time and UUID of the Security
    // Policy and NOT the document's; as a result, the checkpoint times can be
    // the same for documents inheriting permissions from the same Security
    // Policy.
    int count = 0;
    Date prevLastModified = Jan_1_1970;
    Document doc = doclist.nextDocument();
    while (doc != null) {
      Checkpoint cp = new Checkpoint(doclist.checkpoint());
      Date lastModified = DATE_PARSER.parse(
          cp.getString(Checkpoint.JsonField.LAST_SECURITY_POLICY_TIME));
      assertTrue(lastModified.getTime() >= prevLastModified.getTime());
      prevLastModified = lastModified;
      doc = doclist.nextDocument();
      count++;
    }
    assertTrue(count > 1);
  }

  @Test
  public void resumeTraversal_WithUpdatedSecPolicies_DocsInheritingSecFldr()
      throws Exception {
    resumeTraversal(true);
  }

  @Test
  public void resumeTraversal_WithUpdatedSecPolicies_DocsNotInheritingSecFldr()
      throws Exception {
    resumeTraversal(false);
  }

  private void resumeTraversal(boolean docInheritsSecFolder) throws Exception {
    @SuppressWarnings("unchecked")
    AccessPermissionList permList =
        TestObjectFactory.newPermissionList(directAces);
    IId releasedId = new FnId(VersionStatusId.RELEASED);
    ISecurityTemplate secTemplate =
        getSecurityTemplate(new Permissions(permList));
    expect(secTemplate.get_ApplyStateID()).andReturn(releasedId);
    ISecurityPolicy secPolicy = getSecurityPolicy(secPolicyId, Jan_1_1970,
        Lists.newArrayList(secTemplate));

    IFolder folder = null;
    if (docInheritsSecFolder) {
      folder = createMockFolder(folderId);
      replay(folder);
    }
    Iterator<ISecurityPolicy> secPolicyIter = getIterator(secPolicy);
    IObjectSet secPolicySet = getObjectSet(secPolicyIter);
    IDocument doc = getDocument(docId, folder);
    Iterator<IDocument> docIter = getIterator(doc);
    IObjectSet docSet = getObjectSet(docIter);
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(isA(IObjectStore.class))).andReturn(
        searcher);
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).anyTimes();
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(secPolicySet).times(1);
    expect(searcher.execute(isA(String.class), eq(100), eq(1),
        isA(IBaseObjectFactory.class))).andReturn(docSet).times(1);

    replay(os, secPolicySet, secPolicy, secTemplate, docSet, doc, searcher,
        objectFactory);

    Traverser traverser =
        new SecurityPolicyTraverser(objectFactory, os, connector);
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList doclist = traverser.getDocumentList(new Checkpoint());
    assertNotNull(doclist);

    Document docAcl = doclist.nextDocument();
    assertPropertyEquals(SpiConstants.PROPNAME_DOCID,
        docId + AclDocument.SEC_POLICY_POSTFIX, docAcl);
    if (docInheritsSecFolder) {
      assertPropertyEquals(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID,
          docId + AclDocument.SEC_FOLDER_POSTFIX, docAcl);
    }

    String timeStr = DATE_PARSER.format(Jan_1_1970);
    Checkpoint ck = new Checkpoint(doclist.checkpoint());
    assertEquals(timeStr, ck.getString(JsonField.LAST_SECURITY_POLICY_TIME));
    assertEquals(secPolicyId, ck.getString(JsonField.UUID_SECURITY_POLICY));

    verify(os, secPolicySet, secPolicy, secTemplate, docSet, doc, searcher,
        objectFactory);
  }

  private IFolder createMockFolder(String id) throws RepositoryException {
    IFolder folder = createNiceMock(IFolder.class);
    expect(folder.get_Id()).andReturn(new FnId(id));
    return folder;
  }

  private void assertPropertyEquals(String propName, String expected,
      Document doc) throws RepositoryException {
    Property prop = doc.findProperty(propName);
    assertNotNull(propName + " is null", prop);
    assertEquals(propName + " is not equal " + expected,
        expected, prop.nextValue().toString());
  }
}
