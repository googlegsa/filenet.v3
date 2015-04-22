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

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

/**
 * Delegates the traversal to a {@code Traverser}.
 */
public class FileTraversalManager implements TraversalManager,
    TraversalContextAware {
  private static final Logger LOGGER =
      Logger.getLogger(FileTraversalManager.class.getName());

  private final Traverser traverser;

  public FileTraversalManager(Traverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
    traverser.setBatchHint(batchHint);
  }

  @Override
  public DocumentList startTraversal() throws RepositoryException {
    LOGGER.info("Starting traversal...");
    return traverser.getDocumentList(new Checkpoint());
  }

  @Override
  public DocumentList resumeTraversal(String checkPoint)
          throws RepositoryException {
    LOGGER.info("Resuming traversal...");
    return traverser.getDocumentList(new Checkpoint(checkPoint));
  }

  @Override
  public void setTraversalContext(TraversalContext traversalContext) {
    traverser.setTraversalContext(traversalContext);
  }
}
