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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.filewrap.IMarking;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.Marking;

public class FnMarking implements IMarking {
  private final Marking marking;

  public FnMarking(Marking marking) {
    this.marking = marking;
  }

  @Override
  public String get_MarkingValue() {
    return marking.get_MarkingValue();
  }

  @Override
  public int get_ConstraintMask() {
    return marking.get_ConstraintMask();
  }

  @Override
  public AccessPermissionList get_Permissions() {
    return marking.get_Permissions();
  }
}
