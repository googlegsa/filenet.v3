// Copyright (C) 2007-2011 Google Inc.
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

package com.google.enterprise.connector.filenet4.api;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import com.filenet.api.core.Connection;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;

import javax.security.auth.Subject;

public class FnConnection implements IConnection {
  private final Connection conn;
  private final Subject subject;
  private final String userName;
  private final String userPassword;

  public FnConnection(String contentEngineUri, String userName,
      String userPassword) throws RepositoryException {
    this.conn = Factory.Connection.getConnection(contentEngineUri);
    this.subject = UserContext.createSubject(this.conn, userName,
        userPassword, "FileNetP8");
    this.userName = userName;
    this.userPassword = userPassword;
  }

  Connection getConnection() {
    return conn;
  }

  @Override
  public IUserContext getUserContext() {
    return new FnUserContext(this);
  }

  @Override
  public Subject getSubject() {
    return subject;
  }

  @Override
  public void refreshSUserContext() throws RepositoryLoginException {
    getUserContext().authenticate(userName, userPassword);
  }
}
