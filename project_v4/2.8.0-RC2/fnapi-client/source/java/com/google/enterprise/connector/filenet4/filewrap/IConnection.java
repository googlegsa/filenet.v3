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

package com.google.enterprise.connector.filenet4.filewrap;

import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.core.Connection;

import javax.security.auth.Subject;

/**
 * Interface to set FileNet connection and perform operations with connection.
 */
public interface IConnection {

	public Connection getConnection() throws RepositoryException;

	public IUserContext getUserContext();

	public Subject getSubject();

}