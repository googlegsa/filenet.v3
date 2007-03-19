package com.google.enterprise.connector.file;

import java.util.HashMap;

import com.google.enterprise.connector.file.FileConnectorType;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

public class FileConnectorTypeTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.setConfigKeys(List)'
	 */
	public void testSetConfigKeysList() {

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getConfigForm(String)'
	 */
	public void testGetConfigForm() {
		FileConnectorType test = new FileConnectorType();
		
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		test.setConfigKeys(fiels);
		test.getConfigForm("en").getFormSnippet();

	}
	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	public void testValidateConfig() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", FnConnection.userName);
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.objectStoreName);
		map.put("pathToWcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("displayUrl", FnConnection.displayUrl);
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertNull(resp.getMessage());
		

	}
	
	public void testValidateConfigWithWrongObjectStoreName() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", FnConnection.userName);
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.wrongObjectStoreName);
		map.put("pathToWcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("displayUrl", FnConnection.displayUrl);
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertTrue(resp.getMessage().startsWith("Some required configuration is missing: Please check the object Store name."));
	}

	
	public void testValidateConfigWithWrongusername() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", "wrong");
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.objectStoreName);
		map.put("pathToWcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("displayUrl", FnConnection.displayUrl);
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertTrue(resp.getMessage().startsWith("Some required configuration is missing: Please, check the credentials."));
	}
	
	public void testValidateConfigWithWrongPathToWcmApiConfig() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", FnConnection.userName);
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.objectStoreName);
		map.put("pathToWcmApiConfig", "path");
		map.put("displayUrl", FnConnection.displayUrl);
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertTrue(resp.getMessage().startsWith("Some required configuration is missing: The wcmapiconfig file has not been found on the server. Please, check the path to this file."));		
	}
	
	
	
	public void testValidateConfigWithWrongServerOfWorkplaceUrl() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", FnConnection.userName);
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.objectStoreName);
		map.put("pathToWcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("displayUrl", "http://swp-vm-fnet35:8080/Workplace/");
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertTrue(resp.getMessage().startsWith("Some required configuration is missing: Please check the workplace server url."));		
	}
	
	
	public void testValidateConfigWithWrongWorkplaceUrl() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "password", "appId", "credTag",
				"objectStoreName", "pathToWcmApiConfig", "objectFactory","displayUrl","isPublic" };
		map.put("login", FnConnection.userName);
		map.put("password", FnConnection.password);
		map.put("appId", FnConnection.appId);
		map
				.put("objectFactory",
						FnConnection.objectFactory);
		map.put("credTag", FnConnection.credTag);
		map.put("objectStoreName", FnConnection.objectStoreName);
		map.put("pathToWcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("displayUrl", "http://swp-vm-fnet352:8080/Workplac/");
		map.put("isPublic", "false");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, "en");
		assertTrue(resp.getMessage().startsWith("Some required configuration is missing: Please check the workplace server url."));		
	}
	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void testGetPopulatedConfigForm() {

	}

}
