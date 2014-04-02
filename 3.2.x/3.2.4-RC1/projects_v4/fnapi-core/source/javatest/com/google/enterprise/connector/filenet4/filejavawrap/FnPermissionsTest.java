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
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
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
    AccessPermissionMock creatorOwnerPerm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    creatorOwnerPerm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    creatorOwnerPerm.set_GranteeType(SecurityPrincipalType.USER);
    creatorOwnerPerm.set_GranteeName("#CREATOR-OWNER");
    perms.add(creatorOwnerPerm);

    FnPermissions testPerms = new FnPermissions(perms, user.getName());
    assertTrue(testPerms.authorize(user));
  }

  public void testUserName() {
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName(user.getName());
    perms.add(perm);

    // Test internet name
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testShortName() {
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm.set_AccessMask(AccessLevel.VIEW_AS_INT);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName(user.getDistinguishedName());
    perms.add(perm);

    // Test distinguished name
    FnPermissions testPerms = new FnPermissions(perms);
    assertTrue(testPerms.authorize(user));
  }

  public void testInvalidUser() {
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
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

  public void testEmptyPermissionList() {
    assertEquals("Access permission list is not empty", 0, perms.size());
    FnPermissions emptyPerms = new FnPermissions(perms);
    assertEquals(0, emptyPerms.getAllowUsers().size());
    assertEquals(0, emptyPerms.getAllowGroups().size());
    assertEquals(0, emptyPerms.getDenyUsers().size());
    assertEquals(0, emptyPerms.getDenyGroups().size());
  }

  private void populateAces(int maxPerGroup, boolean includeAllowUsers,
      boolean includeDenyUsers, boolean includeAllowGroups,
      boolean includeDenyGroups, PermissionSource permSrc) {
    if (includeAllowUsers) {
      addAces(maxPerGroup, AccessType.ALLOW, SecurityPrincipalType.USER,
          AccessLevel.VIEW_AS_INT, 0, permSrc);
    }
    if (includeDenyUsers) {
      addAces(maxPerGroup, AccessType.DENY, SecurityPrincipalType.USER,
          AccessLevel.VIEW_AS_INT, 0, permSrc);
    }
    if (includeAllowGroups) {
      addAces(maxPerGroup, AccessType.ALLOW, SecurityPrincipalType.GROUP,
          AccessLevel.VIEW_AS_INT, 0, permSrc);
    }
    if (includeDenyGroups) {
      addAces(maxPerGroup, AccessType.DENY, SecurityPrincipalType.GROUP,
          AccessLevel.VIEW_AS_INT, 0, permSrc);
    }
    perms.shuffle();
  }

  private void addAce(PermissionSource permSrc,
      SecurityPrincipalType secPrincipalType, AccessType accessType,
      int accessMask, int inheritableDepth, String ace) {
    AccessPermissionMock perm = new AccessPermissionMock(permSrc);
    perm.set_GranteeType(secPrincipalType);
    perm.set_AccessType(accessType);
    perm.set_AccessMask(accessMask);
    perm.set_InheritableDepth(inheritableDepth);
    perm.set_GranteeName(ace);
    perms.add(perm);
  }

  private void addAces(int numOfAces, AccessType accessType,
      SecurityPrincipalType secType, int accessMask, int inheritDepth,
      PermissionSource... permSrcs) {
    for (PermissionSource permSrc : permSrcs) {
      for (int i = 0; i < numOfAces; i++) {
        String grantee = permSrc.toString() + " "
            + accessType.toString().toLowerCase() + " "
            + secType.toString().toLowerCase() + " " + i;
        addAce(permSrc, secType, accessType, accessMask, inheritDepth, grantee);
      }
    }
  }

  public void testEmptyAllowUsers() {
    populateAces(10, false, true, true, true, PermissionSource.SOURCE_DIRECT);
    FnPermissions testPerms = new FnPermissions(perms);
    assertEquals(0, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyDenyUsers() {
    populateAces(10, true, false, true, true, PermissionSource.SOURCE_DIRECT);
    FnPermissions testPerms = new FnPermissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(0, testPerms.getDenyUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyAllowGroups() {
    populateAces(10, true, true, false, true, PermissionSource.SOURCE_DIRECT);
    FnPermissions testPerms = new FnPermissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(0, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyDenyGroups() {
    populateAces(10, true, true, true, false, PermissionSource.SOURCE_DIRECT);
    FnPermissions testPerms = new FnPermissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(0, testPerms.getDenyGroups().size());
  }

  private FnPermissions getObjectUnderTest(int maxAllowUsers,
      int maxAllowGroups, int maxDenyUsers, int maxDenyGroups,
      PermissionSource... permSrcs) {
    addAces(maxAllowUsers, AccessType.ALLOW, SecurityPrincipalType.USER,
        AccessLevel.VIEW_AS_INT, 0, permSrcs);
    addAces(maxAllowGroups, AccessType.ALLOW, SecurityPrincipalType.GROUP,
        AccessLevel.VIEW_AS_INT, 0, permSrcs);
    addAces(maxDenyUsers, AccessType.DENY, SecurityPrincipalType.USER,
        AccessLevel.VIEW_AS_INT, 0, permSrcs);
    addAces(maxDenyGroups, AccessType.DENY, SecurityPrincipalType.GROUP,
        AccessLevel.VIEW_AS_INT, 0, permSrcs);
    perms.shuffle();

    return new FnPermissions(perms);
  }

  private void assertSetContains(Set<String> theSet, String prefix, int size) {
    for (int i = 0; i < size; i++) {
      assertTrue(theSet.contains(prefix + i));
    }
  }

  public void testGetAllowUsers() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualAllowUsers = testPerms.getAllowUsers();
    assertEquals(8, actualAllowUsers.size());
    assertSetContains(actualAllowUsers,
        PermissionSource.SOURCE_DIRECT.toString() + " allow user ", 8);
  }

  public void testGetAllowUsersBySource() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT, PermissionSource.SOURCE_DEFAULT);
    Set<String> inheritAllowUsers =
        testPerms.getAllowUsers(PermissionSource.SOURCE_PARENT);
    assertEquals(0, inheritAllowUsers.size());

    Set<String> actualDirectAllowUsers =
        testPerms.getAllowUsers(PermissionSource.SOURCE_DIRECT);
    assertEquals(8, actualDirectAllowUsers.size());
    assertSetContains(actualDirectAllowUsers,
        PermissionSource.SOURCE_DIRECT.toString() + " allow user ", 8);
  }

  public void testGetDenyUsers() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualDenyUsers = testPerms.getDenyUsers();
    assertEquals(6, actualDenyUsers.size());
    assertSetContains(actualDenyUsers,
        PermissionSource.SOURCE_DIRECT.toString() + " deny user ", 6);
  }

  public void testGetDenyUsersBySource() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_PARENT, PermissionSource.SOURCE_DEFAULT);
    Set<String> directDenyUsers =
        testPerms.getDenyUsers(PermissionSource.SOURCE_DIRECT);
    assertEquals(0, directDenyUsers.size());

    Set<String> actualInheritDenyUsers =
        testPerms.getDenyUsers(PermissionSource.SOURCE_PARENT);
    assertEquals(6, actualInheritDenyUsers.size());
    assertSetContains(actualInheritDenyUsers,
        PermissionSource.SOURCE_PARENT.toString() + " deny user ", 6);
  }

  public void testGetAllowGroups() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualAllowGroups = testPerms.getAllowGroups();
    assertEquals(7, actualAllowGroups.size());
    assertSetContains(actualAllowGroups,
        PermissionSource.SOURCE_DIRECT.toString() + " allow group ", 7);
  }

  public void testGetAllowGroupsBySource() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT, PermissionSource.SOURCE_DEFAULT);
    Set<String> inheritAllowGroups =
        testPerms.getAllowGroups(PermissionSource.SOURCE_PARENT);
    assertEquals(0, inheritAllowGroups.size());

    Set<String> actualDirectAllowGroups =
        testPerms.getAllowGroups(PermissionSource.SOURCE_DIRECT);
    assertEquals(7, actualDirectAllowGroups.size());
    assertSetContains(actualDirectAllowGroups,
            PermissionSource.SOURCE_DIRECT.toString() + " allow group ", 7);
  }

  public void testGetDenyGroups() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualDenyGroups = testPerms.getDenyGroups();
    assertEquals(5, actualDenyGroups.size());
    assertSetContains(actualDenyGroups,
        PermissionSource.SOURCE_DIRECT.toString() + " deny group ", 5);
  }

  public void testGetDenyGroupsBySource() {
    FnPermissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_PARENT, PermissionSource.SOURCE_DEFAULT);
    Set<String> directDenyGroups =
        testPerms.getDenyGroups(PermissionSource.SOURCE_DIRECT);
    assertEquals(0, directDenyGroups.size());

    Set<String> actualInheritDenyGroups =
        testPerms.getDenyGroups(PermissionSource.SOURCE_PARENT);
    assertEquals(5, actualInheritDenyGroups.size());
    assertSetContains(actualInheritDenyGroups,
        PermissionSource.SOURCE_PARENT.toString() + " deny group ", 5);
  }

}
