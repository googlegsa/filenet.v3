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
import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FnUser implements IUser {
  private final String shortName;
  private final String userName;
  private final String distinguishedName;
  private final String email;
  private final Map<String,Group> groups;

  public FnUser(User user) {
    this.shortName = user.get_ShortName();
    this.userName = user.get_Name();
    this.distinguishedName = user.get_DistinguishedName();
    this.email = user.get_Email();
    this.groups = new HashMap<String,Group>();
    initUserGroups(user.get_MemberOfGroups());
  }

  @SuppressWarnings("unchecked")
  private void initUserGroups(GroupSet groupSet) {
    Iterator<Group> iter = groupSet.iterator();
    while (iter.hasNext()) {
      Group grp = iter.next();
      groups.put(grp.get_Name().toLowerCase(), grp);
    }
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
  public Collection<Group> getGroups() {
    return Collections.unmodifiableCollection(groups.values());
  }

  @Override
  public Set<String> getGroupNames() {
    return groups.keySet();
  }
}
