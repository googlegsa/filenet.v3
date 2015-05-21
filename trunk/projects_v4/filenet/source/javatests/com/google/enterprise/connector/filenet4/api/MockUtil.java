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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.filenet4.TestConnection;

import com.filenet.api.security.Group;

import java.util.HashMap;
import java.util.Map;

public class MockUtil {

  public static String getDistinguishedName(String dnsUserName) {
    String shortName;
    String domain;
    if (dnsUserName.contains("@")) {
      String[] names = dnsUserName.split("@");
      shortName = names[0];
      domain = names[1];
    } else {
      return "cn=" + dnsUserName;
    }
    String distinguishedName;
    if (domain.contains(".")) {
      String[] dcs = domain.split("\\.");
      StringBuilder builder = new StringBuilder();
      builder.append("cn=").append(shortName);
      for (String dc : dcs) {
        builder.append(",dc=").append(dc);
      }
      distinguishedName = builder.toString();
    } else {
      distinguishedName = "cn=" + shortName + ",dc=" + domain;
    }
    return distinguishedName;
  }

  public static IUser createAdministratorUser() {
    String userName = TestConnection.adminUsername + "@"
        + TestConnection.domain;
    String shortName = "administrator";
    String distinguishedName = getDistinguishedName(userName);

    String groupName = "administrators@" + TestConnection.domain;
    Map<String,Group> groups = new HashMap<String,Group>();
    GroupMock groupMock1 =
        new GroupMock(groupName, "administrators",
            getDistinguishedName(groupName));
    groups.put(groupMock1.get_Name(), groupMock1);

    FileUserMock user = new FileUserMock(shortName, userName, distinguishedName,
        userName, groups);
    return user;
  }

  public static IUser createUserWithShortName(String shortName) {
    String userName = shortName + "@" + TestConnection.domain;
    String distinguishedName = getDistinguishedName(userName);

    Map<String,Group> groups = new HashMap<String,Group>();
    Group everyone = createEveryoneGroup();
    groups.put(everyone.get_Name(), everyone);

    FileUserMock user = new FileUserMock(shortName, userName, distinguishedName,
        userName, groups);
    return user;
  }

  public static IUser createUserWithDomain(String shortName, String domain) {
    String userName = shortName + "@" + domain;
    String distinguishedName;
    if (domain.contains(".")) {
      String[] dcs = domain.split(".");
      StringBuilder builder = new StringBuilder();
      builder.append("cn=").append(shortName);
      for (String dc : dcs) {
        builder.append(",dc=").append(dc);
      }
      distinguishedName = builder.toString();
    } else {
      distinguishedName = "cn=" + shortName + ",dc=" + domain;
    }

    Group everyone = createEveryoneGroup();
    Map<String,Group> groups = ImmutableMap.of(everyone.get_Name(), everyone);

    FileUserMock user = new FileUserMock(shortName, userName, distinguishedName,
        userName, groups);
    return user;
  }
  
  public static Group createEveryoneGroup() {
    String groupName = "everyone@" + TestConnection.domain;
    return new GroupMock(groupName, "everyone",
        getDistinguishedName(groupName));
  }

  public static IUser createBlankUser() {
    return new FileUserMock("", "", "", "", null);
  }

  /** Prevents instantiation */
  private MockUtil() {
    throw new AssertionError();
  }
}
