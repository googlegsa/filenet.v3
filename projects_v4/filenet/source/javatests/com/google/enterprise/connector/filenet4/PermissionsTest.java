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

import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.api.AccessPermissionListMock;
import com.google.enterprise.connector.filenet4.api.AccessPermissionMock;
import com.google.enterprise.connector.filenet4.api.IUser;
import com.google.enterprise.connector.filenet4.api.MockUtil;

import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.Group;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PermissionsTest extends TestCase {
  private static int VIEW_ACCESS_RIGHTS =
      AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT;

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

  public void testAllowCreatorOwnerWithViewLevel() {
    testCreatorOwnerAccess(AccessType.ALLOW, AccessLevel.VIEW_AS_INT, true);
  }

  public void testAllowCreatorOwnerWithViewAccessRights() {
    testCreatorOwnerAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS, true);
  }

  public void testDenyCreatorOwner() {
    testCreatorOwnerAccess(AccessType.DENY, VIEW_ACCESS_RIGHTS, false);
  }

  public void testDenyCreatorOwnerWithoutViewContentRight() {
    testCreatorOwnerAccess(AccessType.DENY, AccessRight.READ_AS_INT, false);
  }

  public void testDenyCreatorOwnerWithoutReadRight() {
    testCreatorOwnerAccess(AccessType.DENY, AccessRight.VIEW_CONTENT_AS_INT,
        false);
  }

  private void testCreatorOwnerAccess(AccessType accessType, int accessRights,
      boolean expectedResult) {
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm.set_AccessType(accessType);
    perm.set_AccessMask(accessRights);
    perm.set_GranteeType(SecurityPrincipalType.USER);
    perm.set_GranteeName("#CREATOR-OWNER");
    perms.add(perm);

    Permissions testPerms = new Permissions(perms, user.get_Name());
    assertEquals(expectedResult, testPerms.authorize(user));
  }

  public void testCreatorOwnerWithBothAllowAndDeny() {
    AccessPermissionMock creatorOwnerPermDeny =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    creatorOwnerPermDeny.set_AccessType(AccessType.DENY);
    creatorOwnerPermDeny.set_AccessMask(AccessRight.DELETE_AS_INT);
    creatorOwnerPermDeny.set_GranteeType(SecurityPrincipalType.USER);
    creatorOwnerPermDeny.set_GranteeName(user.get_Name());
    perms.add(creatorOwnerPermDeny);

    AccessPermissionMock creatorOwnerPermAllow =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    creatorOwnerPermAllow.set_AccessType(AccessType.ALLOW);
    creatorOwnerPermAllow.set_AccessMask(VIEW_ACCESS_RIGHTS);
    creatorOwnerPermAllow.set_GranteeType(SecurityPrincipalType.USER);
    creatorOwnerPermAllow.set_GranteeName(user.get_Name());
    perms.add(creatorOwnerPermAllow);

    Permissions testPerms = new Permissions(perms, user.get_Name());
    assertTrue(testPerms.authorize(user));
  }

  public void testAllowUserWithoutViewContentRight() {
    testUserAccess(AccessType.ALLOW, AccessRight.READ_AS_INT, user, false);
  }

  public void testAllowUserWithoutReadRight() {
    testUserAccess(AccessType.ALLOW, AccessRight.VIEW_CONTENT_AS_INT, user,
        false);
  }

  public void testAllowUserWithoutViewRights() {
    int accessRights = AccessRight.WRITE_OWNER_AS_INT
        | AccessRight.WRITE_ACL_AS_INT | AccessRight.DELETE_AS_INT;
    testUserAccess(AccessType.ALLOW, accessRights, user, false);
  }

  public void testDenyUserWithoutViewRights() {
    int accessRights = AccessRight.WRITE_OWNER_AS_INT
        | AccessRight.WRITE_ACL_AS_INT | AccessRight.DELETE_AS_INT;
    testUserAccess(AccessType.DENY, accessRights, user, false);
  }

  private void testUserAccess(AccessType accessType, int accessRights,
      IUser testUser, boolean expectedResult) {
    testUserAccess(accessType, accessRights, testUser.get_Name(), testUser,
        expectedResult);
  }

  private void testUserAccess(AccessType accessType, int accessRights,
      String granteeName, IUser testUser, boolean expectedResult) {
    testAccess(accessType, accessRights, SecurityPrincipalType.USER,
        granteeName, testUser, expectedResult);
  }

  private void testGroupAccess(AccessType accessType, int accessRights,
      String granteeName, IUser testUser, boolean expectedResult) {
    testAccess(accessType, accessRights, SecurityPrincipalType.GROUP,
        granteeName, testUser, expectedResult);
  }

  private void testAccess(AccessType accessType, int accessRights,
      SecurityPrincipalType granteeType, String granteeName, IUser testUser,
      boolean expectedResult) {
    AccessPermissionMock perm =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm.set_AccessType(accessType);
    perm.set_AccessMask(accessRights);
    perm.set_GranteeType(granteeType);
    perm.set_GranteeName(granteeName);
    perms.add(perm);

    Permissions testPerms = new Permissions(perms);
    assertEquals(expectedResult, testPerms.authorize(testUser));
  }

  public void testShortName() {
    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    testUserAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS, jsmith, true);
  }

  public void testShortNameWithDifferentDomain() {
    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    testUserAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS, "jsmith@example.com",
        jsmith, false);
  }

  public void testDistinguishedName() {
    testUserAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        user.get_DistinguishedName(), user, true);
  }

  public void testInvalidUser() {
    IUser invalidUser = MockUtil.createBlankUser();
    testUserAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS, user.get_Name(),
        invalidUser, false);
  }

  public void testAuthenticatedUsers() {
    testAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        SecurityPrincipalType.GROUP, "#AUTHENTICATED-USERS", user, true);
  }

  public void testUserGroupAccess_WithDomainName() {
    Set<String> userGroups = getGroupNames(user);
    assertTrue(userGroups.contains("administrators@" + TestConnection.domain));
    testGroupAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        "administrators@" + TestConnection.domain, user, true);
  }

  public void testUserGroupAccess_WithShortName() {
    Group everyone = MockUtil.createEveryoneGroup();
    assertEquals(everyone.get_ShortName(), "everyone");

    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    assertTrue(getGroupNames(jsmith).contains(everyone.get_Name()));

    testGroupAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        everyone.get_ShortName(), jsmith, false);
  }

  public void testUserGroupAccess_WithDistinguishedName() {
    Group everyone = MockUtil.createEveryoneGroup();
    assertEquals(everyone.get_DistinguishedName(),
        MockUtil.getDistinguishedName("everyone@" + TestConnection.domain));

    IUser jsmith = MockUtil.createUserWithShortName("jsmith");
    assertTrue(getGroupNames(jsmith).contains(everyone.get_Name()));

    testGroupAccess(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        everyone.get_DistinguishedName(), jsmith, true);
  }

  public void testUserGroupAccess_HavingBothAllowAndDeny() {
    Set<String> userGroups = getGroupNames(user);
    assertTrue(userGroups.contains("administrators@" + TestConnection.domain));

    AccessPermissionMock permAllow =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    permAllow.set_AccessType(AccessType.ALLOW);
    permAllow.set_AccessMask(VIEW_ACCESS_RIGHTS);
    permAllow.set_GranteeType(SecurityPrincipalType.USER);
    permAllow.set_GranteeName(user.get_Name());
    perms.add(permAllow);

    AccessPermissionMock permDeny =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    permDeny.set_AccessType(AccessType.DENY);
    permDeny.set_AccessMask(VIEW_ACCESS_RIGHTS);
    permDeny.set_GranteeType(SecurityPrincipalType.GROUP);
    permDeny.set_GranteeName("administrators@" + TestConnection.domain);
    perms.add(permDeny);

    Permissions testPermsDenyGroup = new Permissions(perms);
    assertFalse(testPermsDenyGroup.authorize(user));
  }

  /*
   * TODO(jlacey): This is copied from FileAuthenticationManager, and
   * could be moved to FileUtil and shared.
   */
  private Set<String> getGroupNames(IUser user) {
    HashSet<String> groups = new HashSet<>();
    Iterator<?> iter = user.get_MemberOfGroups().iterator();
    while (iter.hasNext()) {
      Group group = (Group) iter.next();
      groups.add(group.get_Name());
    }
    return groups;
  }

  public void testEmptyPermissionList() {
    assertEquals("Access permission list is not empty", 0, perms.size());
    Permissions emptyPerms = new Permissions(perms);
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
    Permissions testPerms = new Permissions(perms);
    assertEquals(0, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyDenyUsers() {
    populateAces(10, true, false, true, true, PermissionSource.SOURCE_DIRECT);
    Permissions testPerms = new Permissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(0, testPerms.getDenyUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyAllowGroups() {
    populateAces(10, true, true, false, true, PermissionSource.SOURCE_DIRECT);
    Permissions testPerms = new Permissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(0, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyGroups().size());
  }

  public void testEmptyDenyGroups() {
    populateAces(10, true, true, true, false, PermissionSource.SOURCE_DIRECT);
    Permissions testPerms = new Permissions(perms);
    assertEquals(10, testPerms.getAllowUsers().size());
    assertEquals(10, testPerms.getAllowGroups().size());
    assertEquals(10, testPerms.getDenyUsers().size());
    assertEquals(0, testPerms.getDenyGroups().size());
  }

  private Permissions getObjectUnderTest(int maxAllowUsers,
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

    return new Permissions(perms);
  }

  private void assertSetContains(Set<String> theSet, String prefix, int size) {
    for (int i = 0; i < size; i++) {
      assertTrue(theSet.contains(prefix + i));
    }
  }

  public void testGetAllowUsers() {
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualAllowUsers = testPerms.getAllowUsers();
    assertEquals(8, actualAllowUsers.size());
    assertSetContains(actualAllowUsers,
        PermissionSource.SOURCE_DIRECT.toString() + " allow user ", 8);
  }

  public void testGetAllowUsersBySource() {
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
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
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualDenyUsers = testPerms.getDenyUsers();
    assertEquals(6, actualDenyUsers.size());
    assertSetContains(actualDenyUsers,
        PermissionSource.SOURCE_DIRECT.toString() + " deny user ", 6);
  }

  public void testGetDenyUsersBySource() {
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
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
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualAllowGroups = testPerms.getAllowGroups();
    assertEquals(7, actualAllowGroups.size());
    assertSetContains(actualAllowGroups,
        PermissionSource.SOURCE_DIRECT.toString() + " allow group ", 7);
  }

  public void testGetAllowGroupsBySource() {
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
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
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
        PermissionSource.SOURCE_DIRECT);
    Set<String> actualDenyGroups = testPerms.getDenyGroups();
    assertEquals(5, actualDenyGroups.size());
    assertSetContains(actualDenyGroups,
        PermissionSource.SOURCE_DIRECT.toString() + " deny group ", 5);
  }

  public void testGetDenyGroupsBySource() {
    Permissions testPerms = getObjectUnderTest(8, 7, 6, 5,
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

  // Calculate constraint mask for rights that are unchecked in the marking.
  private int constraintMask(AccessRight... allowRights) {
    int mask = 0;
    for (AccessRight right : allowRights) {
      mask |= right.getValue();
    }
    return constraintMask(mask);
  }

  private int constraintMask(int allowRights) {
    return AccessLevel.FULL_CONTROL_AS_INT & ~allowRights;
  }

  private void testUserMarking(AccessType accessType, int accessMask,
      boolean expectedResult, AccessRight... allowRights) {
    IUser user1 = MockUtil.createUserWithDomain("user1", "foo.example.com");
    testMarking(accessType, accessMask, SecurityPrincipalType.USER,
        user1.get_Name(), user1, expectedResult, allowRights);
  }

  private void testMarking(AccessType accessType, int accessMask,
      SecurityPrincipalType secType, String granteeName, IUser testUser,
      boolean expectedResult, AccessRight... allowRights) {
    testMarking(accessType, accessMask, secType, granteeName, testUser,
        expectedResult, constraintMask(allowRights));
  }

  private void testMarking(AccessType accessType, int accessMask,
      SecurityPrincipalType secType, String granteeName, IUser testUser,
      boolean expectedResult, int constraintMask) {
    AccessPermissionMock perm1 =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm1.set_AccessType(accessType);
    perm1.set_AccessMask(accessMask);
    perm1.set_GranteeType(secType);
    perm1.set_GranteeName(granteeName);
    perms.add(perm1);

    Permissions testPerms = new Permissions(perms);
    assertEquals(expectedResult, testPerms.authorizeMarking(testUser,
        constraintMask));
  }

  public void testMarking_WithUseRight_AllowAce() {
      testUserMarking(AccessType.ALLOW, AccessRight.USE_MARKING_AS_INT, true,
    AccessRight.VIEW_CONTENT, AccessRight.READ);
    testUserMarking(AccessType.ALLOW, AccessRight.USE_MARKING_AS_INT, true,
        AccessRight.NONE);
  }

  public void testMarking_WithUseRight_DenyAce() {
    testUserMarking(AccessType.DENY, AccessRight.USE_MARKING_AS_INT, true,
        AccessRight.VIEW_CONTENT, AccessRight.READ);
    testUserMarking(AccessType.DENY, AccessRight.USE_MARKING_AS_INT, false,
        AccessRight.VIEW_CONTENT);
    testUserMarking(AccessType.DENY, AccessRight.USE_MARKING_AS_INT, false,
        AccessRight.READ);
  }

  public void testMarking_NoUseRight_ViewLevelConstraint() {
    IUser user1 = MockUtil.createUserWithDomain("user1", "foo.example.com");
    testMarking(AccessType.ALLOW, AccessRight.NONE_AS_INT,
        SecurityPrincipalType.USER, user1.get_Name(), user1, true,
        constraintMask(AccessLevel.VIEW_AS_INT));
    testMarking(AccessType.DENY, AccessRight.NONE_AS_INT,
        SecurityPrincipalType.USER, user1.get_Name(), user1, true,
        constraintMask(AccessLevel.VIEW_AS_INT));
  }

  public void testMarking_NoUseRight_AllowReadViewContentRights() {
    testUserMarking(AccessType.ALLOW, AccessRight.NONE_AS_INT, true,
        AccessRight.VIEW_CONTENT, AccessRight.READ);
    testUserMarking(AccessType.ALLOW, AccessRight.NONE_AS_INT, true,
        AccessRight.VIEW_CONTENT, AccessRight.READ, AccessRight.DELETE);
  }

  public void testMarking_NoUseRight_MissingAllowReadViewContentRights() {
    testUserMarking(AccessType.ALLOW, AccessRight.NONE_AS_INT, false,
        AccessRight.VIEW_CONTENT);
    testUserMarking(AccessType.ALLOW, AccessRight.NONE_AS_INT, false,
        AccessRight.READ);
  }

  public void testMarking_NoUseRight_DenyReadViewContentRights() {
    // Under live test, the DENY access type does not have any effects or
    // behaves the same as ALLOW; only constraint mask matters.
    // Testing constraint mask of read or view content rights.
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, false,
        AccessRight.VIEW_CONTENT);
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, false,
        AccessRight.READ);

    // Testing constraint mask of both read and view content rights.
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, true,
        AccessRight.VIEW_CONTENT, AccessRight.READ);
  }

  public void testMarking_NoUseRight_DenyOtherRights() {
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, false,
        AccessRight.DELETE);
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, false,
        AccessRight.WRITE);
    testUserMarking(AccessType.DENY, AccessRight.NONE_AS_INT, false,
        AccessRight.DELETE, AccessRight.WRITE, AccessRight.WRITE_ACL);
  }

  public void testMarking_NoUseRight_HavingBothAllowAndDeny() {
    IUser user1 = MockUtil.createUserWithDomain("user1", "foo.example.com");

    AccessPermissionMock perm1 =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm1.set_AccessType(AccessType.ALLOW);
    perm1.set_AccessMask(AccessRight.NONE_AS_INT);
    perm1.set_GranteeType(SecurityPrincipalType.USER);
    perm1.set_GranteeName(user1.get_Name());
    perms.add(perm1);

    // The access mask can be set to any value for DENY as it does not have any
    // effects.
    AccessPermissionMock perm2 =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    perm2.set_AccessType(AccessType.DENY);
    perm2.set_AccessMask(VIEW_ACCESS_RIGHTS);
    perm2.set_GranteeType(SecurityPrincipalType.USER);
    perm2.set_GranteeName(user1.get_Name());
    perms.add(perm2);

    Permissions testPerms = new Permissions(perms);
    assertEquals(true, testPerms.authorizeMarking(user1,
        constraintMask(AccessRight.VIEW_CONTENT, AccessRight.READ)));
  }

  public void testMarking_HavingBothAllowAndDenyUseRights() {
    IUser user1 = MockUtil.createUserWithDomain("user1", "foo.example.com");

    AccessPermissionMock allowUse =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    allowUse.set_AccessType(AccessType.ALLOW);
    allowUse.set_AccessMask(AccessRight.USE_MARKING_AS_INT);
    allowUse.set_GranteeType(SecurityPrincipalType.USER);
    allowUse.set_GranteeName(user1.get_Name());
    perms.add(allowUse);

    AccessPermissionMock denyUse =
        new AccessPermissionMock(PermissionSource.SOURCE_DIRECT);
    denyUse.set_AccessType(AccessType.DENY);
    denyUse.set_AccessMask(AccessRight.USE_MARKING_AS_INT);
    denyUse.set_GranteeType(SecurityPrincipalType.USER);
    denyUse.set_GranteeName(user1.get_Name());
    perms.add(denyUse);

    Permissions testPerms = new Permissions(perms);
    assertEquals(false, testPerms.authorizeMarking(user1,
        constraintMask(AccessRight.NONE_AS_INT)));
    assertEquals(true, testPerms.authorizeMarking(user1,
        constraintMask(AccessRight.VIEW_CONTENT, AccessRight.READ)));
  }

  public void testMarking_UserNotMatchingAnyAces() {
    IUser user1 = MockUtil.createUserWithDomain("user1", "foo.example.com");
    testMarking(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        SecurityPrincipalType.USER, "user2@bar.example.com", user1, true,
        AccessRight.READ, AccessRight.VIEW_CONTENT);
    testMarking(AccessType.ALLOW, VIEW_ACCESS_RIGHTS,
        SecurityPrincipalType.USER, "user2@bar.example.com", user1, false,
        AccessRight.NONE);
  }
}
