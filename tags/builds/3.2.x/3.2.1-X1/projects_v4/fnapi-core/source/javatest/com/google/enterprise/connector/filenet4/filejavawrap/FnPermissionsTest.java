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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IUser;
import com.google.enterprise.connector.filenet4.mock.AccessPermissionListMock;
import com.google.enterprise.connector.filenet4.mock.AccessPermissionMock;
import com.google.enterprise.connector.filenet4.mock.MockUtil;

import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.Group;

import junit.framework.TestCase;

import java.util.Set;

public class FnPermissionsTest extends TestCase {
  private AccessPermissionListMock perms;
  private IUser user;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    perms = new AccessPermissionListMock();
    user = MockUtil.createAdministratorUser();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      perms.clear();
    } finally {
      super.tearDown();
    }
  }

  public void testCreatorOwner() {
    AccessPermissionMock creatorOwnerPerm = new AccessPermissionMock();
    creatorOwnerPerm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    creatorOwnerPerm.set_GranteeType(SecurityPrincipalType.USER);
    creatorOwnerPerm.set_GranteeName("#CREATOR-OWNER");
    perms.add(creatorOwnerPerm);

    FnPermissions testPerms = new FnPermissions(perms, user.getName());
    assertTrue(testPerms.authorize(user));
  }

  public void testUserName() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName(user.getName());
    perms.add(perm);

    // Test internet name
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testShortName() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName("jsmith@example.com");
    perms.add(perm);

    // Short name should not be authorized
    FnPermissions testPerms = new FnPermissions(perms);
    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    assertFalse(testPerms.authorize(jsmith));
  }

  public void testDistinguishedName() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName(user.getDistinguishedName());
    perms.add(perm);

    // Test distinguished name
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testInvalidUser() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName(user.getName());
    perms.add(perm);

    // Test invalid user
    FnPermissions testPerms = new FnPermissions(perms);
    IUser invalidUser = MockUtil.createBlankUser();
    assertFalse(testPerms.authorize(invalidUser));
  }

  public void testAuthenticatedUsers() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.GROUP);
    perm.set_GranteeName("#AUTHENTICATED-USERS");
    perms.add(perm);

    // Test #AUTHENTICATED-USERS
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testUserGroupAccess() {
    Set<String> userGroups = user.getGroupNames();
    assertTrue(userGroups.contains("administrators@" + TestConnection.domain));
    
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.GROUP);
    perm.set_GranteeName("administrators@" + TestConnection.domain);
    perms.add(perm);

    // Test user's member of group access
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testUserGroupAccessWithShortName() {
    Group everyone = MockUtil.createEveryoneGroup();
    assertEquals(everyone.get_ShortName(), "everyone");
    
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.GROUP);
    perm.set_GranteeName(everyone.get_ShortName());
    perms.add(perm);

    // Test user group access where grantee is a group with shortname.
    FnPermissions testPerms = new FnPermissions(perms);
    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    assertTrue(jsmith.getGroupNames().contains(everyone.get_Name()));
    assertFalse(testPerms.authorize(jsmith));
  }

  public void testUserGroupAccessWithDistinguishedName() {
    Group everyone = MockUtil.createEveryoneGroup();
    assertEquals(everyone.get_DistinguishedName(),
        MockUtil.getDistinguishedName("everyone@" + TestConnection.domain));
    
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.GROUP);
    perm.set_GranteeName(everyone.get_DistinguishedName());
    perms.add(perm);

    // Test user group access where grantee is a group with distinguished name
    FnPermissions testPerms = new FnPermissions(perms);
    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    assertTrue(jsmith.getGroupNames().contains(everyone.get_Name()));
    assertTrue(testPerms.authorize(jsmith));
  }

  public void testUsernameWithDomain() {
    AccessPermissionMock perm = new AccessPermissionMock();
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName("user@foo.example.com");
    perms.add(perm);

    // Test invalid user
    FnPermissions testPerms = new FnPermissions(perms);
    IUser invalidUser = MockUtil.createUserWithDomain("user",
        "bar.example.com");
    assertFalse(testPerms.authorize(invalidUser));
  }
}
