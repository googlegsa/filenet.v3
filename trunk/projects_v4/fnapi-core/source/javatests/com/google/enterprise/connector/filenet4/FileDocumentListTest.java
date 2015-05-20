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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.filejavawrap.FnId;
import com.google.enterprise.connector.filenet4.filejavawrap.FnObjectList;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.mockjavawrap.MockBaseObject;
import com.google.enterprise.connector.filenet4.mockjavawrap.MockObjectStore;
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
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FileDocumentListTest {
  private static final Logger LOGGER =
      Logger.getLogger(FileDocumentListTest.class.getName());

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
  public void testTimeAndGUIDSorting() throws Exception {
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

  private void testSorting(int[] expectedOrder, String[][] entries,
      DatabaseType dbType) throws Exception {
    MockObjectStore os = new MockObjectStore("objectstore", dbType,
        generateObjectMap(entries, false, true));
    DocumentList docList =
        getObjectUnderTest(os, getDocuments(os.getObjects()),
            getCustomDeletion(os.getObjects()),
            getDeletionEvents(os.getObjects()));

    // Test the order
    for (int index : expectedOrder) {
      Document doc = docList.nextDocument();
      Property fid = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertEquals("[" + dbType + "] Incorrect id sorting order",
          "{" + entries[index][0] + "}", fid.nextValue().toString());
    }
  }

  private String[][] getEntries() {
    return new String[][] {
      {"AAAAAAA1-0000-0000-0000-000000000000", "2014-02-01T08:00:00.100"},
      {"AAAAAAA2-0000-0000-0000-000000000000", "2014-02-02T08:00:00.100"},
      {"BBBBBBB1-0000-0000-0000-000000000000", "2014-03-01T08:00:00.100"},
      {"BBBBBBB2-0000-0000-0000-000000000000", "2014-03-02T08:00:00.100"}
    };
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
    testUnreleasedNextDocument(getEntries(),
        generateObjectMap(unreleasedEntries, true, false), expectedPosition);
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
    testUnreleasedNextDocument(getEntries(),
        generateCustomDeletion(unreleasedEntries, false), position);
  }

  private void testUnreleasedNextDocument(String[][] docEntries,
      Map<IId, IBaseObject> unreleasedEntries,
      SkipPosition expectedPosition) throws Exception {
    IId unreleasedGuid = unreleasedEntries.keySet().iterator().next();
    Map<IId, IBaseObject> entries =
        generateObjectMap(docEntries, false, true);
    entries.putAll(unreleasedEntries);
    testUnreleasedNextDocument(entries, unreleasedGuid, expectedPosition);
  }

  private void testUnreleasedNextDocument(Map<IId, IBaseObject> entries,
      IId unreleasedGuid, SkipPosition expectedPosition) throws Exception {
    // Setup object store
    @SuppressWarnings("unchecked")
    MockObjectStore os =
        newObjectStore("MockObjectStore", DatabaseType.MSSQL, entries);

    // Begin testing nextDocument for exception
    DocumentList docList = getObjectUnderTest(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()), getDeletionEvents(os.getObjects()));

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

  private MockObjectStore getCheckpointObjectStore()
      throws ParseException, RepositoryDocumentException {
    String[][] docEntries = {
        { "AAAAAAA1-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "AAAAAAA2-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "AAAAAAA3-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "AAAAAAA4-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    };
    String[][] deEntries = {
        { "DE000001-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "DE000002-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    };
    String[][] cdEntries = {
        { "CD000001-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "CD000002-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
        { "CD000003-0000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    };

    // Setup object store
    @SuppressWarnings("unchecked")
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        generateObjectMap(docEntries, false, true),
        generateObjectMap(deEntries, true, true),
        generateCustomDeletion(cdEntries, true));
    return os;
  }

  @Test
  public void testMockCheckpoint() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()),
        getDeletionEvents(os.getObjects()));
  }

  @Test
  public void testMockCheckpoint_nullCustomDeletes() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(os.getObjects()), null,
        getDeletionEvents(os.getObjects()));
  }

  @Test
  public void testMockCheckpoint_emptyDocuments() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, newEmptyObjectSet(),
        getCustomDeletion(os.getObjects()), getDeletionEvents(os.getObjects()));
  }

  @Test
  public void testMockCheckpoint_emptyCustomDeletes() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        newEmptyObjectSet(), getDeletionEvents(os.getObjects()));
  }

  @Test
  public void testMockCheckpoint_emptyDeletionEvents() throws Exception {
    MockObjectStore os = getCheckpointObjectStore();
    testMockCheckpoint(os, getDocuments(os.getObjects()),
        getCustomDeletion(os.getObjects()), newEmptyObjectSet());
  }

  @Test
  public void testMimeTypesAndSizes() throws Exception {
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 32, true);
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 50, false);
    testMimeTypeAndContentSize("video/3gpp", 1024 * 1024 * 32, false);
  }

  private void testMimeTypeAndContentSize(String mimeType, int size,
      boolean expectNotNull) throws Exception {
    Map<IId, IBaseObject> docs = new HashMap<IId, IBaseObject>();
    MockBaseObject doc1 = (MockBaseObject) createObject(
        "AAAAAAA1-0000-0000-0000-000000000000",
        CHECKPOINT_TIMESTAMP, false, false);
    doc1.setProperty(PropertyNames.MIME_TYPE, mimeType);
    doc1.setProperty(PropertyNames.CONTENT_SIZE, String.valueOf(size));
    docs.put(doc1.get_Id(), doc1);

    @SuppressWarnings("unchecked")
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        docs);
    DocumentList docList = getObjectUnderTest(os, getDocuments(os.getObjects()),
        newEmptyObjectSet(), newEmptyObjectSet());
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
    Map<IId, IBaseObject> docs = new HashMap<IId, IBaseObject>();
    MockBaseObject doc1 = (MockBaseObject) createObject(
        "AAAAAAA1-0000-0000-0000-000000000000",
        CHECKPOINT_TIMESTAMP, false, false);
    doc1.setProperty(PropertyNames.MIME_TYPE, "text/plain");
    doc1.setProperty(PropertyNames.CONTENT_SIZE, String.valueOf(1024));
    docs.put(doc1.get_Id(), doc1);

    TraversalContext traversalContext = new SimpleTraversalContext() {
      @Override public int mimeTypeSupportLevel(String mimeType) {
        return -1;
      }
    };

    @SuppressWarnings("unchecked")
    MockObjectStore os = newObjectStore("MockObjectStore", DatabaseType.MSSQL,
        docs);
    DocumentList docList = new FileDocumentList(getDocuments(os.getObjects()),
        newEmptyObjectSet(), newEmptyObjectSet(), os, connec, traversalContext,
        new Checkpoint(CHECKPOINT));
    Document doc = docList.nextDocument();
    assertNotNull(doc);
    try {
      doc.findProperty(SpiConstants.PROPNAME_CONTENT);
      fail("Expected SkippedDocumentException was not thrown.");
    } catch (SkippedDocumentException ex) {
      // Expected
    }
  }

  private void testMockCheckpoint(IObjectStore os, IObjectSet docSet,
      IObjectSet customDeletionSet, IObjectSet deletionEventSet)
          throws Exception {
    boolean expectAddTested = (docSet != null && docSet.getSize() > 0);
    boolean expectCustomDeletionTested =
        (customDeletionSet != null && customDeletionSet.getSize() > 0);
    boolean expectDeletionEventTested =
        (deletionEventSet != null && deletionEventSet.getSize() > 0);

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
        assertFalse(object instanceof FileDeletionObject);
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

  public void testCheckpointWithoutNextDocument() throws Exception {
    @SuppressWarnings("unchecked") IObjectStore os =
        newObjectStore("MockObjectStore", DatabaseType.MSSQL,
            new HashMap<IId, IBaseObject>());
    DocumentList docList = getObjectUnderTest(os, newEmptyObjectSet(),
        newEmptyObjectSet(), newEmptyObjectSet());

    assertCheckpointEquals(CHECKPOINT, docList.checkpoint());
  }

  /**
   * This simulates a first call to getDocumentList that returns no
   * documents. That's silly, of course, since it means the repository
   * is empty, but it describes the behavior of the checkpoint strings
   * in that case.
   */
  @Test
  public void testEmptyCheckpointWithoutNextDocument() throws Exception {
    @SuppressWarnings("unchecked") IObjectStore os =
        newObjectStore("MockObjectStore", DatabaseType.MSSQL,
            new HashMap<IId, IBaseObject>());
    DocumentList docList = new FileDocumentList(newEmptyObjectSet(),
        newEmptyObjectSet(), newEmptyObjectSet(), os, connec,
        new SimpleTraversalContext(), new Checkpoint());
    Checkpoint cp = new Checkpoint(docList.checkpoint());

    // The checkpoint contains empty string values for the UUIDs and
    // the current time for the dates.
    assertFalse(cp.isEmpty());
    assertEquals("", cp.getString(JsonField.UUID));
    assertEquals("", cp.getString(JsonField.UUID_DELETION_EVENT));
    assertEquals("", cp.getString(JsonField.UUID_CUSTOM_DELETED_DOC));

    Date now = new Date();
    assertDateNearly(now, cp.getString(JsonField.LAST_MODIFIED_TIME));
    assertDateNearly(now, cp.getString(JsonField.LAST_DELETION_EVENT_TIME));
    assertDateNearly(now, cp.getString(JsonField.LAST_CUSTOM_DELETION_TIME));
  }

  private void assertDateNearly(Date expectedDate, String actualDate)
      throws ParseException {
    long expectedMillis = expectedDate.getTime();
    long actualMillis = dateFormatter.parse(actualDate).getTime();
    assertTrue(actualDate, Math.abs(expectedMillis - actualMillis) < 10000L);
  }

  @SafeVarargs
  private final MockObjectStore newObjectStore(String name, DatabaseType dbType,
      Map<IId, IBaseObject>... objectMaps) {
    Map<IId, IBaseObject> data = new HashMap<IId, IBaseObject>();
    for (Map<IId, IBaseObject> objectMap : objectMaps) {
      data.putAll(objectMap);
    }
    return new MockObjectStore(name, dbType, data);
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

  // Helper method to create object
  private IBaseObject createObject(String guid, String timeStr,
      boolean isDeletionEvent, boolean isReleasedVersion)
          throws ParseException, RepositoryDocumentException {
    Date createdTime = dateFormatter.parse(timeStr);
    Id id = new Id(guid);
    return new MockBaseObject(new FnId(id), new FnId(id),
        createdTime, isDeletionEvent, isReleasedVersion);
  }

  /**
   * Generate a map of IBaseObject objects.
   * 
   * @param entries - a 2D array of object ID and created time.
   * @param isDeleteEvent - deletion event flag
   * @param releasedVersion - released version flag
   * @param cal - setting object's creation time.  The creation time will be
   *              incremented by timeIncrement before setting the object.
   * @param timeIncrement - time increment between objects in milliseconds.
   * @return Map<IId, IBaseObject>
   * @throws ParseException 
   */
  private Map<IId, IBaseObject> generateObjectMap(String[][] entries,
      boolean isDeleteEvent, boolean releasedVersion)
          throws ParseException, RepositoryDocumentException {
    Map<IId, IBaseObject> objectMap = new HashMap<IId, IBaseObject>();
    for (String[] line : entries) {
      objectMap.put(new FnId(line[0]), createObject(line[0], line[1],
          isDeleteEvent, releasedVersion));
    }
    return objectMap;
  }

  private Map<IId, IBaseObject> generateObjectMap(String[] entries,
      boolean isDeleteEvent, boolean releasedVersion)
          throws ParseException, RepositoryDocumentException {
    Map<IId, IBaseObject> objectMap = new HashMap<IId, IBaseObject>();
    Calendar cal = Calendar.getInstance();
    for (String entry : entries) {
      objectMap.put(new FnId(entry), createObject(entry,
          Value.calendarToIso8601(cal), isDeleteEvent, releasedVersion));
    }
    return objectMap;
  }

  private IObjectSet newEmptyObjectSet() {
    return new FnObjectList(new ArrayList<IBaseObject>());
  }

  /**
   * Generate a map of FileDeletionObject objects for custom deletion.
   * 
   * @param ids - an array of objects' ID to be generated.
   * @param releasedVersion - released version flag
   * @param cal - setting object's creation time.  The creation time will be
   *              incremented by timeIncrement before setting the object.
   * @param timeIncrement - time increment between objects in milliseconds.
   * @return Map<String, IBaseObject>
   * @throws ParseException 
   */
  private Map<IId, IBaseObject> generateCustomDeletion(
      String[] entries, boolean releasedVersion)
          throws ParseException, RepositoryDocumentException {
    Map<IId, IBaseObject> objectMap = new HashMap<IId, IBaseObject>();
    Calendar cal = Calendar.getInstance();
    for (String entry : entries) {
      IBaseObject object = createObject(entry, Value.calendarToIso8601(cal),
          false, releasedVersion);
      objectMap.put(new FnId(entry), new FileDeletionObject(object));
    }
    return objectMap;
  }

  private Map<IId, IBaseObject> generateCustomDeletion(String[][] entries,
      boolean releasedVersion)
          throws ParseException, RepositoryDocumentException {
    Map<IId, IBaseObject> objectMap = new HashMap<IId, IBaseObject>();
    for (String[] line : entries) {
      IBaseObject object =
          createObject(line[0], line[1], false, releasedVersion);
      objectMap.put(new FnId(line[0]), new FileDeletionObject(object));
    }
    return objectMap;
  }

  private IObjectSet getDocuments(Map<IId, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>(objects.size());
    for (IBaseObject obj : objects.values()) {
      if (!obj.isDeletionEvent() && !(obj instanceof FileDeletionObject)) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }

  private IObjectSet getDeletionEvents(Map<IId, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>();
    for (IBaseObject obj : objects.values()) {
      if (obj.isDeletionEvent()) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }

  private IObjectSet getCustomDeletion(Map<IId, IBaseObject> objects)
      throws RepositoryDocumentException {
    List<IBaseObject> objectList = new ArrayList<IBaseObject>();
    for (IBaseObject obj : objects.values()) {
      if (obj instanceof FileDeletionObject) {
        objectList.add(obj);
      }
    }
    return new FnObjectList(objectList);
  }
}
