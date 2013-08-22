// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IActiveMarking;
import com.google.enterprise.connector.filenet4.filewrap.IUser;

import com.filenet.api.constants.AccessRight;
import com.filenet.api.security.ActiveMarking;

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
  private ActiveMarking marking;
  private static Logger LOGGER = Logger.getLogger(FnDocument.class.getName());
  private int VIEW_CONTENT_MASK_LEVEL = AccessRight.VIEW_CONTENT_AS_INT;
  private int VIEW_PROPERTIES_MASK_LEVEL = AccessRight.READ_AS_INT;
  private int MODIFY_OWNER_MASK_LEVEL = AccessRight.WRITE_OWNER_AS_INT;

  public FnActiveMarking(ActiveMarking marking) {
    this.marking = marking;
  }

  public ActiveMarking getActiveMarking() {
    return this.marking;
  }

  /**
   * To authorize a given user against the specific marking value's Access
   * Control Entries for all the permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of
   *         authorization.
   */
  public boolean authorize(IUser user) {
    boolean hasAccess = false;
    LOGGER.log(Level.INFO, "Authorizing user:[" + user.getName()
        + "] For marking set value : "
        + this.marking.get_Marking().get_MarkingValue());

    hasAccess = ((new FnPermissions(
        marking.get_Marking().get_Permissions())).authorizeMarking(user));

    // Check whether the user has USE rights over the document or not.
    // If user does not have USE rights then ConstraintMask check is
    // required.

    if (hasAccess) {
      LOGGER.log(Level.FINER, "User: [{0}] has USE right and is authorized"
          + " to view the document.  Marking: {1}",
          new Object[] {user.getName(),
              marking.get_Marking().get_MarkingValue()});
    } else {
      LOGGER.log(Level.FINER, "User: [{0}] does not have USE right or is not "
          + "authorized by the constraint mask: {1}",
          new Object[] {user.getName(),
              marking.get_Marking().get_MarkingValue()});
      hasAccess = checkConstraintMask();
    }
    return hasAccess;
  }

  /**
   * To check the access rights for 'View_Content' or above level of access
   * control, for the specific marking value of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the access control level applied for
   *         the marking value.
   */

  private boolean checkConstraintMask() {

    // Check whether the user has atleast 'View Content' right over the
    // document or not.
    // if (!(((this.marking.get_Marking().get_ConstraintMask()) &
    // ACCESS_LEVEL) == ACCESS_LEVEL)) {
    if ((!(((this.marking.get_Marking().get_ConstraintMask()) & VIEW_CONTENT_MASK_LEVEL) == VIEW_CONTENT_MASK_LEVEL))
            && ((!(((this.marking.get_Marking().get_ConstraintMask()) & VIEW_PROPERTIES_MASK_LEVEL) == VIEW_PROPERTIES_MASK_LEVEL)) || (!(((this.marking.get_Marking().get_ConstraintMask()) & MODIFY_OWNER_MASK_LEVEL) == MODIFY_OWNER_MASK_LEVEL)))) {
      LOGGER.log(Level.INFO, "Authorization is Successful for Constraint mask with marking value : "
              + this.marking.get_Marking().get_MarkingValue());
      return true;
    } else {
      LOGGER.log(Level.WARNING, "Authorization FAILED due to insufficient Access Security Levels. Minimum expected Access Security Level is \"View Content\"");
      return false;
    }
  }
}
