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

import static com.google.enterprise.connector.filenet4.CheckpointTest.assertDateNearly;
import static com.google.enterprise.connector.filenet4.CheckpointTest.assertNullField;
import static com.google.enterprise.connector.filenet4.ObjectMocks.mockDeletionEvent;
import static com.google.enterprise.connector.filenet4.ObjectMocks.mockDocument;
import static com.google.enterprise.connector.filenet4.ObjectMocks.newId;
import static com.google.enterprise.connector.filenet4.ObjectMocks.newObjectStore;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.EngineCollectionMocks.IndependentObjectSetMock;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.util.Id;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DocumentTraverserTest extends TraverserFactoryFixture {
  private static final SimpleDateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private static final SimpleDateFormat dateFormatterNoTz =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private static final String CHECKPOINT = "{"
      + "\"uuid\":\"{AAAAAAAA-0000-0000-0000-000000000000}\","
      + "\"lastModified\":\"1990-01-01T00:00:00.000\","
      + "\"uuidToDelete\":\"{BBBBBBBB-0000-0000-0000-000000000000}\","
      + "\"lastRemoveDate\":\"2000-01-01T00:00:00.000\","
      + "\"uuidToDeleteDocs\":\"{CCCCCCCC-0000-0000-0000-000000000000}\","
      + "\"lastModifiedDate\":\"2010-01-01T00:00:00.000\""
      + "}";

  private static final String CHECKPOINT_TIMESTAMP =
      "2014-01-01T20:00:00.000";

  /** The expected local time zone offset for checkpoint date strings. */
  private static final String TZ_OFFSET;

  static {
    try {
      TZ_OFFSET = new SimpleDateFormat("Z")
          .format(dateFormatterNoTz.parse(CHECKPOINT_TIMESTAMP));
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }

  private enum SkipPosition {FIRST, MIDDLE, LAST};

  private FileConnector connec;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws RepositoryException {
    // connec = TestObjectFactory.newFileConnector();
    connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);
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

  // FileDocumentTraverserTest/FileDocumentListTest boundary.

  @Test
  public void testLiveCheckpoint() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());

    Traverser traverser = getObjectUnderTest();
    traverser.setBatchHint(100);
    // Under live test and the test account, the deletion events weren't
    // returned from FileNet.
    boolean tested = false;
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      assertTrue(checkpointContains(docList.checkpoint(),
          doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
          JsonField.LAST_MODIFIED_TIME));
      tested = true;
    }
    assertTrue(tested);
  }

  /*
   * Testing chronological traversal
   */
  @Test
  public void testLiveNextDocument() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());

    Traverser traverser = getObjectUnderTest();
    boolean isTested = false;
    DocumentList docList = traverser.getDocumentList(new Checkpoint());
    assertNotNull("Document list is null", docList);
    Document doc = docList.nextDocument();
    while (doc != null && doc instanceof FileDocument) {
      Property lastModifiedProp =
          doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
      Value lastModifiedValue = lastModifiedProp.nextValue();
      Calendar cal = Value.iso8601ToCalendar(lastModifiedValue.toString());

      Document nextDoc = docList.nextDocument();
      if (nextDoc != null && nextDoc instanceof FileDocument) {
        Property nextDocLastModifiedProp =
            nextDoc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
        Value nextDocLastModifiedValue = nextDocLastModifiedProp.nextValue();
        Calendar nextCal =
            Value.iso8601ToCalendar(nextDocLastModifiedValue.toString());
        assertTrue(cal.compareTo(nextCal) <= 0);
        isTested = true;
      }
      doc = nextDoc;
    }
    assertTrue(isTested);
  }

  @Test
  public void testTimeSorting() throws Exception {
    String[][] entries = {
        {"AAAAAAAA-0000-0000-0000-000000000000", "2014-02-11T08:15:30.129"},
        {"BBBBBBBB-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"BBBBBBAA-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCCC-0000-0000-0000-000000000000", "2014-02-11T08:15:10.329"},
        {"DDDDDDDD-0000-0000-0000-000000000000", "2014-02-11T07:14:30.329"},
        {"EEEEEEEE-0000-0000-0000-000000000000", "2014-02-11T07:15:30.329"},
        {"FFFFFFFF-0000-0000-0000-000000000000", "2014-02-10T08:15:30.329"},
        {"FFFFFFFF-AAAA-0000-0000-000000000000", "2014-01-11T08:15:30.329"},
        {"FFFFFFFF-BBBB-0000-0000-000000000000", "2013-02-11T08:15:30.329"}
    };
    testSorting(new int[] {8, 7, 6, 4, 5, 3, 0, 2, 1}, entries,
        DatabaseType.ORACLE);
  }

  @Test
  public void testGUIDSorting() throws Exception {
    String[][] entries = {
        {"AAAAAA01-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"BBBBBBCC-0000-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-00BB-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-00AA-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-AAAA-0000-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-DDDD-AAAA-0000-000000000000", "2014-02-11T08:15:30.329"},
        {"CCCCCCAA-DDDD-00AA-0000-000000000000", "2014-02-11T08:15:30.329"}
    };
    testSorting(new int[]{0, 3, 4, 2, 6, 5, 1}, entries, DatabaseType.DB2);
    testSorting(new int[]{0, 3, 4, 2, 6, 5, 1}, entries, DatabaseType.ORACLE);
    testSorting(new int[]{0, 1, 3, 4, 2, 6, 5}, entries, DatabaseType.MSSQL);
  }

  @Test
  public void testMergeSorting() throws Exception {
    // IDs are sorted in groups right-to-left, and (in SQL Server) within
    // groups left-to-right. For DeletionEvents, the VersionSeriesId and Id
    // properties must be different (for fetches from the object store), but
    // they need to sort together. Object sets are sorted by ID, but we're
    // using the VersionSeriesId (via PROPNAME_DOCID) to verify the sort
    // order.
    String[][] entries = {
        {"BBBBBBBB-BBBB",                  "2014-11-11T00:55:00.320"},
        {"AAAAAAAA-0000", "AAAAAAAA-00AA", "2014-11-11T22:55:00.320"},
        {"BBBBBBBB-AAAA",                  "2014-11-11T22:55:00.320"},
        {"EEEEEEEE-AAAA",                  "2014-11-11T22:55:00.320"},
        {"AAAAAAAA-BBBB", "AAAAAAAA-00BB", "2014-11-11T22:55:00.321"},
        {"DDDDDDDD-0000",                  "2014-12-12T11:11:11.123"},
        {"DDDDDDDD-0000",                  "2014-12-12T11:11:11.123"},
        {"FFFFFFFF-0000",                  "2014-12-12T11:11:11.123"},
        {"CCCCCCCC-0000",                  "2014-12-22T12:12:12.000"},
        {"DDDDDDDD-0000", "DDDDDDDD-00AA", "2014-12-31T23:59:59.999"}
    };
    String[][] docEntries = { entries[0], entries[5], entries[7], entries[8] };
    String[][] cdEntries = { entries[2], entries[3], entries[6] };
    String[][] deEntries = { entries[1], entries[4], entries[9] };
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    testSorting(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, entries, os,
        getDocuments(os, docEntries, true),
        getCustomDeletions(os, cdEntries, true),
        getDeletionEvents(os, deEntries, true));
  }

  private void testSorting(int[] expectedOrder, String[][] entries,
      DatabaseType dbType) throws Exception {
    MockObjectStore os = newObjectStore(dbType);
    IndependentObjectSet documents = getDocuments(os, entries, true);
    testSorting(expectedOrder, entries, os,
        documents, new EmptyObjectSet(), new EmptyObjectSet());
  }

  private void testSorting(int[] expectedOrder, String[][] entries,
      MockObjectStore os, IndependentObjectSet documents,
      IndependentObjectSet customDeletions, IndependentObjectSet deletionEvents)
      throws Exception {
    DocumentList docList =
        getObjectUnderTest(os, documents, customDeletions, deletionEvents);

    // Test the order
    for (int index : expectedOrder) {
      Document doc = docList.nextDocument();
      Property fid = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertEquals("[" + os.get_DatabaseType() + "] Incorrect id sorting order",
          newId(entries[index][0]).toString(), fid.nextValue().toString());
    }
  }

  @Test
  public void testUnreleasedNextDeletionEvent_firstEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-01-01T08:00:00.100",
        SkipPosition.FIRST);
  }

  @Test
  public void testUnreleasedNextDeletionEvent_middleEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-02-03T08:00:00.100",
        SkipPosition.MIDDLE);
  }

  @Test
  public void testUnreleasedNextDeletionEvent_lastEntry() throws Exception {
    testUnreleasedNextDeletionEvent("2014-03-03T08:00:00.100",
        SkipPosition.LAST);
  }

  private void testUnreleasedNextDeletionEvent(String timeStamp,
      SkipPosition expectedPosition) throws Exception {
    String[][] unreleasedEntries =
        { {"AAAAAAA3-AAAA", "AAAAAAA3-0000", timeStamp} };
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    testUnreleasedNextDocument(newId(unreleasedEntries[0][1]), os,
        new EmptyObjectSet(), getDeletionEvents(os, unreleasedEntries, false),
        expectedPosition);
  }

  @Test
  public void testUnreleasedNextCustomDeletion_firstEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-01-01T08:00:00.100",
        SkipPosition.FIRST);
  }

  @Test
  public void testUnreleasedNextCustomDeletion_middleEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-02-03T08:00:00.100",
        SkipPosition.MIDDLE);
  }

  @Test
  public void testUnreleasedNextCustomDeletion_lastEntry() throws Exception {
    testUnreleasedNextCustomDeletion("2014-03-03T08:00:00.100",
        SkipPosition.LAST);
  }

  private void testUnreleasedNextCustomDeletion(String timeStamp,
      SkipPosition position) throws Exception {
    String[][] unreleasedEntries =
        { {"AAAAAAA3-0000-0000-0000-000000000000", timeStamp} };
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    testUnreleasedNextDocument(newId(unreleasedEntries[0][0]), os,
        getCustomDeletions(os, unreleasedEntries, false), new EmptyObjectSet(),
        position);
  }

  private void testUnreleasedNextDocument(Id unreleasedGuid, MockObjectStore os,
      IndependentObjectSet customDeletionSet,
      IndependentObjectSet deletionEventSet, SkipPosition expectedPosition)
      throws Exception {
    String[][] docEntries = new String[][] {
        {"AAAAAAA1-0000-0000-0000-000000000000", "2014-02-01T08:00:00.100"},
        {"AAAAAAA2-0000-0000-0000-000000000000", "2014-02-02T08:00:00.100"},
        {"BBBBBBB1-0000-0000-0000-000000000000", "2014-03-01T08:00:00.100"},
        {"BBBBBBB2-0000-0000-0000-000000000000", "2014-03-02T08:00:00.100"}};

    IndependentObjectSet docSet = getDocuments(os, docEntries, true);

    // Begin testing nextDocument for exception
    DocumentList docList =
        getObjectUnderTest(os, docSet, customDeletionSet, deletionEventSet);

    SkipPosition actualPosition = SkipPosition.FIRST;
    try {
      for (Document doc = docList.nextDocument(); doc != null;
          doc = docList.nextDocument()) {
        actualPosition = SkipPosition.MIDDLE;
      }
      fail("Expect SkippedDocumentException");
    } catch (SkippedDocumentException expected) {
      if (!expected.getMessage().contains(unreleasedGuid.toString())) {
        throw expected;
      }
      if (docList.nextDocument() == null) {
        actualPosition = SkipPosition.LAST;
      }
    }
    assertEquals(expectedPosition, actualPosition);
  }

  private String[][] docEntries = {
    { "AAAAAAA1-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAA2-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAA3-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAA4-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
  };

  private String[][] deEntries = {
    { "DEEEEEE1", "DE000001", CHECKPOINT_TIMESTAMP },
    { "DEEEEEE2", "DE000002", CHECKPOINT_TIMESTAMP },
  };

  private String[][] cdEntries = {
    { "CD000001-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "CD000002-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "CD000003-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
  };

  @Test
  public void testNullObjectSet_nullDocuments() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(os,
        null, new EmptyObjectSet(), new EmptyObjectSet());
  }

  @Test
  public void testNullObjectSet_nullCustomDeletes() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(os,
        new EmptyObjectSet(), null, new EmptyObjectSet());
  }

  @Test
  public void testNullObjectSet_nullDeletionEvents() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(os,
        new EmptyObjectSet(), new EmptyObjectSet(), null);
  }

  @Test
  public void testMockCheckpoint() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    testMockCheckpoint(os, getDocuments(os, docEntries, true),
        getCustomDeletions(os, cdEntries, true),
        getDeletionEvents(os, deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyDocuments() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    testMockCheckpoint(os, new EmptyObjectSet(),
        getCustomDeletions(os, cdEntries, true),
        getDeletionEvents(os, deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyCustomDeletes() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    testMockCheckpoint(os, getDocuments(os, docEntries, true),
        new EmptyObjectSet(), getDeletionEvents(os, deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyDeletionEvents() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    testMockCheckpoint(os, getDocuments(os, docEntries, true),
        getCustomDeletions(os, cdEntries, true), new EmptyObjectSet());
  }

  private void testMockCheckpoint(MockObjectStore os,
      IndependentObjectSet docSet, IndependentObjectSet customDeletionSet,
      IndependentObjectSet deletionEventSet) throws Exception {
    boolean expectAddTested = !docSet.isEmpty();
    boolean expectCustomDeletionTested = !customDeletionSet.isEmpty();
    boolean expectDeletionEventTested = !deletionEventSet.isEmpty();

    boolean isAddTested = false;
    boolean isDeletionEventTested = false;
    boolean isCustomDeletionTested = false;

    DocumentList docList =
        getObjectUnderTest(os, docSet, customDeletionSet, deletionEventSet);
    Document doc = null;
    while ((doc = docList.nextDocument()) != null) {
      Property actionProp = doc.findProperty(SpiConstants.PROPNAME_ACTION);
      ActionType actionType = SpiConstants.ActionType.findActionType(
          actionProp.nextValue().toString());

      String id =
          doc.findProperty(SpiConstants.PROPNAME_DOCID).nextValue().toString();
      if (ActionType.ADD.equals(actionType)) {
        assertTrue(os.containsObject(ClassNames.DOCUMENT, new Id(id)));
        assertTrue(checkpointContains(docList.checkpoint(),
            doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
            JsonField.LAST_MODIFIED_TIME));
        isAddTested = true;
      } else if (ActionType.DELETE.equals(actionType)) {
        if (!os.containsObject(ClassNames.DOCUMENT, new Id(id))) {
          // Proxy for a DeletionEvent.
          assertTrue(checkpointContains(docList.checkpoint(),
              doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
              JsonField.LAST_DELETION_EVENT_TIME));
          isDeletionEventTested = true;
        } else {
          assertTrue(checkpointContains(docList.checkpoint(),
              doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
              JsonField.LAST_CUSTOM_DELETION_TIME));
          isCustomDeletionTested = true;
        }
      }
    }
    assertEquals(expectAddTested, isAddTested);
    assertEquals(expectCustomDeletionTested, isCustomDeletionTested);
    assertEquals(expectDeletionEventTested, isDeletionEventTested);

    String expectedCheckpoint = "{"
        + (expectAddTested
            ? "\"uuid\":\"{AAAAAAA4-0000-0000-0000-000000000000}\","
            + "\"lastModified\":\"" + CHECKPOINT_TIMESTAMP + TZ_OFFSET + "\","
            : "\"uuid\":\"{AAAAAAAA-0000-0000-0000-000000000000}\","
            + "\"lastModified\":\"1990-01-01T00:00:00.000\",")
        + (expectDeletionEventTested
            ? "\"uuidToDelete\":\"{DE000002-0000-0000-0000-000000000000}\","
            + "\"lastRemoveDate\":\"" + CHECKPOINT_TIMESTAMP + TZ_OFFSET + "\","
            : "\"uuidToDelete\":\"{BBBBBBBB-0000-0000-0000-000000000000}\","
            + "\"lastRemoveDate\":\"2000-01-01T00:00:00.000\",")
        + (expectCustomDeletionTested
            ? "\"uuidToDeleteDocs\":\"{CD000003-0000-0000-0000-000000000000}\","
            + "\"lastModifiedDate\":\"" + CHECKPOINT_TIMESTAMP + TZ_OFFSET
            + "\""
            : "\"uuidToDeleteDocs\":\"{CCCCCCCC-0000-0000-0000-000000000000}\","
            + "\"lastModifiedDate\":\"2010-01-01T00:00:00.000\"")
        + "}";
    assertCheckpointEquals(expectedCheckpoint, docList.checkpoint());
  }

  @Test
  public void testCheckpointWithoutNextDocument() throws Exception {
    IObjectStore os = newObjectStore(DatabaseType.MSSQL);
    DocumentList docList = getObjectUnderTest(os, new EmptyObjectSet(),
        new EmptyObjectSet(), new EmptyObjectSet());

    assertCheckpointEquals(CHECKPOINT, docList.checkpoint());
  }

  /**
   * This simulates a first call to getDocumentList that times out, so
   * nextDocument is never called but checkpoint is.
   */
  @Test
  public void testEmptyCheckpointWithoutNextDocument() throws Exception {
    IObjectStore os = newObjectStore(DatabaseType.MSSQL);
    DocumentList docList = new FileDocumentList(new EmptyObjectSet(),
        new EmptyObjectSet(), new EmptyObjectSet(), null, os, connec,
        new SimpleTraversalContext(), new Checkpoint());
    Checkpoint cp = new Checkpoint(docList.checkpoint());

    // The checkpoint contains only the dates for the delete queries.
    Date now = new Date();
    assertNullField(cp, JsonField.LAST_MODIFIED_TIME);
    assertDateNearly(now, cp.getString(JsonField.LAST_DELETION_EVENT_TIME));
    assertDateNearly(now, cp.getString(JsonField.LAST_CUSTOM_DELETION_TIME));

    assertNullField(cp, JsonField.UUID);
    assertEquals("", cp.getString(JsonField.UUID_DELETION_EVENT));
    assertEquals("", cp.getString(JsonField.UUID_CUSTOM_DELETED_DOC));
  }

  @Test
  public void testMimeTypesAndSizes() throws Exception {
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 32, true);
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 50, false);
    testMimeTypeAndContentSize("video/3gpp", 1024 * 1024 * 32, false);
  }

  private void testMimeTypeAndContentSize(String mimeType, double size,
      boolean expectNotNull) throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    IndependentObject doc1 = mockDocument(os,
        "AAAAAAA1", CHECKPOINT_TIMESTAMP, false, size, mimeType);
    IndependentObjectSet docSet =
        new IndependentObjectSetMock(ImmutableList.of(doc1));

    DocumentList docList = getObjectUnderTest(os, docSet,
        new EmptyObjectSet(), new EmptyObjectSet());
    Document doc = docList.nextDocument();
    assertNotNull(doc);
    if (expectNotNull) {
      assertNotNull(SpiConstants.PROPNAME_CONTENT + " is null",
          doc.findProperty(SpiConstants.PROPNAME_CONTENT));
    } else {
      assertNull(doc.findProperty(SpiConstants.PROPNAME_CONTENT));
    }
  }

  @Test
  public void testExcludedMimeType() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    IndependentObject doc1 = mockDocument(os,
        "AAAAAAA1", CHECKPOINT_TIMESTAMP, false, 1024.0, "text/plain");
    IndependentObjectSet docSet =
        new IndependentObjectSetMock(ImmutableList.of(doc1));

    TraversalContext traversalContext = new SimpleTraversalContext() {
      @Override public int mimeTypeSupportLevel(String mimeType) {
        return -1;
      }
    };

    DocumentList docList = new FileDocumentList(docSet,
        new EmptyObjectSet(), new EmptyObjectSet(), null, os, connec,
        traversalContext, new Checkpoint(CHECKPOINT));
    Document doc = docList.nextDocument();
    assertNotNull(doc);
    try {
      doc.findProperty(SpiConstants.PROPNAME_CONTENT);
      fail("Expected SkippedDocumentException was not thrown.");
    } catch (SkippedDocumentException ex) {
      // Expected
    }
  }

  /**
   * Creates an object set of documents.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the documents should be released versions
   */
  private IndependentObjectSet getDocuments(MockObjectStore os,
      String[][] entries, boolean releasedVersion) {
    return getObjects(os, entries, false, releasedVersion);
  }

  /**
   * Creates an object set of deletion events.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the deleted documents should be released versions
   */
  private IndependentObjectSet getDeletionEvents(MockObjectStore os,
      String[][] entries, boolean releasedVersion) {
    return getObjects(os, entries, true, releasedVersion);
  }

  /**
   * Creates an object set of custom deletion documents.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the deleted documents should be released versions
   */
  private IndependentObjectSet getCustomDeletions(MockObjectStore os,
      String[][] entries, boolean releasedVersion) {
    return getDocuments(os, entries, releasedVersion);
  }

  /**
   * Creates an object set of new objects.
   *
   * @param os the object store to create the objects in
   * @param entries an array of arrays of IDs and timestamps
   * @param isDeletionEvent if the objects are DeletionEvents
   * @param releasedVersion if the objects should refer to released versions
   */
  private IndependentObjectSet getObjects(MockObjectStore os,
      String[][] entries, boolean isDeletionEvent, boolean releasedVersion) {
    List<IndependentObject> objectList = new ArrayList<>(entries.length);
    for (String[] entry : entries) {
      objectList.add(
          (isDeletionEvent)
          ? mockDeletionEvent(os, entry[0], entry[1], entry[2], releasedVersion)
          : mockDocument(os, entry[0], entry[1], releasedVersion));
    }
    return new IndependentObjectSetMock(objectList);
  }

  private DocumentList getObjectUnderTest(IObjectStore os,
      IndependentObjectSet docSet, IndependentObjectSet customDeletionSet,
      IndependentObjectSet deletionEventSet) throws RepositoryException {
    return new FileDocumentList(docSet, customDeletionSet, deletionEventSet,
        null, os, connec, getTraversalContext(), new Checkpoint(CHECKPOINT));
  }

  private TraversalContext getTraversalContext() {
    Set<String> supportedMimeTypes = new HashSet<String>();
    supportedMimeTypes.add("text/plain");
    supportedMimeTypes.add("text/html");

    SimpleTraversalContext context = new SimpleTraversalContext();
    context.setMaxDocumentSize(1024 * 1024 * 32);
    context.setMimeTypeSet(supportedMimeTypes);
    return context;
  }

  private boolean checkpointContains(String checkpoint, Property lastModified,
      JsonField jsonField) throws JSONException, RepositoryException {
    if (Strings.isNullOrEmpty(checkpoint) || lastModified == null
        || jsonField == null) {
      return false;
    }
    JSONObject json = new JSONObject(checkpoint);
    String checkpointTime = (String) json.get(jsonField.toString());
    String docLastModifiedTime = lastModified.nextValue().toString();

    return checkpointTime.equals(docLastModifiedTime);
  }

  private void assertCheckpointEquals(String expected, String actual)
      throws JSONException {
    JSONObject expectedJson = new JSONObject(expected);
    JSONObject actualJson = new JSONObject(actual);

    ImmutableSet<String> expectedKeys =
        ImmutableSet.copyOf(JSONObject.getNames(expectedJson));
    ImmutableSet<String> actualKeys =
        ImmutableSet.copyOf(JSONObject.getNames(actualJson));

    assertEquals("Checkpoint keys", expectedKeys, actualKeys);
    for (String key : expectedKeys) {
      assertEquals("Checkpoint key " + key,
          expectedJson.getString(key), actualJson.getString(key));
    }
  }
}
