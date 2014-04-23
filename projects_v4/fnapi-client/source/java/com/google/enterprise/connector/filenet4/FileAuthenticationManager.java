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

import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IUser;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import java.util.ArrayList;
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
  public AuthenticationResponse authenticate(AuthenticationIdentity id)
      throws RepositoryException {
    IUserContext uc = conn.getUserContext();
    String username = FileUtil.getUserName(id);
    try {
      IUser user = uc.authenticate(username, id.getPassword());
      // Disable ACLs for the 3.2.4 release
      // List<Principal> principalGroups = FileUtil.getPrincipals(
      //     PrincipalType.UNKNOWN, globalNamespace, user.getGroupNames(),
      //     CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
      // return new AuthenticationResponse(true, "", principalGroups);
      return new AuthenticationResponse(true, "");
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Authentication failed for user "
          + username, e);
      return new AuthenticationResponse(false, "");
    }
  }
}
