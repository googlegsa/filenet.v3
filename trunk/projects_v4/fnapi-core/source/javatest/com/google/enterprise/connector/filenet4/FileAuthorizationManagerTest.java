package com.google.enterprise.connector.filenet4;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.google.enterprise.connector.filenet4.FileAuthenticationIdentity;
import com.google.enterprise.connector.filenet4.FileAuthorizationManager;
import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import junit.framework.TestCase;

public class FileAuthorizationManagerTest extends TestCase {

	public void testAuthorizeDocids() throws RepositoryLoginException, RepositoryException {
	
		FileConnector connec = new FileConnector();
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);
		
		FileSession fs = (FileSession)connec.login();
		FileAuthorizationManager fam = (FileAuthorizationManager) fs.getAuthorizationManager();
//		FileAuthenticationManager fatm = (FileAuthenticationManager) fs.getAuthenticationManager();		
//		FileAuthenticationIdentity fai = new FileAuthenticationIdentity(TestConnection.username, TestConnection.password);
//		AuthenticationResponse ar = fatm.authenticate(fai);
		
		Map expectedResults = new HashMap();
		expectedResults.put(TestConnection.docVsId1, Boolean.FALSE);
		expectedResults.put(TestConnection.docVsId2, Boolean.FALSE);
		expectedResults.put(TestConnection.docVsId3, Boolean.TRUE);
		expectedResults.put(TestConnection.docVsId4, Boolean.TRUE);
		
		testAuthorization(fam, expectedResults, TestConnection.username, TestConnection.password);
		
	}
	
	private void testAuthorization(FileAuthorizationManager fam, Map expectedResults, String username, String password) throws RepositoryException {
	
		List docids = new LinkedList(expectedResults.keySet());
		
//		List resultSet = (List) fam.authorizeDocids(docids,
//				new FileAuthenticationIdentity(username, password));
		List resultSet = (List) fam.authorizeDocids(docids,
				new FileAuthenticationIdentity(username, null));
		
		Boolean expected;
		AuthorizationResponse ar;
		for (Iterator i = resultSet.iterator(); i.hasNext();) {
			ar = (AuthorizationResponse) i.next();
			String uuid = ar.getDocid();

			expected = (Boolean) expectedResults.get(uuid);
			assertEquals(username + " access to " + uuid, expected
					.booleanValue(), ar.isValid());

		
		}
	}
}
