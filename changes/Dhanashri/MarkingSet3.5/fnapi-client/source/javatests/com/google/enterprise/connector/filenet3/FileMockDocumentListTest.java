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
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class FileMockDocumentListTest extends TestCase {

	Connector connector = null;

	Session sess = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector).setObject_store(FnMockConnection.objectStoreName);
		// ((FileConnector)
		// connector).setCredential_tag(FnMockConnection.credTag);
		((FileConnector) connector).setWorkplace_display_url(FnMockConnection.displayUrl);
		((FileConnector) connector).setObject_factory(FnMockConnection.objectFactory);
		((FileConnector) connector).setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() {
		String uuid = "doc2";
		String statement = "";

		try {
			statement = qtm.makeCheckpointQueryString(uuid, "1970-01-01 01:00:00.020", FnConnection.PARAM_UUID);
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
	}
}
