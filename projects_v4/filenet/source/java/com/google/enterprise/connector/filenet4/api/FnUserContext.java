// Copyright 2008 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.api;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.core.Factory;
import com.filenet.api.security.User;
import com.filenet.api.util.UserContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

/**
 * FileNet user context
 */
public class FnUserContext implements IUserContext {
  private static final Logger logger =
      Logger.getLogger(FnUserContext.class.getName());

  private final IConnection conn;

  public FnUserContext(IConnection conn) {
    this.conn = conn;
  }

  @Override
  public String getName() throws RepositoryException {
    User user = Factory.User.fetchCurrent(
        ((FnConnection) conn).getConnection(), null);
    logger.config("User name from connection: " + user.get_Name());
    return user.get_Name();
  }

  @Override
  public User authenticate(String username, String password)
          throws RepositoryLoginException {
    if (password == null) {
      throw new RepositoryLoginException("Password is null");
    }

    UserContext uc = UserContext.get();
    try {
      Subject s = UserContext.createSubject(
          ((FnConnection) conn).getConnection(), username, password,
          "FileNetP8");
      uc.pushSubject(s);
      User u = Factory.User.fetchCurrent(((FnConnection) conn).getConnection(),
          null);
      logger.info("User: " + u.get_Name() + " is authenticated");
      return u;
    } catch (Throwable e) {
      logger.log(Level.WARNING,
          "Unable to GET connection or user is not authenticated");
      throw new RepositoryLoginException(e);
    }
  }

  /*
   * TODO(tdnguyen) This seems to be a strange place to put lookupUser method
   * here.  Consider moving or refactoring this method.
   *
   * TODO(jlacey): r569 moved this code here from FnPermissions, but
   * left behind a now unused PropertyFilter. We should use this
   * filter here, or just delete this TODO.
   *
   * private final PropertyFilter pf;
   * pf = new PropertyFilter();
   * pf.addIncludeProperty(new FilterElement(null, null, null,
   *     PropertyNames.EMAIL, null));
   * pf.addIncludeProperty(new FilterElement(null, null, null,
   *     PropertyNames.SHORT_NAME, null));
   * pf.addIncludeProperty(new FilterElement(null, null, null,
   *     PropertyNames.NAME, null));
   * pf.addIncludeProperty(new FilterElement(null, null, null,
   *     PropertyNames.DISTINGUISHED_NAME, null));
   */
  @Override
  public User lookupUser(String username) throws RepositoryException {
    try {
      logger.log(Level.FINE, "Lookup user: {0}", username);
      User user = Factory.User.fetchInstance(
          ((FnConnection) conn).getConnection(), username, null);
      return user;
    } catch (Exception e) {
      throw new RepositoryException(username + " username is not found", e);
    }
  }
}
