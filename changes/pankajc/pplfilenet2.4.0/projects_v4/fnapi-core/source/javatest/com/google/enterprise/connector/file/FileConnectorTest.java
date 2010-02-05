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
package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileConnectorTest extends FileNetTestCase {

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileConnector.login()'
	 */
	public void testLogin() throws RepositoryLoginException, RepositoryException {

		FileConnector connec = new FileConnector();
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);

		Session fs = connec.login();
		assertNotNull(fs);
		assertTrue(fs instanceof FileSession);

	}

}
