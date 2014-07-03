// Copyright 2007-2010 Google Inc.  All Rights Reserved.
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

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Concrete Document class for deletions. */
public class FileDeleteDocument implements Document {
  private static final Logger logger =
      Logger.getLogger(FileDeleteDocument.class.getName());

  private final IId versionId;
  private final Date timeStamp;

  public FileDeleteDocument(IId commonVersionId, Date timeStamp) {
    this.versionId = commonVersionId;
    this.timeStamp = timeStamp;
  }

  @Override
  public Property findProperty(String name) {
    LinkedList<Value> list = new LinkedList<Value>();

    if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      Calendar tmpCal = Calendar.getInstance();
      tmpCal.setTime(timeStamp);
      list.add(Value.getDateValue(tmpCal));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getStringValue(SpiConstants.ActionType.DELETE.toString()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      logger.log(Level.FINEST, "Getting property: " + name);
      list.add(Value.getStringValue(versionId.toString()));
      return new SimpleProperty(list);
    } else {
      return null;
    }
  }

  @Override
  public Set<String> getPropertyNames() {
    return ImmutableSet.of();
  }
}
