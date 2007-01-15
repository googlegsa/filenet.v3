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
	 * Test method for 'com.google.enterprise.connector.file.FileAuthorizationManager.authorizeDocids(List, String)'
	 */
	public void testAuthorizeDocids() throws RepositoryException{
		
		FileAuthorizationManager authorizationManager;
		authorizationManager = null;
		Connector myconn=new FileConnector();
		
		Session sess = (FileSession) myconn.login();
		authorizationManager = (FileAuthorizationManager)sess.getAuthorizationManager(); 
	{
	      String username = "p8Admin";

	      Map expectedResults = new HashMap();
	      expectedResults.put("{8AE0301C-2F52-46FD-B487-FC7A468A902A}", Boolean.TRUE);
	      expectedResults.put("{BC13F942-5EF5-42BB-B5B2-1E2442C32A1C}", Boolean.TRUE);
	      expectedResults.put("{3D5E1FB4-2670-4C52-B695-02C800112B2A}", Boolean.TRUE);
	      expectedResults.put("{33D6C374-427D-4F96-A0D9-C641E3DECD7F}", Boolean.TRUE);
	     

	      testAuthorization(authorizationManager, expectedResults, username);
	    }

	    {
	      String username = "ebouvier";

	      Map expectedResults = new HashMap();
	      expectedResults.put("{8AE0301C-2F52-46FD-B487-FC7A468A902A}", Boolean.TRUE);
	      expectedResults.put("{BC13F942-5EF5-42BB-B5B2-1E2442C32A1C}", Boolean.TRUE);
	      expectedResults.put("{3D5E1FB4-2670-4C52-B695-02C800112B2A}", Boolean.FALSE);
	      expectedResults.put("{33D6C374-427D-4F96-A0D9-C641E3DECD7F}", Boolean.TRUE);
	      testAuthorization(authorizationManager, expectedResults, username);
	    }
//
//	    {
//	      String username = "Fred";
//
//	      Map expectedResults = new HashMap();
//	      expectedResults.put("{8AE0301C-2F52-46FD-B487-FC7A468A902A}", Boolean.TRUE);
//	      expectedResults.put("{BC13F942-5EF5-42BB-B5B2-1E2442C32A1C}", Boolean.TRUE);
//	      expectedResults.put("{3D5E1FB4-2670-4C52-B695-02C800112B2A}", Boolean.TRUE);
//	      expectedResults.put("{33D6C374-427D-4F96-A0D9-C641E3DECD7F}", Boolean.TRUE);
//
//	      testAuthorization(authorizationManager, expectedResults, username);
//	    }
		
	}

	private void testAuthorization(FileAuthorizationManager authorizationManager, Map expectedResults, String username) throws RepositoryException {
		List docids = new LinkedList(expectedResults.keySet());
		
		ResultSet resultSet =
	        authorizationManager.authorizeDocids(docids, username);
		
		for (Iterator i = resultSet.iterator(); i.hasNext();) {
		      PropertyMap pm = (PropertyMap) i.next();
		      String uuid =
		          pm.getProperty(SpiConstants.PROPNAME_DOCID).getValue().getString();
		      boolean ok =
		          pm.getProperty(SpiConstants.PROPNAME_AUTH_VIEWPERMIT).getValue()
		              .getBoolean();
		      Boolean expected = (Boolean) expectedResults.get(uuid);
		      Assert.assertEquals(username + " access to " + uuid, expected.booleanValue(), ok);
		    }
		  }
	
		
	}



