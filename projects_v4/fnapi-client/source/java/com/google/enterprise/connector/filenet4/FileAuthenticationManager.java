// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

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
  public AuthenticationResponse authenticate(AuthenticationIdentity authenticationIdentity)throws RepositoryException {
    String username = authenticationIdentity.getUsername();
    String password = authenticationIdentity.getPassword();

    IUserContext uc = conn.getUserContext();

    try {
      return authenticate(username, password, uc);
    } catch (Throwable e) {
      logger.log(Level.WARNING,"Authentication Failed for user " + username);
      String shortName = FileUtil.getShortName(username);
      logger.log(Level.INFO,"Trying to authenticate with Short Name: " + shortName);
      try {
        return authenticate(shortName, password, uc);
      } catch (Throwable th) {
        logger.log(Level.WARNING,"Authentication Failed for user " + shortName);
        logger.log(Level.FINE,"While authenticating got exception",th);
        return new AuthenticationResponse(false, "");
      }
    }
  }

  /**
   * Wrapper over authenticate method of UserContext class.
   * @param username UserName which needs to be authenticated
   * @param password Valid password of the user name
   * @param uc     UserContext reference
   * @return       Returns the Authentication response
   * @throws RepositoryException
   */
  private AuthenticationResponse authenticate(String username, String password, IUserContext uc) throws RepositoryException {
    uc.authenticate(username, password);
    logger.info("Authentication Succeeded for user " + username);
    return new AuthenticationResponse(true, "");
  }
}
