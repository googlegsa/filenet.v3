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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.adaptor.Acl;
import com.google.enterprise.adaptor.DocId;
import com.google.enterprise.adaptor.testing.RecordingDocIdPusher;
import com.google.enterprise.adaptor.testing.RecordingResponse;
import com.google.enterprise.connector.filenet4.Checkpoint.JsonField;
import com.google.enterprise.connector.filenet4.EngineCollectionMocks.IndependentObjectSetMock;
import com.google.enterprise.connector.filenet4.api.MockObjectFactory;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DatabaseType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.PropertyNames;
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

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  private FileConnector connec;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws RepositoryException {
    connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(MockObjectFactory.class.getName());
    connec.setContent_engine_url(TestConnection.uri);
    connec.setGoogleGlobalNamespace("ns");
    connec.login();
  }

  @Test
  public void testGetDocumentList_empty() throws Exception {
    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    DocumentTraverser traverser = getDocumentTraverser(connec, objectStore,
        new EmptyObjectSet(), new Capture<String>(CaptureType.NONE));
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(new Checkpoint().toString(), pusher);
    assertEquals(pusher.getDocIds().toString(), 0, pusher.getDocIds().size());
    verifyAll();
  }

  @Test
  public void testGetDocumentList_nonEmpty() throws Exception {
    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    String id = "{AAAAAAAA-0000-0000-0000-000000000000}";
    String lastModified = dateFormatter.format(new Date());
    IndependentObject doc = mockDocument(objectStore, id, lastModified, true,
        getPermissions(PermissionSource.SOURCE_DIRECT));
    DocumentTraverser traverser = getDocumentTraverser(connec, objectStore,
        new IndependentObjectSetMock(ImmutableList.of(doc)),
        new Capture<String>(CaptureType.NONE));
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(new Checkpoint().toString(), pusher);

    assertEquals(ImmutableList.of(id), getDocids(pusher.getDocIds()));
    String checkpoint = getCheckpoint(pusher.getDocIds());
    assertTrue(checkpoint, checkpoint.contains(id));
    assertTrue(checkpoint, checkpoint.contains(lastModified));
    verifyAll();
  }

  private ImmutableList<String> getDocids(List<DocId> docList) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (DocId docId : docList) {
      String s = docId.getUniqueId();
      if (s.startsWith("guid/")) {
        builder.add(s.substring(5));
      }
    }
    return builder.build();
  }

  private String getCheckpoint(List<DocId> docList) {
    String checkpoint = null;
    for (DocId docId : docList) {
      String s = docId.getUniqueId();
      if (s.startsWith("pseudo/")) {
        assertNull(checkpoint);
        checkpoint = s.substring(7);
      }
    }
    return checkpoint;
  }

  @Test
  public void testGetDocumentList_initialCheckpoint() throws Exception {
    connec.setDelete_additional_where_clause("and 1=1");

    MockObjectStore objectStore = newObjectStore(DatabaseType.ORACLE);
    Capture<String> capture = new Capture<>(CaptureType.ALL);
    DocumentTraverser traverser = getDocumentTraverser(connec, objectStore,
        new EmptyObjectSet(), capture);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(new Checkpoint().toString(), pusher);
    assertEquals(pusher.getDocIds().toString(), 0, pusher.getDocIds().size());
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

  private String[][] docEntries = {
    { "AAAAAAAA-1000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAAA-2000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAAA-3000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
    { "AAAAAAAA-4000-0000-0000-000000000000", CHECKPOINT_TIMESTAMP },
  };

  @Test
  public void testNullObjectSet_nullDocuments() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    DocumentTraverser traverser = getObjectUnderTest(os, null);
    // We're expecting a NullPointerException, but that leaves
    // unverified mocks, so an AssertionError is also thrown. So we
    // get a MultipleFailureException, but the version of JUnit we
    // compile against doesn't know about that (I think we're getting
    // a newew version of JUnit from Cobertura at runtime, but haven't
    // verified that).
    thrown.expect(Exception.class);
    traverser.getDocIds(CHECKPOINT, new RecordingDocIdPusher());
  }

  @Test
  public void testGetDocIds_noResults() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    DocumentTraverser traverser = getObjectUnderTest(os, new EmptyObjectSet());
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<DocId> docList = pusher.getDocIds();
    assertEquals(ImmutableList.of(), docList);
    verifyAll();
  }

  @Test
  public void testGetDocIds_emptyCheckpoint() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    IndependentObjectSet docSet = getDocuments(os, docEntries, true);

    int counter = 0;
    Date lastModified = null;
    DocumentTraverser traverser = getObjectUnderTest(os, docSet);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(new Checkpoint().toString(), pusher);
    List<DocId> docList = pusher.getDocIds();
    for (String id : getDocids(docList)) {
      assertTrue(os.containsObject(ClassNames.DOCUMENT, new Id(id)));
      RecordingResponse response = new RecordingResponse();
      traverser.getDocContent(id, response);
      lastModified = response.getLastModified();
      counter++;
    }
    assertEquals(docEntries.length, counter);

    assertEquals(CHECKPOINT_TIMESTAMP, dateFormatterNoTz.format(lastModified));
    assertCheckpointEquals(getCheckpoint(docList),
        CHECKPOINT_TIMESTAMP + TZ_OFFSET,
        "{AAAAAAAA-4000-0000-0000-000000000000}");
    verifyAll();
  }

  @Test
  public void testGetDocIds_checkpoint() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    IndependentObjectSet docSet = getDocuments(os, docEntries, true);

    int counter = 0;
    Date lastModified = null;
    DocumentTraverser traverser = getObjectUnderTest(os, docSet);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<DocId> docList = pusher.getDocIds();
    for (String id : getDocids(docList)) {
      assertTrue(os.containsObject(ClassNames.DOCUMENT, new Id(id)));
      RecordingResponse response = new RecordingResponse();
      traverser.getDocContent(id, response);
      lastModified = response.getLastModified();
      counter++;
    }
    assertEquals(docEntries.length, counter);

    assertEquals(CHECKPOINT_TIMESTAMP, dateFormatterNoTz.format(lastModified));
    assertCheckpointEquals(getCheckpoint(docList),
        CHECKPOINT_TIMESTAMP + TZ_OFFSET,
        "{AAAAAAAA-4000-0000-0000-000000000000}");
    verifyAll();
  }

  // TODO(jlacey): This is not interesting against the current mocks,
  // but it could become so with H2-backed proxies, or even made so
  // with better mocks.
  @Test
  public void testGetDocIds_monotonicDates() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    IndependentObjectSet docSet = getDocuments(os, docEntries, true);

    DocumentTraverser traverser = getObjectUnderTest(os, docSet);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<DocId> docList = pusher.getDocIds();
    assertFalse(docList.isEmpty());

    int counter = 0;
    Date prevDate = new Date(0L);
    for (String id : getDocids(docList)) {
      assertTrue(os.containsObject(ClassNames.DOCUMENT, new Id(id)));
      RecordingResponse response = new RecordingResponse();
      traverser.getDocContent(id, response);
      Date thisDate = response.getLastModified();
      assertTrue("Previous date " + prevDate + " is after " + thisDate,
          prevDate.compareTo(thisDate) <= 0);
      prevDate = thisDate;
      counter++;
    }
    assertEquals(docEntries.length, counter);
    verifyAll();
  }

  @Test
  public void testGetDocIds_batchHint() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.ORACLE);
    IndependentObjectSet docSet = getDocuments(os, docEntries, true);

    int counter = 0;
    Date lastModified = null;
    DocumentTraverser traverser = getObjectUnderTest(os, docSet);
    traverser.setBatchHint(1);
    assertTrue(String.valueOf(docEntries.length), docEntries.length > 1);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<DocId> docList = pusher.getDocIds();
    for (String id : getDocids(docList)) {
      assertTrue(os.containsObject(ClassNames.DOCUMENT, new Id(id)));
      RecordingResponse response = new RecordingResponse();
      traverser.getDocContent(id, response);
      lastModified = response.getLastModified();
      counter++;
    }
    assertEquals(1, counter);

    assertEquals(CHECKPOINT_TIMESTAMP, dateFormatterNoTz.format(lastModified));
    assertCheckpointEquals(getCheckpoint(docList),
        CHECKPOINT_TIMESTAMP + TZ_OFFSET,
        "{AAAAAAAA-1000-0000-0000-000000000000}");
    verifyAll();
  }

  @Test
  public void testCheckpointWithoutNextDocument() throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    DocumentTraverser traverser = getObjectUnderTest(os, new EmptyObjectSet());
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<DocId> docList = pusher.getDocIds();

    assertEquals(ImmutableList.of(), docList);
    verifyAll();
  }

  @Test
  public void testMimeTypesAndSizes() throws Exception {
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 32, true);
    testMimeTypeAndContentSize("text/plain", 1024 * 1024 * 1024 * 3L, false);
    testMimeTypeAndContentSize("video/3gpp", 1024 * 1024 * 100, true);
  }

  private void testMimeTypeAndContentSize(String mimeType, double size,
      boolean expectNotNull) throws Exception {
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    IndependentObject doc1 = mockDocument(os,
        "AAAAAAA1", CHECKPOINT_TIMESTAMP, false, size, mimeType);
    IndependentObjectSet docSet =
        new IndependentObjectSetMock(ImmutableList.of(doc1));

    DocumentTraverser traverser = getObjectUnderTest(os, docSet);
    RecordingDocIdPusher pusher = new RecordingDocIdPusher();
    traverser.getDocIds(CHECKPOINT, pusher);
    List<String> docList = getDocids(pusher.getDocIds());

    RecordingResponse response = new RecordingResponse();
    traverser.getDocContent(docList.get(0), response);

    int contentSize =
        ((ByteArrayOutputStream) response.getOutputStream()).size();
    assertEquals("Content size: " + contentSize,
        expectNotNull, contentSize > 0);
    verifyAll();
  }

  @Test
  public void testGetDocContent() throws Exception {
    String id = "{AAAAAAAA-0000-0000-0000-000000000000}";
    DocId docId = new DocId("guid/" + id);
    MockObjectStore os = newObjectStore(DatabaseType.MSSQL);
    IndependentObject doc = mockDocument(os, id, CHECKPOINT_TIMESTAMP, true,
        getPermissions(
            PermissionSource.SOURCE_DIRECT,
            PermissionSource.SOURCE_TEMPLATE,
            PermissionSource.SOURCE_PARENT));

    DocumentTraverser traverser =
        new DocumentTraverser(null, null, os, connec);
    RecordingResponse response = new RecordingResponse();
    traverser.getDocContent(id, response);

    assertEquals(
        ImmutableSet.of(PropertyNames.ID, PropertyNames.DATE_LAST_MODIFIED),
        response.getMetadata().getKeys());

    Acl acl = response.getAcl();
    assertFalse(acl.getPermitUsers().toString(),
        acl.getPermitUsers().isEmpty());
    assertEquals(docId, acl.getInheritFrom());
    assertEquals("TMPL", acl.getInheritFromFragment());

    acl = response.getNamedResources().get("TMPL");
    assertFalse(acl.getPermitUsers().toString(),
        acl.getPermitUsers().isEmpty());
    assertEquals(docId, acl.getInheritFrom());
    assertEquals("FLDR", acl.getInheritFromFragment());

    acl = response.getNamedResources().get("FLDR");
    assertFalse(acl.getPermitUsers().toString(),
        acl.getPermitUsers().isEmpty());
    assertEquals(null, acl.getInheritFrom());
    assertEquals(null, acl.getInheritFromFragment());
  }

  /**
   * Creates an object set of documents.
   *
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the documents should be released versions
   */
  private IndependentObjectSet getDocuments(MockObjectStore os,
      String[][] entries, boolean releasedVersion) {
    return getObjects(os, entries, releasedVersion);
  }

  /**
   * Creates an object set of new objects.
   *
   * @param os the object store to create the objects in
   * @param entries an array of arrays of IDs and timestamps
   * @param releasedVersion if the objects should refer to released versions
   */
  private IndependentObjectSet getObjects(MockObjectStore os,
      String[][] entries, boolean releasedVersion) {
    List<IndependentObject> objectList = new ArrayList<>(entries.length);
    for (String[] entry : entries) {
      objectList.add(mockDocument(os, entry[0], entry[1], releasedVersion));
    }
    return new IndependentObjectSetMock(objectList);
  }

  private DocumentTraverser getObjectUnderTest(MockObjectStore os,
      IndependentObjectSet docSet) throws RepositoryException {
    return getDocumentTraverser(connec, os, docSet,
        new Capture<String>(CaptureType.NONE));
  }

  private void assertCheckpointEquals(String actualCheckpoint,
      String expectedDate, String expectedId) throws JSONException {
    assertFalse("Missing checkpoint: " + actualCheckpoint,
        Strings.isNullOrEmpty(actualCheckpoint));
    assertNotNull("Null expected date", expectedDate);
    assertNotNull("Null expected guid", expectedId);

    JSONObject json = new JSONObject(actualCheckpoint);
    String actualDate =
        (String) json.get(JsonField.LAST_MODIFIED_TIME.toString());
    String actualId = (String) json.get(JsonField.UUID.toString());

    assertEquals(expectedDate, actualDate);
    assertEquals(expectedId, actualId);
  }
}
