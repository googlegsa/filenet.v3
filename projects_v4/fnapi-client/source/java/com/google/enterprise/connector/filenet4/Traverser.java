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

/**
 * Similar to {@code TraversalManager} but using a mutable {@link
 * Checkpoint} object instead of a checkpoint string, and merging the
 * {@code startTraversal} and {@code resumeTraversal} methods.
 * Implementations should interrogate the {@code Checkpoint} to see if
 * the traversal is being started from the beginning.
 */
interface Traverser {
  /** @see com.google.enterprise.connector.spi.TraversalManager#setBatchHint */
  void setBatchHint(int batchHint) throws RepositoryException;

  /**
   * @see com.google.enterprise.connector.spi.TraversalManager#startTraversal
   * @see com.google.enterprise.connector.spi.TraversalManager#resumeTraversal
   */
  DocumentList getDocumentList(Checkpoint checkpoint)
      throws RepositoryException;

  void setTraversalContext(TraversalContext traversalContext);
}
