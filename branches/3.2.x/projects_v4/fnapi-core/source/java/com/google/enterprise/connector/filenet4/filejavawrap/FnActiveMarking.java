// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IActiveMarking;
import com.google.enterprise.connector.filenet4.filewrap.IUser;

import com.filenet.api.security.ActiveMarking;
import com.filenet.api.security.Marking;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class over the FileNet API class ActiveMarking. This class is
 * responsible to authorize a target user against the Access Control Entries for
 * the specific marking applied to the target document.
 *
 * @author Dhanashri_Deshpande
 */

public class FnActiveMarking implements IActiveMarking {
  private static final Logger LOGGER =
      Logger.getLogger(FnActiveMarking.class.getName());

  private final Marking marking;

  public FnActiveMarking(ActiveMarking marking) {
    this.marking = marking.get_Marking();
  }

  public Marking get_Marking() {
    return this.marking;
  }

  /**
   * Authorize user's access against the active marking of the target document.
   *
   * @param user which needs to be authorized.
   * @return True or False.
   */
  public boolean authorize(IUser user) {
    LOGGER.log(Level.FINEST,
        "Authorizing user: {0} [Marking: {1}, Constraint Mask: {2}]",
        new Object[] {user.getName(), marking.get_MarkingValue(),
            marking.get_ConstraintMask()});

    FnPermissions perms = new FnPermissions(marking.get_Permissions());
    return perms.authorizeMarking(user, marking.get_ConstraintMask());
  }
}
