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

package com.google.enterprise.connector.filenet4.mock;

import com.google.enterprise.connector.filenet4.filewrap.IUser;

import com.filenet.api.security.Group;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FileUserMock implements IUser {
  private final String shortName;
  private final String userName;
  private final String distinguishedName;
  private final String email;
  private final Map<String,Group> userGroups;

  public FileUserMock(String shortName, String userName,
      String distinguishedName, String email, Map<String,Group> userGroups) {
    this.shortName = shortName;
    this.userName = userName;
    this.distinguishedName = distinguishedName;
    this.email = email;
    this.userGroups = userGroups;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  @Override
  public String getName() {
    return userName;
  }

  @Override
  public String getDistinguishedName() {
    return distinguishedName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public Collection<Group> getGroups() {
    return userGroups.values();
  }

  @Override
  public Set<String> getGroupNames() {
    return userGroups.keySet();
  }

}
