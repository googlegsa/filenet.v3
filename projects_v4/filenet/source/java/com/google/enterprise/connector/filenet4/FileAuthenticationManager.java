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

import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IUser;
import com.google.enterprise.connector.filenet4.api.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import com.filenet.api.security.Group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileNet Authentication Manager. It contains method for authenticating the user while search is performed.
 * @author pankaj_chouhan
 * */
public class FileAuthenticationManager implements AuthenticationManager {
  private static final Logger logger =
      Logger.getLogger(FileAuthenticationManager.class.getName());
  
  private final IConnection conn;
  private final String globalNamespace;

  public FileAuthenticationManager(IConnection conn, String namespace) {
    this.conn = conn;
    this.globalNamespace = namespace;
  }

  /**
   * authenticates the user
   * @param authenticationIdentity: contains user credentials
   * */
  @Override
  public AuthenticationResponse authenticate(AuthenticationIdentity id)
      throws RepositoryException {
    IUserContext uc = conn.getUserContext();
    try {
      IUser user = uc.authenticate(id.getUsername(), id.getPassword());
      List<Principal> principalGroups = FileUtil.getPrincipals(
          PrincipalType.UNKNOWN, globalNamespace, getGroupNames(user),
          CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
      principalGroups.add(new Principal(PrincipalType.UNKNOWN, globalNamespace,
          Permissions.AUTHENTICATED_USERS,
          CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE));
      return new AuthenticationResponse(true, "", principalGroups);
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Authentication failed for user " + id, e);
      return new AuthenticationResponse(false, "");
    }
  }

  private Set<String> getGroupNames(IUser user) {
    Set<String> groups = new HashSet<>();
    Iterator<?> iter = user.get_MemberOfGroups().iterator();
    while (iter.hasNext()) {
      Group group = (Group) iter.next();
      groups.add(group.get_Name());
    }
    return groups;
  }
}
