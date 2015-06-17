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
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.EngineSetMocks.DocumentSetMock;
import com.google.enterprise.connector.filenet4.EngineSetMocks.SecurityPolicySetMock;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.SecurityTemplateList;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.VersionStatusId;
import com.filenet.api.core.Folder;
import com.filenet.api.security.SecurityPolicy;
import com.filenet.api.security.SecurityTemplate;
import com.filenet.api.util.Id;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class SecurityPolicyTraverserTest extends TraverserFactoryFixture {
  private static final String secPolicyId =
      "{AAAAAAAA-0000-0000-0000-000000000000}";
  private static final String docId =
      "{AAAAAAAA-AAAA-0000-0000-000000000001}";
  private static final Date Jan_1_1970 = new Date(72000000L);
  private static final SimpleDateFormat DATE_PARSER =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private FileConnector connector;

  @Before
  public void setUp() {
    this.connector = TestObjectFactory.newFileConnector();
  }

  private SecurityPolicy getSecurityPolicy(String id, Date lastModified,
      SecurityTemplateList securityTemplates) {
    Id iid = new Id(id);
    SecurityPolicy secPolicy = createMock(SecurityPolicy.class);
    expect(secPolicy.get_Id()).andReturn(iid).anyTimes();
    expect(secPolicy.get_Name()).andReturn("Mock security policy").anyTimes();
    expect(secPolicy.get_DateLastModified()).andReturn(lastModified).anyTimes();
    expect(secPolicy.get_SecurityTemplates()).andReturn(securityTemplates)
        .atLeastOnce();
    replayAndSave(secPolicy);
    return secPolicy;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class MockSecurityTemplateList
      extends ArrayList implements SecurityTemplateList {
    MockSecurityTemplateList(SecurityTemplate... templates) {
      Collections.addAll(this, templates);
    }
  }

  private SecurityTemplate getSecurityTemplate(AccessPermissionList perms) {
    SecurityTemplate secTemplate = createMock(SecurityTemplate.class);
    expect(secTemplate.get_TemplatePermissions()).andReturn(perms)
        .atLeastOnce();
    expect(secTemplate.get_ApplyStateID()).andReturn(VersionStatusId.RELEASED)
        .atLeastOnce();
    replayAndSave(secTemplate);
    return secTemplate;
  }

  // Document collides with the SPI class of the same name.
  private com.filenet.api.core.Document getDocument(String id, Folder folder)
      throws RepositoryException {
    Id iid = new Id(id);
    com.filenet.api.core.Document doc =
        createMock(com.filenet.api.core.Document.class);
    expect(doc.get_Id()).andReturn(iid).anyTimes();
    expect(doc.get_SecurityFolder()).andReturn(folder).atLeastOnce();
    replayAndSave(doc);
    return doc;
  }

  @Test
  public void startTraversal_WithoutUpdatedSecPolicy() throws Exception {
    SecurityPolicySetMock secPolicySet = new SecurityPolicySetMock();
    Traverser traverser =
        getSecurityPolicyTraverser(connector, secPolicySet, null);
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList acls = traverser.getDocumentList(new Checkpoint());
    assertNull(acls);
    verifyAll();
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
    verifyAll();
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

  private SecurityPolicySetMock getSecurityPolicySet() {
    AccessPermissionList permList =
        getPermissions(PermissionSource.SOURCE_DIRECT);
    SecurityTemplate secTemplate = getSecurityTemplate(permList);
    SecurityTemplateList secTemplateList =
        new MockSecurityTemplateList(secTemplate);
    SecurityPolicy secPolicy = getSecurityPolicy(secPolicyId, Jan_1_1970,
        secTemplateList);
    return new SecurityPolicySetMock(Collections.singletonList(secPolicy));
  }

  /** Gets a traverser with a default stack of mock collaborators. */
  private SecurityPolicyTraverser getObjectUnderTest(
      boolean docInheritsSecFolder) throws RepositoryException {
    SecurityPolicySetMock secPolicySet = getSecurityPolicySet();
    Folder folder = null;
    if (docInheritsSecFolder) {
      folder = createMock(Folder.class);
    }
    com.filenet.api.core.Document doc = getDocument(docId, folder);
    DocumentSet docSet = new DocumentSetMock(Collections.singletonList(doc));
    return getSecurityPolicyTraverser(connector, secPolicySet, docSet);
  }

  private void resumeTraversal(boolean docInheritsSecFolder) throws Exception {
    Traverser traverser = getObjectUnderTest(docInheritsSecFolder);
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList doclist = traverser.getDocumentList(new Checkpoint());
    assertNotNull(doclist);

    Document docAcl = doclist.nextDocument();
    assertTrue(docAcl.getClass().toString(), docAcl instanceof AclDocument);
    assertEquals(docId + AclDocument.SEC_POLICY_POSTFIX,
        Value.getSingleValueString(docAcl, SpiConstants.PROPNAME_DOCID));
    if (docInheritsSecFolder) {
      assertEquals(docId + AclDocument.SEC_FOLDER_POSTFIX,
          Value.getSingleValueString(docAcl,
              SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    }

    String timeStr = DATE_PARSER.format(Jan_1_1970);
    Checkpoint ck = new Checkpoint(doclist.checkpoint());
    assertEquals(timeStr, ck.getString(JsonField.LAST_SECURITY_POLICY_TIME));
    assertEquals(secPolicyId, ck.getString(JsonField.UUID_SECURITY_POLICY));

    assertNull(doclist.nextDocument());
    verifyAll();
  }
}
