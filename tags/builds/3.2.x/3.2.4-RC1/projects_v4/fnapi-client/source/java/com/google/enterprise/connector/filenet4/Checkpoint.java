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

class Checkpoint {

  public enum JsonField {
    UUID("uuid"),
    UUID_DELETION_EVENT("uuidToDelete"),
    UUID_CUSTOM_DELETED_DOC("uuidToDeleteDocs"),
    LAST_MODIFIED_TIME("lastModified"),
    LAST_DELETION_EVENT_TIME("lastRemoveDate"),
    LAST_CUSTOM_DELETION_TIME("lastModifiedDate");

    private final String fieldName;

    private JsonField(String name) {
      this.fieldName = name;
    }

    @Override
    public String toString() {
      return fieldName;
    }
  }
}
