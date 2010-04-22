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

public class FileDocumentListTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	FileDocumentList fdl = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector).setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector).setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector).setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector).setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector).setAdditional_where_clause(FnConnection.additionalWhereClause);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentList.FileDocumentList(Document,
	 * IObjectStore, boolean, String, HashSet, HashSet)'
	 */
	public void testFileDocumentList() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileDocumentList.nextDocument()'
	 */
	public void testNextDocument() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.checkpoint(PropertyMap)'
	 */
	public void testCheckpoint() throws RepositoryException {

		FileDocumentList set = (FileDocumentList) qtm.resumeTraversal(FnConnection.checkpoint);
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(FnConnection.checkpoint, set.checkpoint());
	}

}