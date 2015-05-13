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

import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a mutable checkpoint. This class is not thread-safe, and
 * must be synchronized externally if thread safety is required.
 */
class Checkpoint {
  private static final Logger logger =
      Logger.getLogger(Checkpoint.class.getName());

  public enum JsonField {
    UUID("uuid"),
    UUID_DELETION_EVENT("uuidToDelete"),
    UUID_CUSTOM_DELETED_DOC("uuidToDeleteDocs"),
    UUID_SECURITY_POLICY("uuidPolicy"),
    LAST_MODIFIED_TIME("lastModified"),
    LAST_DELETION_EVENT_TIME("lastRemoveDate"),
    LAST_CUSTOM_DELETION_TIME("lastModifiedDate"),
    LAST_SECURITY_POLICY_TIME("lastPolicyDate");

    private final String fieldName;

    private JsonField(String name) {
      this.fieldName = name;
    }

    @Override
    public String toString() {
      return fieldName;
    }
  }

  private final JSONObject jo;

  private boolean isEmpty;

  public Checkpoint() {
    jo = new JSONObject();
    isEmpty = true;
  }

  public Checkpoint(String checkpoint) throws RepositoryException {
    try {
      jo = new JSONObject(checkpoint);
    } catch (JSONException e) {
      throw new RepositoryException(
          "Unable to initialize a JSON object for the checkpoint", e);
    }
    isEmpty = false;
  }

  /**
   * Gets whether the checkpoint is uninitialized.
   *
   * @return {@code true} if the checkpoint is uninitialized, or
   *     {@code false} if any checkpoint fields have been set
   */
  public boolean isEmpty() {
    return isEmpty;
  }

  /*
   * Helper method to compute the checkpoint date and UUID value.
   */
  public void setTimeAndUuid(JsonField jsonDateField, Date nextCheckpointDate,
      JsonField jsonUuidField, IId uuid) throws RepositoryException {
    Calendar cal = Calendar.getInstance();
    String dateString;
    try {
      if (nextCheckpointDate == null) {
        if (jo.isNull(jsonDateField.toString())) {
          dateString = Value.calendarToIso8601(cal);
        } else {
          dateString = jo.getString(jsonDateField.toString());
        }
      } else {
        cal.setTime(nextCheckpointDate);
        dateString = Value.calendarToIso8601(cal);
      }
      String guid;
      if (uuid == null) {
        if (jo.isNull(jsonUuidField.toString())) {
          guid = "";
        } else {
          guid = jo.getString(jsonUuidField.toString());
        }
      } else {
        guid = uuid.toString();
      }
      jo.put(jsonUuidField.toString(), guid);
      jo.put(jsonDateField.toString(), dateString);
      isEmpty = false;
      logger.log(Level.FINE, "Set new checkpoint for {0} field to {1}, "
          + "{2} field to {3}", new Object[] {
              jsonDateField.toString(), dateString,
              jsonUuidField.toString(), uuid});
    } catch (JSONException e) {
      throw new RepositoryException("Failed to set JSON values for fields: "
          + jsonDateField.toString() + " or " + jsonUuidField.toString(), e);
    }
  }

  /**
   * Gets the given field from the checkpoint.
   *
   * @return the string value for the field
   * @throws RepositoryException if the field is uninitialized or the
   *     value is not a string
   */
  public String getString(JsonField jsonField) throws RepositoryException {
    try {
      return jo.getString(jsonField.toString());
    } catch (JSONException e) {
      throw new RepositoryException("Illegal checkpoint object: could not get "
          + jsonField + " from checkpoint: " + this);

    }
  }

  @Override
  public String toString() {
    return jo.toString();
  }
}
