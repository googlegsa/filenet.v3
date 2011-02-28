//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IActiveMarking;

import com.filenet.api.constants.AccessRight;
import com.filenet.api.security.ActiveMarking;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FnActiveMarking implements IActiveMarking {
	private ActiveMarking marking;
	private Logger logger = null;
	public int ACCESS_MASK_LEVEL = AccessRight.VIEW_CONTENT_AS_INT;

	public FnActiveMarking(ActiveMarking marking) {
		this.marking = marking;
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public ActiveMarking getActiveMarking() {
		return this.marking;
	}

	public boolean authorize(String username) {

		boolean hasAccess = false;
		logger.log(Level.FINE, "Authorizing user:[" + username
				+ "] For marking set value : "
				+ this.marking.get_Marking().get_MarkingValue());

		hasAccess = ((new FnPermissions(
				this.marking.get_Marking().get_Permissions())).authorizeMarking(username));

		if (hasAccess) {
			logger.log(Level.FINE, " User: [" + username
					+ "] has USE right for the marking value : "
					+ this.marking.get_Marking().get_MarkingValue());

		} else {
			logger.log(Level.FINE, " User: [" + username
					+ "] does not have USE right for the marking value : "
					+ this.marking.get_Marking().get_MarkingValue());
			logger.log(Level.FINE, " Authorizing User: [" + username
					+ "] for Constraint mask with marking value : "
					+ this.marking.get_Marking().get_MarkingValue());

			hasAccess = checkConstraintMask();
		}
		return hasAccess;
	}

	private boolean checkConstraintMask() {

		if (!(((this.marking.get_Marking().get_ConstraintMask()) & ACCESS_MASK_LEVEL) == ACCESS_MASK_LEVEL)) {
			logger.log(Level.INFO, "Authorization is Successful for Constraint mask with marking value : "
					+ this.marking.get_Marking().get_MarkingValue());
			return true;
		} else {
			logger.log(Level.WARNING, "Authorization FAILED due to insufficient Access Security Levels. Minimum expected Access Security Level is \"View Content\"");
			return false;
		}
	}
}
