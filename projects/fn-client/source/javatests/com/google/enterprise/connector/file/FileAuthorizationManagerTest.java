package com.google.enterprise.connector.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FileAuthorizationManagerTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileAuthorizationManager.authorizeDocids(List,
	 * String)'
	 */
	public void testAuthorizeDocids() throws RepositoryException {

		FileAuthorizationManager authorizationManager;
		authorizationManager = null;
		Connector connector = new FileConnector();
		((FileConnector) connector).setLogin(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector)
				.setObjectStoreName(FnConnection.objectStoreName);
		((FileConnector) connector).setAppId(FnConnection.appId);
		((FileConnector) connector).setCredTag(FnConnection.credTag);
		((FileConnector) connector).setDisplayUrl(FnConnection.displayUrl);
		((FileConnector) connector)
				.setObjectFactory(FnConnection.objectFactory);
		((FileConnector) connector)
				.setPathToWcmApiConfig(FnConnection.pathToWcmApiConfig);
		Session sess = (FileSession) connector.login();
		authorizationManager = (FileAuthorizationManager) sess
				.getAuthorizationManager();
		{
			String username = "p8Admin";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B8AE0301C-2F52-46FD-B487-FC7A468A902A%7D",
					Boolean.TRUE);
			expectedResults.put("%7BBC13F942-5EF5-42BB-B5B2-1E2442C32A1C%7D",
					Boolean.TRUE);
			expectedResults.put("%7B3D5E1FB4-2670-4C52-B695-02C800112B2A%7D",
					Boolean.TRUE);
			expectedResults.put("%7B33D6C374-427D-4F96-A0D9-C641E3DECD7F%7D",
					Boolean.TRUE);

			testAuthorization(authorizationManager, expectedResults, username);
		}

		{
			String username = "ebouvier";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B8AE0301C-2F52-46FD-B487-FC7A468A902A%7D",
					Boolean.TRUE);
			expectedResults.put("%7BBC13F942-5EF5-42BB-B5B2-1E2442C32A1C%7D",
					Boolean.TRUE);
			expectedResults.put("%7B3D5E1FB4-2670-4C52-B695-02C800112B2A%7D",
					Boolean.FALSE);
			expectedResults.put("%7B33D6C374-427D-4F96-A0D9-C641E3DECD7F%7D",
					Boolean.TRUE);
			testAuthorization(authorizationManager, expectedResults, username);
		}

		{
			String username = "P8TestUser";

			Map expectedResults = new HashMap();
			expectedResults.put("%7B8AE0301C-2F52-46FD-B487-FC7A468A902A%7D",
					Boolean.TRUE);
			expectedResults.put("%7BBC13F942-5EF5-42BB-B5B2-1E2442C32A1C%7D",
					Boolean.TRUE);
			expectedResults.put("%7B3D5E1FB4-2670-4C52-B695-02C800112B2A%7D",
					Boolean.TRUE);
			expectedResults.put("%7B33D6C374-427D-4F96-A0D9-C641E3DECD7F%7D",
					Boolean.TRUE);

			testAuthorization(authorizationManager, expectedResults, username);
		}

	}

	private void testAuthorization(
			FileAuthorizationManager authorizationManager, Map expectedResults,
			String username) throws RepositoryException {
		List docids = new LinkedList(expectedResults.keySet());

		ResultSet resultSet = authorizationManager.authorizeDocids(docids,
				username);

		for (Iterator i = resultSet.iterator(); i.hasNext();) {
			PropertyMap pm = (PropertyMap) i.next();
			String uuid = pm.getProperty(SpiConstants.PROPNAME_DOCID)
					.getValue().getString();
			boolean ok = pm.getProperty(SpiConstants.PROPNAME_AUTH_VIEWPERMIT)
					.getValue().getBoolean();
			Boolean expected = new Boolean(false);
			expected = (Boolean) expectedResults.get(uuid);

			Assert.assertEquals(username + " access to " + uuid, expected
					.booleanValue(), ok);
		}
	}

}
