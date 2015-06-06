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

package com.google.enterprise.connector.filenet4.api;

import com.filenet.api.collection.GroupSet;
import com.filenet.api.security.User;

public class FnUser implements IUser {
  private final String shortName;
  private final String userName;
  private final String distinguishedName;
  private final String email;
  private final GroupSet groups;

  public FnUser(User user) {
    this.shortName = user.get_ShortName();
    this.userName = user.get_Name();
    this.distinguishedName = user.get_DistinguishedName();
    this.email = user.get_Email();
    this.groups = user.get_MemberOfGroups();
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
  public String get_Email() {
    return email;
  }

  @Override
  public String get_DistinguishedName() {
    return distinguishedName;
  }

  @Override
  public GroupSet get_MemberOfGroups() {
    return groups;
  }
}
