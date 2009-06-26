package com.google.enterprise.connector.file;

import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileDocumentList;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.file.FileTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FileDocumentListTest extends TestCase {

	FileSession fs;
	FileTraversalManager ftm;
	
	protected void setUp() throws Exception {
		FileConnector connec = new FileConnector();
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);
		
		 fs = (FileSession)connec.login();
		 ftm = (FileTraversalManager) fs.getTraversalManager();
	}

	
	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentList.nextDocument()'
	 */
	public void testNextDocument() {

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileDocumentList.checkpoint()'
	 */
	public void testCheckpoint() throws RepositoryException {

		ftm.setBatchHint(100);
		FileDocumentList set = (FileDocumentList) ftm.startTraversal();
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();

		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
//		assertEquals(TestConnection.checkpoint1, set.checkpoint());
		
//		To Get the Checkpoint of the 4th document, comment the block above 
//		and uncomment the block below.
//		Then add this checkpoint in the TestConnection file as value of checkpoint2
		while (counter <  20) {
			doc = set.nextDocument();
			counter++;
		}
		System.out.println(set.checkpoint());
		
	}

}
