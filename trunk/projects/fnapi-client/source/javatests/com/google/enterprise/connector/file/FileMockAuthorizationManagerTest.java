package com.google.enterprise.connector.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.AuthorizationResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FileMockAuthorizationManagerTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthorizationManager.authorizeDocids(List,
	 * String)'
	 */
	public void testAuthorizeDocids() throws RepositoryException {

		Connector connector = new FileConnector();
		connector = new FileConnector();
		((FileConnector) connector).setLogin(FnMockConnection.userName);
		((FileConnector) connector).setPassword(FnMockConnection.password);
		((FileConnector) connector)
				.setObject_store(FnMockConnection.objectStoreName);
		// ((FileConnector)
		// connector).setCredential_tag(FnMockConnection.credTag);
		((FileConnector) connector)
				.setWorkplace_display_url(FnMockConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnMockConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
		((FileConnector) connector).setIs_public("false");
		Session sess = (FileSession) connector.login();
		FileAuthorizationManager authorizationManager = (FileAuthorizationManager) sess
				.getAuthorizationManager();
		assertNotNull(authorizationManager);

		{
			String username = FnMockConnection.FN_LOGIN_OK2;

			Map expectedResults = new HashMap();
			expectedResults.put(FnMockConnection.FN_ID1, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID2, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID3, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID4, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID5, Boolean.FALSE);
			assertNotNull((FileAuthorizationManager) authorizationManager);
			assertNotNull(expectedResults);
			assertNotNull(username);
			testAuthorization((FileAuthorizationManager) authorizationManager,
					expectedResults, username);
		}

		{
			String username = FnMockConnection.FN_LOGIN_OK3;

			Map expectedResults = new HashMap();
			expectedResults.put(FnMockConnection.FN_ID1, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID2, Boolean.FALSE);
			expectedResults.put(FnMockConnection.FN_ID3, Boolean.FALSE);
			expectedResults.put(FnMockConnection.FN_ID4, Boolean.FALSE);
			expectedResults.put(FnMockConnection.FN_ID5, Boolean.FALSE);
			testAuthorization((FileAuthorizationManager) authorizationManager,
					expectedResults, username);
		}

		{
			String username = FnMockConnection.FN_LOGIN_OK1;

			Map expectedResults = new HashMap();
			expectedResults.put(FnMockConnection.FN_ID1, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID2, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID3, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID4, Boolean.TRUE);
			expectedResults.put(FnMockConnection.FN_ID5, Boolean.TRUE);

			testAuthorization((FileAuthorizationManager) authorizationManager,
					expectedResults, username);
		}

	}

	private void testAuthorization(
			FileAuthorizationManager authorizationManager, Map expectedResults,
			String username) throws RepositoryException {
		List docids = new LinkedList(expectedResults.keySet());

		assertNotNull(docids);
		List resultSet = (List) authorizationManager.authorizeDocids(docids,
				new FileAuthenticationIdentity(username, null));
		assertNotNull(resultSet);
		Boolean expected;
		AuthorizationResponse authorizationResponse;
		String uuid;
		for (Iterator i = resultSet.iterator(); i.hasNext();) {
			authorizationResponse = (AuthorizationResponse) i.next();
			assertNotNull(authorizationResponse);
			uuid = authorizationResponse.getDocid();
			assertNotNull(uuid);
			expected = (Boolean) expectedResults.get(uuid);

			Assert.assertEquals(username + " access to " + uuid, expected
					.booleanValue(), authorizationResponse.isValid());
		}
	}

}
