// Copyright 2014 Google Inc. All Rights Reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Delegates the traversal to a list of {@code Traverser}s.
 */
public class FileTraversalManager implements TraversalManager,
    TraversalContextAware {
  private static final Logger LOGGER =
      Logger.getLogger(FileTraversalManager.class.getName());

  private final List<Traverser> traversers;

  public FileTraversalManager(Traverser... traversers) {
    this.traversers = ImmutableList.copyOf(traversers);
  }

  @Override
  public void setTraversalContext(TraversalContext traversalContext) {
    for (Traverser t : traversers) {
      t.setTraversalContext(traversalContext);
    }
  }

  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
    for (Traverser t : traversers) {
      t.setBatchHint(batchHint);
    }
  }

  @Override
  public DocumentList startTraversal() throws RepositoryException {
    return getDocumentList(new Checkpoint());
  }

  @Override
  public DocumentList resumeTraversal(String checkPoint)
          throws RepositoryException {
    return getDocumentList(new Checkpoint(checkPoint));
  }

  private DocumentList getDocumentList(Checkpoint checkpoint)
      throws RepositoryException {
    List<DocumentList> docLists = new ArrayList<>(traversers.size());
    for (Traverser t : traversers) {
      DocumentList docList = t.getDocumentList(checkpoint);
      if (docList != null) {
        LOGGER.finest("Adding document list to queue: "
            + docList.getClass().getName());
        docLists.add(docList);
      }
    }
    if (docLists.size() > 0) {
      LOGGER.finest("Concatenating " + docLists.size() + " document lists");
      return new ConcatenatedDocumentList(docLists);
    } else {
      LOGGER.finest("No document lists returned from traversers");
      return null;
    }
  }
}
