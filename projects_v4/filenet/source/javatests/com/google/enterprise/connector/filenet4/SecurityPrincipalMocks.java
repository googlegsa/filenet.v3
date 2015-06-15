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

import com.google.common.collect.ImmutableList;

import com.filenet.api.security.Group;
import com.filenet.api.security.User;

public class SecurityPrincipalMocks {

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

  public static User createAdministratorUser() {
    String userName = TestConnection.adminUsername + "@"
        + TestConnection.domain;
    String shortName = "administrator";
    String distinguishedName = getDistinguishedName(userName);

    String groupName = "administrators@" + TestConnection.domain;
    Group groupMock1 = new GroupMock(groupName, "administrators",
        getDistinguishedName(groupName));

    UserMock user = new UserMock(shortName, userName, distinguishedName,
        userName, ImmutableList.of(groupMock1));
    return user;
  }

  public static User createUserWithShortName(String shortName) {
    String userName = shortName + "@" + TestConnection.domain;
    String distinguishedName = getDistinguishedName(userName);

    Group everyone = createEveryoneGroup();

    UserMock user = new UserMock(shortName, userName, distinguishedName,
        userName, ImmutableList.of(everyone));
    return user;
  }

  public static User createUserWithDomain(String shortName, String domain) {
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

    UserMock user = new UserMock(shortName, userName, distinguishedName,
        userName, ImmutableList.of(everyone));
    return user;
  }

  public static Group createEveryoneGroup() {
    String groupName = "everyone@" + TestConnection.domain;
    return new GroupMock(groupName, "everyone",
        getDistinguishedName(groupName));
  }

  public static User createBlankUser() {
    return new UserMock("", "", "", "", null);
  }

  /** Prevents instantiation */
  private SecurityPrincipalMocks() {
    throw new AssertionError();
  }
}
