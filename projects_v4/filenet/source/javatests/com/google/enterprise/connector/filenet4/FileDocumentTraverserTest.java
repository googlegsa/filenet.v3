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

package com.google.enterprise.connector.filenet4;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.FnObjectList;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IVersionSeries;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.PermissionSource;
import com.filenet.api.util.Id;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileDocumentTraverserTest extends TraverserFactoryFixture {
  private static final SimpleDateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private FileConnector connec;

  @Before
  public void setUp() {
    connec = TestObjectFactory.newFileConnector();
  }

  private Traverser getObjectUnderTest() throws RepositoryException {
    FileSession fs = (FileSession) connec.login();
    return fs.getFileDocumentTraverser();
  }

  @Test
  public void testStartTraversal() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    Traverser traverser = getObjectUnderTest();
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList set = traverser.getDocumentList(new Checkpoint());
    long counter = 0;
    com.google.enterprise.connector.spi.Document doc = null;
    doc = set.nextDocument();
    while (doc != null) {
      if (counter == 113) {
        String checkpoint = set.checkpoint();
        System.out.println(checkpoint);
      }
        doc = set.nextDocument();
        counter++;
    }
    assertEquals(TestConnection.batchSize, counter);
  }

  @Test
  public void testResumeTraversal() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    Traverser traverser = getObjectUnderTest();
    traverser.setBatchHint(TestConnection.batchSize);
    DocumentList set = traverser.getDocumentList(
        new Checkpoint(TestConnection.checkpoint2));
    assertNotNull(set);
    int counter = 0;
    com.google.enterprise.connector.spi.Document doc = null;
    doc = set.nextDocument();
    while (doc != null) {
      doc = set.nextDocument();
      counter++;
    }
    assertEquals(TestConnection.batchSize, counter);

  }

  @Test
  public void testSetBatchHint() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    Traverser traverser = getObjectUnderTest();
    traverser.setBatchHint(10);
    DocumentList set = traverser.getDocumentList(new Checkpoint());
    int counter = 0;
    while (set.nextDocument() != null) {
      counter++;
    }
    assertEquals(10, counter);
  }

  @Test
  public void testGetDocumentList_empty() throws Exception {
    Traverser traverser =
        getFileDocumentTraverser(connec, new EmptyObjectSet());
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNull(docList);
  }

  @Test
  public void testGetDocumentList_nonEmpty() throws RepositoryException {
    String id = "{AAAAAAAA-0000-0000-0000-000000000000}";
    Date lastModified = new Date();
    IDocument doc = getDocument(id, lastModified);

    Traverser traverser = getFileDocumentTraverser(connec,
        new FnObjectList(ImmutableList.of(doc)));
    DocumentList docList = traverser.getDocumentList(new Checkpoint());

    assertEquals(ImmutableList.of(id), getDocids(docList));
    String checkpoint = docList.checkpoint();
    assertTrue(checkpoint, checkpoint.contains(id));
    assertTrue(checkpoint,
        checkpoint.contains(dateFormatter.format(lastModified)));
  }

  protected IDocument getDocument(String id, Date lastModified)
      throws RepositoryException {
    Id iid = new Id(id);

    IVersionSeries version = createNiceMock(IVersionSeries.class);
    expect(version.get_Id()).andReturn(iid).anyTimes();

    IDocument doc = createNiceMock(IDocument.class);
    expect(doc.get_Id()).andReturn(iid).atLeastOnce();
    expect(doc.getModifyDate()).andReturn(lastModified).atLeastOnce();
    expect(doc.get_Permissions())
        .andReturn(getPermissions(PermissionSource.SOURCE_DIRECT))
        .atLeastOnce();
    expect(doc.getVersionSeries()).andReturn(version).atLeastOnce();

    replayAndVerify(version, doc);
    return doc;
  }

  private ImmutableList<String> getDocids(DocumentList docList)
      throws RepositoryException {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      builder.add(
          Value.getSingleValueString(doc, SpiConstants.PROPNAME_DOCID));
    }
    return builder.build();
  }

  /**
   * TODO(jlacey): These tests are of only moderate value. They mostly
   * mirror the implementation of getCheckpointClause itself, with the
   * mild exception of testing the extraction of the values from the
   * Checkpoint. It would be better to expect literal strings here
   * instead of reusing the WHERE_CLAUSE* constants, but the dates in
   * the output are in the local timezone. We could do a fuzzy match
   * on the dates (with regex or Date.getTime differences). These
   * tests might be more valuable if we test the buildQuery* methods
   * instead.
   */
  private void testGetCheckpointClause(JsonField dateField, JsonField uuidField,
      boolean useIdForChangeDetection, String whereClause) throws Exception {
    String expectedId = "{AAAAAAAA-0000-0000-0000-000000000000}";

    // Dates in the query string are in the local time zone, which
    // means we can't hard code them in an expected value.
    Date expectedDate = new Date();
    String expectedDateString =
        FileUtil.getQueryTimeString(dateFormatter.format(expectedDate));

    Checkpoint cp = new Checkpoint();
    cp.setTimeAndUuid(dateField, expectedDate, uuidField, new Id(expectedId));

    connec.setUseIDForChangeDetection(String.valueOf(useIdForChangeDetection));
    FileDocumentTraverser traverser =
        new FileDocumentTraverser(null, null, connec);

    assertEquals(whereClause
        .replace("{0}", expectedDateString)
        .replace("{1}", expectedId)
        .replace("''", "'"),
        traverser.getCheckpointClause(cp, dateField, uuidField,
            useIdForChangeDetection ? whereClause : "",
            useIdForChangeDetection ? "" : whereClause));
  }

  @Test
  public void testGetCheckpointClause() throws Exception {
    testGetCheckpointClause(JsonField.LAST_MODIFIED_TIME,
        JsonField.UUID, true, FileDocumentTraverser.WHERE_CLAUSE);
  }

  @Test
  public void testGetCheckpointClause_onlyDate() throws Exception {
    testGetCheckpointClause(JsonField.LAST_MODIFIED_TIME,
        JsonField.UUID, false, FileDocumentTraverser.WHERE_CLAUSE_ONLY_DATE);
  }

  @Test
  public void testGetCheckpointClauseToDeleteDocs() throws Exception {
    testGetCheckpointClause(JsonField.LAST_CUSTOM_DELETION_TIME,
        JsonField.UUID_CUSTOM_DELETED_DOC, true,
        FileDocumentTraverser.WHERE_CLAUSE_TO_DELETE_DOCS);
  }

  @Test
  public void testGetCheckpointClauseToDeleteDocs_onlyDate() throws Exception {
    testGetCheckpointClause(JsonField.LAST_CUSTOM_DELETION_TIME,
        JsonField.UUID_CUSTOM_DELETED_DOC, false,
        FileDocumentTraverser.WHERE_CLAUSE_TO_DELETE_DOCS_ONLY_DATE);
  }

  @Test
  public void testGetCheckpointClauseToDelete() throws Exception {
    testGetCheckpointClause(JsonField.LAST_DELETION_EVENT_TIME,
        JsonField.UUID_DELETION_EVENT, true,
        FileDocumentTraverser.WHERE_CLAUSE_TO_DELETE);
  }

  @Test
  public void testGetCheckpointClauseToDelete_onlyDate() throws Exception {
    testGetCheckpointClause(JsonField.LAST_DELETION_EVENT_TIME,
        JsonField.UUID_DELETION_EVENT, false,
        FileDocumentTraverser.WHERE_CLAUSE_TO_DELETE_ONLY_DATE);
  }
}
