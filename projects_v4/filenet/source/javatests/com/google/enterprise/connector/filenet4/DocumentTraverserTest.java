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

import static com.google.enterprise.connector.filenet4.ObjectMocks.mockDocument;
import static com.google.enterprise.connector.filenet4.ObjectMocks.newObjectStore;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.EngineCollectionMocks.IndependentObjectSetMock;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.util.Id;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DocumentTraverserTest extends TraverserFactoryFixture {
  private static final SimpleDateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private FileConnector connec;

  @Before
  public void setUp() {
    connec = TestObjectFactory.newFileConnector();
  }

  private Traverser getObjectUnderTest() throws RepositoryException {
    FileSession fs = (FileSession) connec.login();
    return fs.getDocumentTraverser();
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
    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    Traverser traverser = getDocumentTraverser(connec, objectStore,
        new EmptyObjectSet(), new Capture<String>(CaptureType.NONE));
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNull(docList);
    verifyAll();
  }

  @Test
  public void testGetDocumentList_nonEmpty() throws RepositoryException {
    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    String id = "{AAAAAAAA-0000-0000-0000-000000000000}";
    String lastModified = dateFormatter.format(new Date());
    IndependentObject doc = mockDocument(objectStore, id, lastModified, true,
        getPermissions(PermissionSource.SOURCE_DIRECT));
    Traverser traverser = getDocumentTraverser(connec, objectStore,
        new IndependentObjectSetMock(ImmutableList.of(doc)),
        new Capture<String>(CaptureType.NONE));
    DocumentList docList = traverser.getDocumentList(new Checkpoint());

    assertEquals(ImmutableList.of(id), getDocids(docList));
    String checkpoint = docList.checkpoint();
    assertTrue(checkpoint, checkpoint.contains(id));
    assertTrue(checkpoint, checkpoint.contains(lastModified));
    verifyAll();
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

  @Test
  public void testGetDocumentList_initialCheckpoint() throws Exception {
    connec.setDelete_additional_where_clause("and 1=1");

    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    Capture<String> capture = new Capture<>(CaptureType.ALL);
    Traverser traverser = getDocumentTraverser(connec, objectStore,
        new EmptyObjectSet(), capture);
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNull(docList);
    assertTrue(capture.toString(), capture.hasCaptured());
    List<String> queries = capture.getValues();
    assertEquals(queries.toString(), 1, queries.size());

    // Smoke test the executed queries.
    SearchMock search = new SearchMock();
    for (String query : queries) {
      search.executeSql(query);
    }

    // The document query should be unconstrained.
    assertFalse(queries.get(0),
        queries.get(0).contains(" AND ((DateLastModified="));
    verifyAll();
  }

  private String prefix(String whereClause) {
    return whereClause.substring(0, whereClause.indexOf('{'));
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
  @Test
  public void testGetCheckpointClause() throws Exception {
    String expectedId = "{AAAAAAAA-0000-0000-0000-000000000000}";

    // Dates in the query string are in the local time zone, which
    // means we can't hard code them in an expected value.
    Date expectedDate = new Date();
    String expectedDateString =
        FileUtil.getQueryTimeString(dateFormatter.format(expectedDate));

    Checkpoint cp = new Checkpoint();
    cp.setTimeAndUuid(JsonField.LAST_MODIFIED_TIME, expectedDate,
        JsonField.UUID, new Id(expectedId));

    DocumentTraverser traverser =
        new DocumentTraverser(null, null, null, connec);

    String whereClause = " AND ((DateLastModified=" + expectedDateString
        + " AND ('" + expectedId + "'<Id)) OR (DateLastModified>"
        + expectedDateString + "))";

    assertEquals(whereClause,
        traverser.getCheckpointClause(cp, JsonField.LAST_MODIFIED_TIME,
            JsonField.UUID));
  }
}
