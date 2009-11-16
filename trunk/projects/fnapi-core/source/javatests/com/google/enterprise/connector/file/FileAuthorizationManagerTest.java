package com.google.enterprise.connector.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.AuthorizationResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FileAuthorizationManagerTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthorizationManager.authorizeDocids(List,
	 * String)'
	 */
	/*public void testAuthorizeDocids() throws RepositoryException {

		FileAuthorizationManager authorizationManager;
		authorizationManager = null;
		Connector connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector)
				.setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector).setIs_public("false");
		Session sess = (FileSession) connector.login();
		authorizationManager = (FileAuthorizationManager) sess
				.getAuthorizationManager();
		
		
		
		{
			String username = "p8Admin";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B488A0F52-9F4A-48A8-8175-C32C6A422C16%7D",
					Boolean.TRUE);

			testAuthorization(authorizationManager, expectedResults, username);
		}
		{
			String username = "p8Admin";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B56042FFC-976E-4F61-8B32-B789218B9324%7D",
					Boolean.TRUE);

			testAuthorization(authorizationManager, expectedResults, username);
		}
		{
			String username = "ebouvier";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B56042FFC-976E-4F61-8B32-B789218B9324%7D",
					Boolean.TRUE);
			testAuthorization(authorizationManager, expectedResults, username);
		}
		{
			String username = "P8TestUser";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B56042FFC-976E-4F61-8B32-B789218B9324%7D",
					Boolean.TRUE);

			testAuthorization(authorizationManager, expectedResults, username);
		}

	}*/

	private void testAuthorization(
			FileAuthorizationManager authorizationManager, Map expectedResults,
			String username) throws RepositoryException {
		List docids = new LinkedList(expectedResults.keySet());

		List resultSet = (List) authorizationManager.authorizeDocids(docids,
				new FileAuthenticationIdentity(username, null));

		Boolean expected;
		AuthorizationResponse authorizationResponse;
		for (Iterator i = resultSet.iterator(); i.hasNext();) {
			authorizationResponse = (AuthorizationResponse) i.next();
			String uuid = authorizationResponse.getDocid();

			expected = (Boolean) expectedResults.get(uuid);
			Assert.assertEquals(username + " access to " + uuid, expected
					.booleanValue(), authorizationResponse.isValid());
		}
	}

	public void testAuthorizeWithVsId() throws RepositoryLoginException,
			RepositoryException {
		{
			FileAuthorizationManager authorizationManager;
			authorizationManager = null;
			Connector connector = new FileConnector();
			((FileConnector) connector).setUsername(FnConnection.userName);
			((FileConnector) connector).setPassword(FnConnection.password);
			((FileConnector) connector)
					.setObject_store(FnConnection.objectStoreName);
			((FileConnector) connector)
					.setWorkplace_display_url(FnConnection.displayUrl);
			((FileConnector) connector)
					.setObject_factory(FnConnection.objectFactory);
			((FileConnector) connector)
					.setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
			((FileConnector) connector).setIs_public("false");
			Session sess = (FileSession) connector.login();
			authorizationManager = (FileAuthorizationManager) sess
					.getAuthorizationManager();
			{
				String username = "jbombonati";

				Map expectedResults = new HashMap();
				expectedResults.put(
						"%7B488A0F52-9F4A-48A8-8175-C32C6A422C16%7D",
						Boolean.FALSE);

				testAuthorization(authorizationManager, expectedResults,
						username);
			}
			{
				String username = "jpasquon";

				Map expectedResults = new HashMap();
				expectedResults.put(
						"%7B488A0F52-9F4A-48A8-8175-C32C6A422C16%7D",
						Boolean.TRUE);

				testAuthorization(authorizationManager, expectedResults,
						username);
			}
			{
				String username = "scauchy";

				Map expectedResults = new HashMap();
				expectedResults.put(
						"%7B488A0F52-9F4A-48A8-8175-C32C6A422C16%7D",
						Boolean.TRUE);

				testAuthorization(authorizationManager, expectedResults,
						username);
			}
		}

	}
}
