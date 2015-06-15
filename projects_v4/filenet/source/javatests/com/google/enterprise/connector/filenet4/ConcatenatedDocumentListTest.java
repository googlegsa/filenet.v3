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

import static com.google.enterprise.connector.filenet4.SpiMocks.mockDocument;
import static com.google.enterprise.connector.filenet4.SpiMocks.mockDocumentList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.util.EmptyDocumentList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcatenatedDocumentListTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testNoDocumentLists() throws RepositoryException {
    List<DocumentList> emptyList = ImmutableList.of();
    DocumentList cat =
        new ConcatenatedDocumentList(emptyList);
    assertNull(cat.nextDocument());
    assertEquals(null, cat.checkpoint());
  }

  @Test
  public void testNullDocumentList() throws RepositoryException {
    List<DocumentList> listOfNull = Collections.singletonList(null);
    DocumentList cat = new ConcatenatedDocumentList(listOfNull);

    thrown.expect(NullPointerException.class);
    cat.nextDocument();
  }

  @Test
  public void testEmptyDocumentList() throws RepositoryException {
    DocumentList emptyList = new EmptyDocumentList("empty");

    List<DocumentList> listOfEmpty = ImmutableList.of(emptyList);
    DocumentList cat = new ConcatenatedDocumentList(listOfEmpty);

    assertNull(cat.nextDocument());
    assertEquals("empty", cat.checkpoint());
  }

  @Test
  public void testFirstEmptyDocumentList() throws RepositoryException {
    DocumentList emptyList = new EmptyDocumentList("empty");
    Document mockDocument = mockDocument();
    DocumentList mockList =
        mockDocumentList(ImmutableList.of(mockDocument), "mock");

    List<DocumentList> listOfLists = ImmutableList.of(emptyList, mockList);
    DocumentList cat = new ConcatenatedDocumentList(listOfLists);

    assertEquals(mockDocument, cat.nextDocument());
    assertNull(cat.nextDocument());
    assertEquals("mock", cat.checkpoint());
  }

  @Test
  public void testLastEmptyDocumentList() throws RepositoryException {
    DocumentList emptyList = new EmptyDocumentList("empty");
    Document mockDocument = mockDocument();
    DocumentList mockList =
        mockDocumentList(ImmutableList.of(mockDocument), "mock");

    List<DocumentList> listOfLists = ImmutableList.of(mockList, emptyList);
    DocumentList cat = new ConcatenatedDocumentList(listOfLists);

    assertEquals(mockDocument, cat.nextDocument());
    assertNull(cat.nextDocument());
    assertEquals("empty", cat.checkpoint());
  }

  @Test
  public void testMiddleEmptyDocumentList() throws RepositoryException {
    DocumentList emptyList = new EmptyDocumentList("empty");
    Document firstDocument = mockDocument();
    DocumentList firstList =
        mockDocumentList(ImmutableList.of(firstDocument), "first");
    Document lastDocument = mockDocument();
    DocumentList lastList =
        mockDocumentList(ImmutableList.of(lastDocument), "last");

    List<DocumentList> listOfLists =
        ImmutableList.of(firstList, emptyList, lastList);
    DocumentList cat = new ConcatenatedDocumentList(listOfLists);

    assertEquals(firstDocument, cat.nextDocument());
    assertEquals(lastDocument, cat.nextDocument());
    assertNull(cat.nextDocument());
    assertEquals("last", cat.checkpoint());
  }

  @Test
  public void testMultipleDocumentsPerList() throws RepositoryException {
    List<Document> documents = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      documents.add(mockDocument());
    }

    DocumentList firstList = mockDocumentList(documents.subList(0, 3), "first");
    DocumentList middleList = mockDocumentList(documents.subList(3, 6), "mid");
    DocumentList lastList = mockDocumentList(documents.subList(6, 9), "last");

    List<DocumentList> listOfLists =
        ImmutableList.of(firstList, middleList, lastList);
    DocumentList cat = new ConcatenatedDocumentList(listOfLists);

    for (Document expected : documents) {
      assertEquals(expected, cat.nextDocument());
    }
    assertNull(cat.nextDocument());
    assertEquals("last", cat.checkpoint());
  }
}
