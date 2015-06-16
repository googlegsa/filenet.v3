// Copyright 2007-2010 Google Inc. All Rights Reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.api.FnObjectList;
import com.google.enterprise.connector.filenet4.api.IBaseObject;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.MockBaseObject;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.util.Id;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FileDocumentListTest {
  private static final Logger LOGGER =
      Logger.getLogger(FileDocumentListTest.class.getName());

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String CHECKPOINT = "{"
      + "\"uuid\":\"{AAAAAAAA-0000-0000-0000-000000000000}\","
      + "\"lastModified\":\"1990-01-01T00:00:00.000\","
      + "\"uuidToDelete\":\"{BBBBBBBB-0000-0000-0000-000000000000}\","
      + "\"lastRemoveDate\":\"2000-01-01T00:00:00.000\","
      + "\"uuidToDeleteDocs\":\"{CCCCCCCC-0000-0000-0000-000000000000}\","
      + "\"lastModifiedDate\":\"2010-01-01T00:00:00.000\""
      + "}";

  private static final DateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private static final String CHECKPOINT_TIMESTAMP =
      "2014-01-01T20:00:00.000";

  /** The expected local time zone offset for checkpoint date strings. */
  private static final String TZ_OFFSET;

  static {
    try {
      TZ_OFFSET = new SimpleDateFormat("Z")
          .format(dateFormatter.parse(CHECKPOINT_TIMESTAMP));
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }

  private enum SkipPosition {FIRST, MIDDLE, LAST};

  private FileConnector connec;

  @Before
  public void setUp() throws RepositoryException {
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
    return fs.getFileDocumentTraverser();
  }

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
    String[][] entries = {
        {"BBBBBBBB-BBBB-0000-0000-000000000000", "2014-11-11T00:55:00.320"},
        {"AAAAAAAA-0000-0000-0000-000000000000", "2014-11-11T22:55:00.320"},
        {"BBBBBBBB-0000-0000-0000-000000000000", "2014-11-11T22:55:00.320"},
        {"EEEEEEEE-0000-0000-0000-000000000000", "2014-11-11T22:55:00.320"},
        {"AAAAAAAA-BBBB-0000-0000-000000000000", "2014-11-11T22:55:00.321"},
        {"DDDDDDDD-0000-0000-0000-000000000000", "2014-12-12T11:11:11.000"},
        {"DDDDDDDD-0000-0000-0000-000000000000", "2014-12-12T11:11:11.123"},
        {"FFFFFFFF-0000-0000-0000-000000000000", "2014-12-12T11:11:11.123"},
        {"CCCCCCCC-0000-0000-0000-000000000000", "2014-12-22T12:12:12.000"},
        {"DDDDDDDD-0000-0000-0000-000000000000", "2014-12-31T23:59:59.999"}
    };
    String[][] docEntries = { entries[0], entries[5], entries[7], entries[8] };
    String[][] cdEntries = { entries[2], entries[3], entries[6] };
    String[][] deEntries = { entries[1], entries[4], entries[9] };
    testSorting(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, entries,
        DatabaseType.MSSQL, getDocuments(docEntries, true),
        getCustomDeletions(cdEntries, true),
        getDeletionEvents(deEntries, true));
  }

  private void testSorting(int[] expectedOrder, String[][] entries,
      DatabaseType dbType) throws Exception {
    IObjectSet documents = getDocuments(entries, true);
    testSorting(expectedOrder, entries, dbType,
        documents, new EmptyObjectSet(), new EmptyObjectSet());
  }

  private void testSorting(int[] expectedOrder, String[][] entries,
      DatabaseType dbType, IObjectSet documents, IObjectSet customDeletions,
      IObjectSet deletionEvents) throws Exception {
    MockObjectStore os = newObjectStore("objectstore", dbType,
        documents, customDeletions, deletionEvents);
    DocumentList docList =
        getObjectUnderTest(os, documents, customDeletions, deletionEvents);

    // Test the order
    for (int index : expectedOrder) {
      Document doc = docList.nextDocument();
      Property fid = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertEquals("[" + dbType + "] Incorrect id sorting order",
          "{" + entries[index][0] + "}", fid.nextValue().toString());
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
          { {"AAAAAAA3-0000-0000-0000-000000000000", timeStamp} };
    testUnreleasedNextDocument(new Id(unreleasedEntries[0][0]),
        new EmptyObjectSet(), getDeletionEvents(unreleasedEntries, false),
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
    testUnreleasedNextDocument(new Id(unreleasedEntries[0][0]),
        getCustomDeletions(unreleasedEntries, false), new EmptyObjectSet(),
        position);
  }

  private void testUnreleasedNextDocument(Id unreleasedGuid,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet,
      SkipPosition expectedPosition) throws Exception {
    String[][] docEntries = new String[][] {
        {"AAAAAAA1-0000-0000-0000-000000000000", "2014-02-01T08:00:00.100"},
        {"AAAAAAA2-0000-0000-0000-000000000000", "2014-02-02T08:00:00.100"},
        {"BBBBBBB1-0000-0000-0000-000000000000", "2014-03-01T08:00:00.100"},
        {"BBBBBBB2-0000-0000-0000-000000000000", "2014-03-02T08:00:00.100"}};

    IObjectSet docSet = getDocuments(docEntries, true);
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        docSet, customDeletionSet, deletionEventSet);

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
    { "DE000001-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "DE000002-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
  };

  private String[][] cdEntries = {
    { "CD000001-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "CD000002-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "CD000003-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
  };

  private MockObjectStore getCheckpointObjectStore()
      throws ParseException, RepositoryDocumentException {
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        getDocuments(docEntries, true), getCustomDeletions(cdEntries, true),
        getDeletionEvents(deEntries, true));
    return os;
  }

  @Test
  public void testNullObjectSet_nullDocuments() throws Exception {
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(getCheckpointObjectStore(),
        null, new EmptyObjectSet(), new EmptyObjectSet());
  }

  @Test
  public void testNullObjectSet_nullCustomDeletes() throws Exception {
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(getCheckpointObjectStore(),
        new EmptyObjectSet(), null, new EmptyObjectSet());
  }

  @Test
  public void testNullObjectSet_nullDeletionEvents() throws Exception {
    thrown.expect(NullPointerException.class);
    DocumentList docList = getObjectUnderTest(getCheckpointObjectStore(),
        new EmptyObjectSet(), new EmptyObjectSet(), null);
  }

  @Test
  public void testMockCheckpoint() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(docEntries, true),
        getCustomDeletions(cdEntries, true),
        getDeletionEvents(deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyDocuments() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, new EmptyObjectSet(),
        getCustomDeletions(cdEntries, true),
        getDeletionEvents(deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyCustomDeletes() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(docEntries, true),
        new EmptyObjectSet(), getDeletionEvents(deEntries, true));
  }

  @Test
  public void testMockCheckpoint_emptyDeletionEvents() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(docEntries, true),
        getCustomDeletions(cdEntries, true), new EmptyObjectSet());
  }

  private void testMockCheckpoint(IObjectStore os, IObjectSet docSet,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet)
          throws Exception {
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
        IBaseObject object = os.getObject(null, id);
        assertTrue(checkpointContains(docList.checkpoint(),
            doc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED),
            JsonField.LAST_MODIFIED_TIME));
        isAddTested = true;
      } else if (ActionType.DELETE.equals(actionType)) {
        IBaseObject object = os.getObject(null, id);
        if (object.isDeletionEvent()) {
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
    IObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL);
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
    IObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL);
    DocumentList docList = new FileDocumentList(new EmptyObjectSet(),
        new EmptyObjectSet(), new EmptyObjectSet(), os, connec,
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

  private void testMimeTypeAndContentSize(String mimeType, int size,
      boolean expectNotNull) throws Exception {
    MockBaseObject doc1 = (MockBaseObject) createObject(
        "AAAAAAA1-0000-0000-0000-000000000000",
        CHECKPOINT_TIMESTAMP, false, false);
    doc1.setProperty(PropertyNames.MIME_TYPE, mimeType);
    doc1.setProperty(PropertyNames.CONTENT_SIZE, String.valueOf(size));
    IObjectSet docSet = new FnObjectList(ImmutableList.of(doc1));

    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        docSet);
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
    MockBaseObject doc1 = (MockBaseObject) createObject(
        "AAAAAAA1-0000-0000-0000-000000000000",
        CHECKPOINT_TIMESTAMP, false, false);
    doc1.setProperty(PropertyNames.MIME_TYPE, "text/plain");
    doc1.setProperty(PropertyNames.CONTENT_SIZE, String.valueOf(1024));
    IObjectSet docSet = new FnObjectList(ImmutableList.of(doc1));

    TraversalContext traversalContext = new SimpleTraversalContext() {
      @Override public int mimeTypeSupportLevel(String mimeType) {
        return -1;
      }
    };

    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        docSet);
    DocumentList docList = new FileDocumentList(docSet,
        new EmptyObjectSet(), new EmptyObjectSet(), os, connec,
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
  private IObjectSet getDocuments(String[][] entries, boolean releasedVersion)
      throws ParseException {
    return getObjects(entries, false, releasedVersion);
  }

  /**
   * Creates an object set of deletion events.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the deleted documents should be released versions
   */
  private IObjectSet getDeletionEvents(String[][] entries,
      boolean releasedVersion) throws ParseException {
    return getObjects(entries, true, releasedVersion);
  }

  /**
   * Creates an object set of custom deletion documents.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the deleted documents should be released versions
   */
  private IObjectSet getCustomDeletions(String[][] entries,
      boolean releasedVersion) throws ParseException {
    return getDocuments(entries, releasedVersion);
  }

  /**
   * Creates an object set.
   *
   * @param entries an array of arrays of IDs and timestamps
   * param isDeletionEvent if the objects are DeletionEvents
   * @param releasedVersion if the objects should refer to released versions
   */
  private IObjectSet getObjects(String[][] entries, boolean isDeletionEvent,
      boolean releasedVersion) throws ParseException {
    List<IBaseObject> objectList = new ArrayList<>(entries.length);
    for (String[] line : entries) {
      objectList.add(
          createObject(line[0], line[1], isDeletionEvent, releasedVersion));
    }
    return new FnObjectList(objectList);
  }

  private IBaseObject createObject(String guid, String timeStr,
      boolean isDeletionEvent, boolean isReleasedVersion)
      throws ParseException {
    Date createdTime = dateFormatter.parse(timeStr);
    Id id = new Id(guid);
    return new MockBaseObject(id, id,
        createdTime, isDeletionEvent, isReleasedVersion);
  }

  private final MockObjectStore newObjectStore(String name, DatabaseType dbType,
      IObjectSet... objectSets) throws RepositoryDocumentException {
    return new MockObjectStore(name, dbType, generateObjectMap(objectSets));
  }

  /**
   * Generate a map of IBaseObject objects.
   *
   * @param objectSets zero or more object sets
   * @return a map from ID to object for all objects in the given sets
   */
  private Map<Id, IBaseObject> generateObjectMap(IObjectSet... objectSets)
          throws RepositoryDocumentException {
    Map<Id, IBaseObject> objectMap = new HashMap<Id, IBaseObject>();
    for (IObjectSet objectSet : objectSets) {
      Iterator<?> iter = objectSet.iterator();
      while (iter.hasNext()) {
        IBaseObject object = (IBaseObject) iter.next();
        objectMap.put(object.get_Id(), object);
      }
    }
    return objectMap;
  }

  private DocumentList getObjectUnderTest(IObjectStore os, IObjectSet docSet,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet)
      throws RepositoryException {
    return new FileDocumentList(docSet, customDeletionSet, deletionEventSet,
        os, connec, getTraversalContext(), new Checkpoint(CHECKPOINT));
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
