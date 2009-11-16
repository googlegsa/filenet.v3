package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.FileTraversalManager;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class FileTraversalManagerTest extends TestCase {

	FileSession fs;
	
	FileTraversalManager ftm;
	String checkpoint;
	
	protected void setUp() throws RepositoryException {
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
	 * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {

		ftm.setBatchHint(200);
		DocumentList set = this.ftm.startTraversal();
		long counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			if(counter == 113){
				checkpoint = set.checkpoint();
				System.out.println(checkpoint);
			}
				doc = set.nextDocument();
				counter++;
		}
		assertEquals(200, counter);
		
		
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {

		ftm.setBatchHint(200);
		DocumentList set = this.ftm.resumeTraversal(TestConnection.checkpoint2);
		assertNotNull(set);
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(200, counter);

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.FileTraversalManager.setBatchHint(int)'
	 */
	public void testSetBatchHint() throws RepositoryException {
		this.ftm.setBatchHint(10);
		DocumentList set = this.ftm.startTraversal();
		int counter = 0;
		while (set.nextDocument() != null) {
			counter++;
		}
		assertEquals(10, counter);

	}

}
