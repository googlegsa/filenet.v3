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
import com.google.enterprise.connector.filenet4.filewrap.IMarking;

import com.filenet.api.collection.ActiveMarkingList;
import com.filenet.api.security.ActiveMarking;
import com.filenet.api.security.Marking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class FnActiveMarkingList implements IActiveMarkingList {
  private static final Logger LOGGER =
      Logger.getLogger(FnActiveMarkingList.class.getName());

  private final ArrayList<FnMarking> markings;

  public FnActiveMarkingList(ActiveMarkingList activeMarkings) {
    ArrayList<FnMarking> list = new ArrayList<FnMarking>();
    for (Object activeMarking : activeMarkings) {
      Marking marking = ((ActiveMarking) activeMarking).get_Marking();
      list.add(new FnMarking(marking));
    }
    this.markings = list;
  }

  @Override
  public Iterator<? extends IMarking> iterator() {
    return markings.iterator();
  }
}
