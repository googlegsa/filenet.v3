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

import com.google.common.base.Strings;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import com.filenet.api.security.Group;
import com.filenet.api.security.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileNet Authentication Manager. It contains method for authenticating the user while search is performed.
 */
public class FileAuthenticationManager implements AuthenticationManager {
  private static final Logger logger =
      Logger.getLogger(FileAuthenticationManager.class.getName());

  private final IConnection conn;
  private final String globalNamespace;
  private final boolean returnGroups;

  public FileAuthenticationManager(IConnection conn, String namespace,
        boolean returnGroups) {
    this.conn = conn;
    this.globalNamespace = namespace;
    this.returnGroups = returnGroups;
  }

  /**
   * authenticates the user
   * @param authenticationIdentity: contains user credentials
   */
  @Override
  public AuthenticationResponse authenticate(AuthenticationIdentity id)
      throws RepositoryException {
    IUserContext uc = conn.getUserContext();
    try {
      User user;
      if (Strings.isNullOrEmpty(id.getPassword())) {
        user = uc.lookupUser(id.getUsername());
      } else {
        user = uc.authenticate(id.getUsername(), id.getPassword());
      }
      String identityDomain = getDomain(id);
      if (!Strings.isNullOrEmpty(identityDomain)
          && !matchesDomain(identityDomain, user.get_Email(),
              user.get_Name(), user.get_DistinguishedName())) {
        logger.log(Level.FINE, "GSA identity {0} does not match {1}.",
            new Object[] {id, user});
        return new AuthenticationResponse(false, "");
      }
      if (returnGroups) {
        List<Principal> principalGroups = FileUtil.getPrincipals(
            PrincipalType.UNKNOWN, globalNamespace, getGroupNames(user),
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
        principalGroups.add(new Principal(PrincipalType.UNKNOWN,
            globalNamespace, Permissions.AUTHENTICATED_USERS,
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE));
        return new AuthenticationResponse(true, "", principalGroups);
      } else {
        return new AuthenticationResponse(true, "");
      }
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Authentication failed for user " + id, e);
      return new AuthenticationResponse(false, "");
    }
  }

  /**
   * Returns domain from AuthenticationIdentity.
   */
  private String getDomain(AuthenticationIdentity id) {
    String user = id.getUsername();
    String identityDomain = id.getDomain();
    String domain = null;
    if (Strings.isNullOrEmpty(identityDomain)) {
      if (!Strings.isNullOrEmpty(user)) {
        domain = parseDomain(user);
      }
    } else {
      domain = identityDomain;
    }
    return domain;
  }

  private boolean matchesDomain(String identityDomain, String... names) {
    boolean identityHasFqdn = (identityDomain.indexOf('.') > -1);
    for (String name : names) {
      if (!Strings.isNullOrEmpty(name)) {
        String userDomain = parseDomain(FileUtil.convertDn(name));
        if (identityHasFqdn && (userDomain.indexOf('.') > -1)) {
          if (identityDomain.equalsIgnoreCase(userDomain)) {
            return true;
          }
        } else {
          if (dumbDown(identityDomain).equalsIgnoreCase(dumbDown(userDomain))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Parses the domain from name which is in following formats: username@domain,
   * domain\\username or domain/username.
   *
   * @param string name
   * @return string domain
   */
  private String parseDomain(String name) {
    if (name.indexOf('@') > -1) {
      String[] ary = name.split("@");
      return ary[ary.length - 1];
    } else {
      String[] ary = name.split("\\\\");
      return ary[0];
    }
  }

  private String dumbDown(String domain) {
    int pos = domain.indexOf('.');
    if (pos == -1) {
      return domain;
    } else {
      return domain.substring(0, pos);
    }
  }

  private Set<String> getGroupNames(User user) {
    Set<String> groups = new HashSet<>();
    Iterator<?> iter = user.get_MemberOfGroups().iterator();
    while (iter.hasNext()) {
      Group group = (Group) iter.next();
      groups.add(group.get_Name());
    }
    return groups;
  }
}
