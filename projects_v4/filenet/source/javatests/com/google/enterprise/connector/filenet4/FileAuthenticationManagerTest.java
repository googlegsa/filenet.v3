// Copyright 2007-2010 Google Inc. All Rights Reserved.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.EngineCollectionMocks.GroupSetMock;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FileAuthenticationManagerTest {
  private AuthenticationManager getObjectUnderTest(boolean pushAcls)
      throws RepositoryException {
    FileConnector connec = TestObjectFactory.newFileConnector();
    connec.setPushAcls(pushAcls);

    return connec.login().getAuthenticationManager();
  }

  private AuthenticationManager getObjectUnderTest(IConnection conn)
      throws RepositoryException {
    return new FileAuthenticationManager(conn, null, true);
  }

  @Test
  public void testAuthenticate() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity fai = new SimpleAuthenticationIdentity(
        TestConnection.username, TestConnection.password);
    AuthenticationResponse ar = fatm.authenticate(fai);
    assertEquals(true, ar.isValid());

    @SuppressWarnings("unchecked") List<Principal> groups =
        (List<Principal>) ar.getGroups();
    assertTrue(groups.size() > 1);

    boolean hasAuthUserGrp = false;
    for (Principal group : groups) {
      if (Permissions.AUTHENTICATED_USERS.equals(group.getName())) {
        hasAuthUserGrp = true;
      } else {
        assertTrue("Group: " + group.getName(), group.getName().contains("@"));
      }
    }
    assertTrue("Missing " + Permissions.AUTHENTICATED_USERS + " group",
        hasAuthUserGrp);
  }

  @Test
  public void testAuthenticateNoGroupsIfPushAclsFalse()
      throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    AuthenticationManager fatm = getObjectUnderTest(false);
    SimpleAuthenticationIdentity fai = new SimpleAuthenticationIdentity(
        TestConnection.username, TestConnection.password);
    AuthenticationResponse ar = fatm.authenticate(fai);
    assertEquals(true, ar.isValid());
    assertNull(ar.getGroups());
  }

  @Test
  public void testAuthenticate_fail() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity faiWrong = new SimpleAuthenticationIdentity(TestConnection.username, TestConnection.wrongPassword);
    AuthenticationResponse arWrong = fatm.authenticate(faiWrong);
    assertEquals(false, arWrong.isValid());
  }

  private User getMockUserNoReplay(String email, String name, String dn) {
    User user = createMock(User.class);
    expect(user.get_Email()).andReturn(email);
    expect(user.get_Name()).andReturn(name);
    expect(user.get_DistinguishedName()).andReturn(dn);
    return user;
  }

  private User getMockUser(String email, String name, String dn) {
    User user = getMockUserNoReplay(email, name, dn);
    replay(user);
    return user;
  }

  private User getMockUserWithGroup(String email, String name, String dn) {
    Group group = createMock(Group.class);
    expect(group.get_Name()).andReturn(
        "cn=Group1,cn=Groups,dc=example,dc=com");
    User user = getMockUserNoReplay(email, name, dn);
    expect(user.get_MemberOfGroups()).andReturn(
        new GroupSetMock(Collections.singletonList(group)));
    replay(group, user);
    return user;
  }

  private SimpleAuthenticationIdentity getIdentityUser(String identityUser,
      String identityDomain) {
    return new SimpleAuthenticationIdentity(identityUser, null, identityDomain);
  }

  private AuthenticationResponse testGroupLookup(
      AuthenticationIdentity id, User user, boolean expectAuthenticated)
      throws RepositoryException {
    IUserContext uc = createMock(IUserContext.class);
    expect(uc.lookupUser(isA(String.class))).andReturn(user);
    IConnection conn = createMock(IConnection.class);
    expect(conn.getUserContext()).andReturn(uc);
    replay(uc, conn);

    AuthenticationManager fam = getObjectUnderTest(conn);
    AuthenticationResponse authResp = fam.authenticate(id);
    assertNotNull(authResp);
    assertEquals(expectAuthenticated, authResp.isValid());
    verify(uc, conn);

    return authResp;
  }

  @Test
  public void testGroupLookup_matchUserEmail() throws RepositoryException {
    // Expects an exact match of domain in ID with domain in user's email
    testGroupLookup(
        getIdentityUser("jsmith", "example.com"),
        getMockUserWithGroup("jsmith@example.com", "jsmith",
            "cn=jsmith,ou=Users,dc=example,dc=com"),
        true);
  }

  @Test
  public void testGroupLookup_matchUserName() throws RepositoryException {
    // Expects a match of domain in ID with domain in user's name
    testGroupLookup(
        getIdentityUser("jsmith", "example.com"),
        getMockUserWithGroup("jsmith@foo.example.com", "jsmith@example.com",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        true);
  }

  @Test
  public void testGroupLookup_matchUserDn() throws RepositoryException {
    // Expects a match of domain in ID with domain in user's DN
    testGroupLookup(
        getIdentityUser("jsmith", "example.com"),
        getMockUserWithGroup("jsmith@foo.example.com", "jsmith",
            "cn=jsmith,ou=Users,dc=example,dc=com"),
        true);
  }

  @Test
  public void testGroupLookup_matchUserNetbiosName()
      throws RepositoryException {
    // Expects a match of domain in ID with dumb-down domain in user's name
    testGroupLookup(
        getIdentityUser("jsmith", "example"),
        getMockUserWithGroup("jsmith@foo.example.com", "example\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=com"),
        true);
  }

  @Test
  public void testGroupLookup_matchDumbdownUserEmail()
      throws RepositoryException {
    // Expects a match of domain in ID with dumb-down email domain
    testGroupLookup(
        getIdentityUser("jsmith", "example"),
        getMockUserWithGroup("jsmith@example.com", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"), true);
  }

  @Test
  public void testGroupLookup_matchUserDumbdownUserDn()
      throws RepositoryException {
    // Expects a match of domain in ID with dumb-down domain in user's DN
    testGroupLookup(
        getIdentityUser("jsmith", "example"),
        getMockUserWithGroup("jsmith@foo.example.com", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=example,dc=com"),
        true);
  }

  @Test
  public void testGroupLookup_invalidDnsDomain() throws RepositoryException {
    testGroupLookup(
        getIdentityUser("jsmith", "example.com"),
        getMockUser("jsmith@example.com.au", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  @Test
  public void testGroupLookup_invalidNetbiosDomain()
      throws RepositoryException {
    testGroupLookup(
        getIdentityUser("jsmith", "example"),
        getMockUser("jsmith@foo.example.com", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  private Set<String> getPrincipalNames(Collection<?> coll) {
    ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<String>();
    Iterator<?> iterator = coll.iterator();
    while (iterator.hasNext()) {
      Principal group = (Principal) iterator.next();
      builder.add(group.getName());
    }
    return builder.build();
  }

  @Test
  public void testGroupLookup_groups() throws RepositoryException {
    AuthenticationResponse authResp =
        testGroupLookup(
            getIdentityUser("jsmith", "foo.example.com"),
            getMockUserWithGroup("", "",
                "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
            true);
    assertEquals(
        ImmutableSet.of("Group1@example.com", Permissions.AUTHENTICATED_USERS),
        getPrincipalNames(authResp.getGroups()));
  }

  @Test
  public void testAuthenUser_dnsShortWrong() throws RepositoryException {
    testGroupLookup(
        getIdentityUser("jsmith", "example"),
        getMockUserWithGroup("", "",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  @Test
  public void testAuthenUser_noDomain() throws RepositoryException {
    testGroupLookup(
        getIdentityUser("jsmith@foo.example.com", ""),
        getMockUserWithGroup("", "",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        true);
  }

  @Test
  public void testAuthenUser_noDomainDnsUserAussieMockUser()
      throws RepositoryException {
    testGroupLookup(
        getIdentityUser("jsmith@example.com", ""),
        getMockUser("jsmith@example.com.au", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  @Test
  public void testAuthenUser_noDomainWindowsUser() throws RepositoryException {
    testGroupLookup(
        getIdentityUser("foo\\jsmith", ""),
        getMockUserWithGroup("", "",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        true);
  }

  @Test
  public void testAuthenUser_noDomainWrongWindowsUser()
      throws RepositoryException {
    testGroupLookup(
        getIdentityUser("somedomain\\jsmith", ""),
        getMockUserWithGroup("", "",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  @Test
  public void testAuthenUser_domainWindowsUserAussieMockUser()
      throws RepositoryException {
    testGroupLookup(
        getIdentityUser("example.com.au\\jsmith", ""),
        getMockUserWithGroup("jsmith@example.com.au", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        true);
  }

  @Test
  public void testAuthenUser_noDomainWindowsUserAussieMockUser()
      throws RepositoryException {
    testGroupLookup(
        getIdentityUser("example.com\\jsmith", ""),
        getMockUser("jsmith@example.com.au", "foo\\jsmith",
            "cn=jsmith,ou=Users,dc=foo,dc=example,dc=com"),
        false);
  }

  @Test
  public void testGroupLookup_repositoryException() throws RepositoryException {
    IUserContext uc = createMock(IUserContext.class);
    expect(uc.lookupUser(isA(String.class))).andThrow(
        new RepositoryException("User not found"));
    IConnection conn = createMock(IConnection.class);
    expect(conn.getUserContext()).andReturn(uc);
    replay(uc, conn);

    SimpleAuthenticationIdentity id =
        new SimpleAuthenticationIdentity(TestConnection.username);
    AuthenticationManager fam = getObjectUnderTest(conn);

    try {
      AuthenticationResponse authResp = fam.authenticate(id);
    } catch (RepositoryException expected) {
    }
    verify(uc, conn);
  }

  @Test
  public void testGroupLookup_runtimeException() throws RepositoryException {
    IUserContext uc = createMock(IUserContext.class);
    expect(uc.lookupUser(isA(String.class))).andThrow(
        new RuntimeException("User not found"));
    IConnection conn = createMock(IConnection.class);
    expect(conn.getUserContext()).andReturn(uc);
    replay(uc, conn);

    SimpleAuthenticationIdentity id =
        new SimpleAuthenticationIdentity(TestConnection.username);
    AuthenticationManager fam = getObjectUnderTest(conn);

    AuthenticationResponse authResp = fam.authenticate(id);
    assertNotNull(authResp);
    assertEquals(false, authResp.isValid());
    verify(uc, conn);
  }

  @Test
  public void testGroupLookup_liveShortname() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    testGroupLookup_live(TestConnection.username);
  }

  @Test
  public void testGroupLookup_liveShortnameDomain()
      throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    testGroupLookup_live(TestConnection.username + "@" + TestConnection.domain);
  }

  private void testGroupLookup_live(String username)
      throws RepositoryException {
    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity id =
        new SimpleAuthenticationIdentity(TestConnection.username);
    AuthenticationResponse authResp = fatm.authenticate(id);
    assertNotNull(TestConnection.username + " failed authentication", authResp);
    assertTrue("No group is found for " + TestConnection.username,
        authResp.getGroups().size() > 1);
  }

  @Test
  public void testGroupLookup_liveFakeUser() throws RepositoryException {
    assumeTrue(TestConnection.isLiveConnection());

    AuthenticationManager fatm = getObjectUnderTest(true);
    SimpleAuthenticationIdentity id =
        new SimpleAuthenticationIdentity("fake_user");
    AuthenticationResponse authResp = fatm.authenticate(id);
    assertNotNull("Authentication response is not returned", authResp);
    assertEquals(false, authResp.isValid());
  }
}
