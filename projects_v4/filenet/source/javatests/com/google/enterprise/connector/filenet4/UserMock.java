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

import com.google.enterprise.connector.filenet4.EngineCollectionMocks.GroupSetMock;

import com.filenet.api.collection.GroupSet;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import java.util.Collection;

class UserMock extends SecurityPrincipalMock implements User {
  private final String shortName;
  private final String userName;
  private final String distinguishedName;
  private final String email;
  private final Collection<Group> userGroups;

  public UserMock(String shortName, String userName,
      String distinguishedName, String email, Collection<Group> userGroups) {
    this.shortName = shortName;
    this.userName = userName;
    this.distinguishedName = distinguishedName;
    this.email = email;
    this.userGroups = userGroups;
  }

  @Override
  public String get_ShortName() {
    return shortName;
  }

  @Override
  public String get_Name() {
    return userName;
  }

  @Override
  public String get_DistinguishedName() {
    return distinguishedName;
  }

  @Override
  public String get_Email() {
    return email;
  }

  @Override
  public GroupSet get_MemberOfGroups() {
    return new GroupSetMock(userGroups);
  }

  @Override
  public String get_DisplayName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String get_Id() {
    throw new UnsupportedOperationException();
  }
}
