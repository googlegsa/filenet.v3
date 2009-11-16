package com.google.enterprise.connector.fileWhereClause;

import java.util.HashMap;
import java.util.Locale;
import com.google.enterprise.connector.file.FileConnectorType;
import com.google.enterprise.connector.file.FileNetConnectorFactory;
import com.google.enterprise.connector.spi.ConfigureResponse;


import junit.framework.TestCase;

public class FileConnectorTypeTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	public void testValidateConfigWithBlankWhereClause() {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] fields = { "username", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("username", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", FileNETConnection.objectFactory);
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FileNETConnection.completePathToWcmApiConfig);
		map.put("workplace_display_url", FileNETConnection.displayUrl);
		map.put("additional_where_clause", "");//NOT COMPULSORY
		map.put("authentication_type", "");
		map.put("is_public", "on");
		FileConnectorType testConnectorType = new FileConnectorType();
		testConnectorType.setConfigKeys(fields);
		ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
		assertNull(resp);//NULL - IF NO ERROR VALUE FOUND

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	
	public void testValidateConfigWithIncorrectWhereClause() {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] fields = { "username", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("username", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", FileNETConnection.objectFactory);
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FileNETConnection.completePathToWcmApiConfig);
		map.put("workplace_display_url", FileNETConnection.displayUrl);
		map.put("additional_where_clause", FileNETConnection.wrongAdditionalWhereClause);//NOT COMPULSORY
		map.put("authentication_type", "");
		map.put("is_public", "on");
		FileConnectorType testConnectorType = new FileConnectorType();
		testConnectorType.setConfigKeys(fields);
		ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
		assertEquals(resp.getMessage(), FileNETConnection.error_message);//Compares the error message

	}

	public void testValidateConfigWithCorrectWhereClause() {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] fields = { "username", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("username", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", FileNETConnection.objectFactory);
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FileNETConnection.completePathToWcmApiConfig);
		map.put("workplace_display_url", FileNETConnection.displayUrl);
		map.put("additional_where_clause", FileNETConnection.additionalWhereClause);//NOT COMPULSORY
		map.put("authentication_type", "");
		map.put("is_public", "on");
		FileConnectorType testConnectorType = new FileConnectorType();
		testConnectorType.setConfigKeys(fields);
		ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
		assertNull(resp);//NULL - IF NO ERROR VALUE FOUND
	}
	
}



