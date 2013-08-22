// Copyright (C) 2007-2010 Google Inc.
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
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileNet Authentication Manager. It contains method for authenticating the user while search is performed.
 * @author pankaj_chouhan
 * */
public class FileAuthenticationManager implements AuthenticationManager {

  IConnection conn;

  private static Logger logger = null;
  static {
    logger = Logger.getLogger(FileAuthenticationManager.class.getName());
  }

  public FileAuthenticationManager(IConnection conn) {
    this.conn = conn;
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
      return new AuthenticationResponse(true, "", user.getGroupNames());
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Authentication failed for user "
          + username, e);
      return new AuthenticationResponse(false, "");
    }
  }
}
