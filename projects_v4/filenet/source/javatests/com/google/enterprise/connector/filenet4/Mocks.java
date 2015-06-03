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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SimpleDocumentList;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;

import java.util.Collections;
import java.util.List;

class Mocks {
  public static Document mockDocument() {
    return new SimpleDocument(Collections.<String, List<Value>>emptyMap());
  }

  public static DocumentList mockDocumentList(
      List<? extends Document> documents, final String checkpoint) {
    return new SimpleDocumentList(documents) {
      @Override public String checkpoint() { return checkpoint; }
    };
  }

  public static Traverser mockTraverser(List<? extends Document> documents,
      String checkpoint) {
    return new MockTraverser(documents, checkpoint);
  }

  private static class MockTraverser implements Traverser {
    private final List<? extends Document> documents;
    private String checkpoint;
    private int batchHint;

    private MockTraverser(List<? extends Document> documents,
        String checkpoint) {
      this.documents = documents;
      this.checkpoint = checkpoint;
    }

    @Override
    public void setBatchHint(int batchHint) {
      this.batchHint = batchHint;
    }

    @Override
    public void setTraversalContext(TraversalContext traversalContext) {
    }

    @Override
    public DocumentList getDocumentList(Checkpoint startCheckpoint) {
      return mockDocumentList(documents.subList(0, batchHint), checkpoint);
    }
  }
}
