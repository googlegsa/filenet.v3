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
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.List;
import java.util.logging.Logger;

/**
 * Splices a list of {@code DocumentList}s together. It assumes that
 * the checkpoints are cumulative, and that a {@code DocumentList}
 * does not modify the checkpoint if {@code nextDocument} was not
 * called on that document list.
 */
class ConcatenatedDocumentList implements DocumentList {
  private static final Logger LOG =
      Logger.getLogger(ConcatenatedDocumentList.class.getName());

  private final List<DocumentList> docLists;

  /** The current document list for {@code nextDocument}. */
  private int index = 0;

  public ConcatenatedDocumentList(List<DocumentList> docLists) {
    this.docLists = docLists;
  }

  @Override
  public Document nextDocument() throws RepositoryException {
    while (index < docLists.size()) {
      Document doc = docLists.get(index).nextDocument();
      if (doc != null) {
        return doc;
      }
      index++;
    }
    return null;
  }

  /**
   * Calls checkpoint on each {@code DocumentList} and returns the
   * last value returned, or {@code null} if the list is empty.
   */
  @Override
  public String checkpoint() throws RepositoryException {
    String checkpoint = null;
    for (DocumentList docList : docLists) {
      checkpoint = docList.checkpoint();
    }
    return checkpoint;
  }
}
