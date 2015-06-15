// Copyright 2013 Google Inc. All Rights Reserved.
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

import com.filenet.api.collection.GroupSet;
import com.filenet.api.collection.UserSet;
import com.filenet.api.security.Group;

class GroupMock extends SecurityPrincipalMock implements Group {
  private final String name;
  private final String shortName;
  private final String distinguishedName;

  public GroupMock(String name, String shortName, String distinguishedName) {
    this.name = name;
    this.shortName = shortName;
    this.distinguishedName = distinguishedName;
  }

  @Override
  public String get_DisplayName() {
    return name;
  }

  @Override
  public String get_DistinguishedName() {
    return this.distinguishedName;
  }

  @Override
  public GroupSet get_Groups() {
    return null;
  }

  @Override
  public String get_Id() {
    return null;
  }

  @Override
  public GroupSet get_MemberOfGroups() {
    return null;
  }

  @Override
  public String get_Name() {
    return name;
  }

  @Override
  public String get_ShortName() {
    return shortName;
  }

  @Override
  public UserSet get_Users() {
    return null;
  }
}
