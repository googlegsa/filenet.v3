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

import com.google.enterprise.connector.filenet4.filewrap.IActiveMarkingList;
import com.google.enterprise.connector.filenet4.filewrap.IUser;

import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.security.ActiveMarking;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class over the FileNet API class ActiveMarkingList. This class is
 * responsible to authorize a target user against the Access Control Entries for
 * all the markings applied to the target document.
 *
 * @author Dhanashri_Deshpande
 */
public class FnActiveMarkingList implements IActiveMarkingList {
  private static final Logger LOGGER =
      Logger.getLogger(FnActiveMarkingList.class.getName());

  private final ActiveMarkingList markings;

  public FnActiveMarkingList(ActiveMarkingList markings) {
    this.markings = markings;
  }

  /**
   * To authorize a given user against the list of marking values Access
   * Control Entries for all the permission of the target document.
   *
   * @param Username which needs to be authorized.
   * @return True or False, depending on the success or failure of
   *         authorization.
   */
  @Override
  public boolean authorize(IUser user) {
    @SuppressWarnings("unchecked") Iterator<ActiveMarking> markings =
        this.markings.iterator();

    LOGGER.log(Level.FINER, "Authorizing user :[" + user.getName()
        + "] for Marking Sets");
    while (markings.hasNext()) {
      ActiveMarking marking = markings.next();
      FnActiveMarking currentMarking = new FnActiveMarking(marking);

      if (!(currentMarking.authorize(user))) {
        LOGGER.log(Level.FINER,
            "User {0} is not authorized for Marking value: {1}",
            new Object[] {user.getName(), currentMarking.getActiveMarking()
                .get_Marking().get_MarkingValue()});
        return false;
      }
    }
    LOGGER.log(Level.FINER, "User " + user.getName()
        + " is authorized to view the document");
    return true;
  }
}
