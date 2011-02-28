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

import com.google.enterprise.connector.filenet4.filewrap.IActiveMarkingList;

import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.security.ActiveMarking;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnActiveMarkingList implements IActiveMarkingList {
	private ActiveMarkingList markings;
	private Logger logger = null;

	public FnActiveMarkingList(ActiveMarkingList markings) {
		this.markings = markings;
		logger = Logger.getLogger(FnDocument.class.getName());
	}

	public IActiveMarkingList getActiveMarkings() {
		return this;
	}

	public boolean authorize(String username) {

		Iterator<ActiveMarking> markings = this.markings.iterator();

		while (markings.hasNext()) {
			logger.log(Level.INFO, "Authorizing user :[" + username
					+ "] for Marking Sets ");

			ActiveMarking marking = markings.next();
			FnActiveMarking currentMarking = new FnActiveMarking(marking);

			if (!(currentMarking.authorize(username))) {
				logger.log(Level.INFO, "User "
						+ username
						+ " is not authorized for Marking value : "
						+ currentMarking.getActiveMarking().get_Marking().get_MarkingValue());
				return false;
			}
		}
		logger.log(Level.INFO, "User " + username
				+ " is authorized with all Marking Sets ");
		return true;
	}
}
