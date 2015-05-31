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
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.Lists;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.FnObjectList;
import com.google.enterprise.connector.filenet4.api.IBaseObject;
import com.google.enterprise.connector.filenet4.api.IBaseObjectFactory;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IFolder;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.filenet4.api.ISecurityPolicy;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.SecurityTemplateList;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.VersionStatusId;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.security.SecurityTemplate;
import com.filenet.api.util.Id;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

  private final List<Object> mocksToVerify = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    this.directAces = TestObjectFactory.generatePermissions(1, 1, 1, 1,
        VIEW_ACCESS_RIGHTS, 0, PermissionSource.SOURCE_DIRECT);
    this.connector = TestObjectFactory.newFileConnector();
  }

  @After
  public void verifyMocks() {
    for (Object mock : mocksToVerify) {
      verify(mock);
    }
  }

  private void replayAndVerify(Object... mocks) {
    replay(mocks);
    Collections.addAll(mocksToVerify, mocks);
  }

  private ISecurityPolicy getSecurityPolicy(String id, Date lastModified,
      SecurityTemplateList securityTemplates) throws RepositoryException {
    Id iid = new Id(id);
    ISecurityPolicy secPolicy = createMock(ISecurityPolicy.class);
    expect(secPolicy.get_Id()).andReturn(iid).anyTimes();
    expect(secPolicy.get_Name()).andReturn("Mock security policy").anyTimes();
    expect(secPolicy.getModifyDate()).andReturn(lastModified).anyTimes();
    expect(secPolicy.get_SecurityTemplates()).andReturn(securityTemplates)
        .atLeastOnce();
    replayAndVerify(secPolicy);
    return secPolicy;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class MockSecurityTemplateList
      extends ArrayList implements SecurityTemplateList {
    MockSecurityTemplateList(SecurityTemplate... templates) {
      Collections.addAll(this, templates);
    }
  }

  private SecurityTemplate getSecurityTemplate(AccessPermissionList perms)
      throws RepositoryException {
    SecurityTemplate secTemplate = createMock(SecurityTemplate.class);
    expect(secTemplate.get_TemplatePermissions()).andReturn(perms)
        .atLeastOnce();
    expect(secTemplate.get_ApplyStateID()).andReturn(VersionStatusId.RELEASED)
        .atLeastOnce();
    replayAndVerify(secTemplate);
    return secTemplate;
  }

  private IDocument getDocument(String id, IFolder folder)
      throws RepositoryException {
    Id iid = new Id(id);
    IDocument doc = createMock(IDocument.class);
    expect(doc.get_Id()).andReturn(iid).anyTimes();
    expect(doc.get_SecurityFolder()).andReturn(folder).atLeastOnce();
    replayAndVerify(doc);
    return doc;
  }

  private SecurityPolicyTraverser getObjectUnderTest(IObjectSet secPolicySet,
      IObjectSet docSet) throws RepositoryException {
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = createMock(ISearch.class);
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(isA(IObjectStore.class))).andReturn(
        searcher).atLeastOnce();
    expect(objectFactory.getFactory(isA(String.class))).andReturn(
        createNiceMock(IBaseObjectFactory.class)).atLeastOnce();
    expect(searcher.execute(isA(String.class), eq(100), eq(0),
        isA(IBaseObjectFactory.class))).andReturn(secPolicySet).atLeastOnce();
    if (secPolicySet.getSize() > 0) {
      expect(searcher.execute(isA(String.class), eq(100), eq(1),
              isA(IBaseObjectFactory.class)))
          .andReturn(docSet)
          .times(secPolicySet.getSize(), secPolicySet.getSize() * 2);
    }
    replayAndVerify(objectFactory, os, searcher);

    return new SecurityPolicyTraverser(objectFactory, os, connector);
  }

  @Test
  public void startTraversal_WithoutUpdatedSecPolicy() throws Exception {
    IObjectSet secPolicySet =
        new FnObjectList(Collections.<IBaseObject>emptyList());

    Traverser traverser = getObjectUnderTest(secPolicySet, null);
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList acls = traverser.getDocumentList(new Checkpoint());
    assertNull(acls);
  }

  private DocumentList getDocumentList_Live(Checkpoint checkpoint)
      throws RepositoryException {
    FileSession session = (FileSession) connector.login();
    Traverser traverser = session.getSecurityPolicyTraverser();
    traverser.setBatchHint(TestConnection.batchSize);
    return traverser.getDocumentList(checkpoint);
  }

  @Test
  public void startTraversal_WithoutUpdatedSecPolicies_Live()
      throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    DocumentList doclist = getDocumentList_Live(new Checkpoint());
    assertNull(doclist);
  }

  @Test
  public void resumeTraversal_WithUpdatedSecPolicies_Live()
      throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setTimeAndUuid(JsonField.LAST_SECURITY_POLICY_TIME, Jan_1_1970,
        JsonField.UUID_SECURITY_POLICY, new Id(secPolicyId));
    DocumentList doclist = getDocumentList_Live(checkpoint);
    assertNotNull(doclist);
  }

  @Test
  public void resumeTraversal_Checkpointing_Live()
      throws RepositoryException, ParseException {
    assumeTrue(TestConnection.isLiveConnection());

    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setTimeAndUuid(JsonField.LAST_SECURITY_POLICY_TIME, Jan_1_1970,
        JsonField.UUID_SECURITY_POLICY, new Id(secPolicyId));
    DocumentList doclist = getDocumentList_Live(checkpoint);

    // The checkpoint holds the last modified time and UUID of the Security
    // Policy and NOT the document's; as a result, the checkpoint times can be
    // the same for documents inheriting permissions from the same Security
    // Policy.
    int count = 0;
    Date prevLastModified = Jan_1_1970;
    Document doc;
    while ((doc = doclist.nextDocument()) != null) {
      Checkpoint cp = new Checkpoint(doclist.checkpoint());
      Date lastModified = DATE_PARSER.parse(
          cp.getString(Checkpoint.JsonField.LAST_SECURITY_POLICY_TIME));
      assertTrue(lastModified.getTime() >= prevLastModified.getTime());
      prevLastModified = lastModified;
      count++;
    }
    assertTrue(count > 1);
  }

  @Test
  public void testGetDocumentList_multipleCalls() throws Exception {
    Traverser traverser = getObjectUnderTest(true);

    DocumentList first = traverser.getDocumentList(new Checkpoint());
    DocumentList second = traverser.getDocumentList(new Checkpoint());

    assertNotNull("Got null Document on first pass", first.nextDocument());
    assertNull(first.nextDocument());

    assertNotNull("Got null Document on second pass", second.nextDocument());
    assertNull(second.nextDocument());
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

  private IObjectSet getSecurityPolicySet() throws RepositoryException {
    AccessPermissionList permList =
        TestObjectFactory.newPermissionList(directAces);

    SecurityTemplate secTemplate = getSecurityTemplate(permList);
    SecurityTemplateList secTemplateList =
        new MockSecurityTemplateList(secTemplate);
    ISecurityPolicy secPolicy = getSecurityPolicy(secPolicyId, Jan_1_1970,
        secTemplateList);

    IObjectSet secPolicySet =
        new FnObjectList(Collections.singletonList(secPolicy));
    return secPolicySet;
  }

  /** Gets a traverser with a default stack of mock collaborators. */
  private SecurityPolicyTraverser getObjectUnderTest(
      boolean docInheritsSecFolder) throws RepositoryException {
    IObjectSet secPolicySet = getSecurityPolicySet();
    IFolder folder = null;
    if (docInheritsSecFolder) {
      folder = createMock(IFolder.class);
    }
    IDocument doc = getDocument(docId, folder);
    IObjectSet docSet = new FnObjectList(Collections.singletonList(doc));
    return getObjectUnderTest(secPolicySet, docSet);
  }

  private void resumeTraversal(boolean docInheritsSecFolder) throws Exception {
    Traverser traverser = getObjectUnderTest(docInheritsSecFolder);
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

    assertNull(doclist.nextDocument());
  }

  private void assertPropertyEquals(String propName, String expected,
      Document doc) throws RepositoryException {
    Property prop = doc.findProperty(propName);
    assertNotNull(propName + " is null", prop);
    assertEquals(propName + " is not equal " + expected,
        expected, prop.nextValue().toString());
  }
}
