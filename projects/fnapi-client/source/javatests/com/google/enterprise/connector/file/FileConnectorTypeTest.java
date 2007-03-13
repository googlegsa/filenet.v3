package com.google.enterprise.connector.file;

import java.util.HashMap;

import com.google.enterprise.connector.file.FileConnectorType;

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
		String[] fiels = { "login", "password", "docbase", "clientX",
				"authenticationType", "webtopServerUrl" };
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
		map.put("login", "queryUser");
		map.put("password", "p@ssw0rd");
		map.put("docbase", "gsafile");
		map
				.put("objectFactory",
						"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory");
		map.put("authenticationType", "api");
		map.put("webtopServerUrl", "http://swp-srv-vmgsa:8080/webtop/");
		map.put("additionalWhereClause", "and owner_name != 'Administrator'");
		FileConnectorType test = new FileConnectorType();
		String[] fiels = { "login", "password", "docbase", "clientX",
				"authenticationType", "webtopServerUrl",
				"additionalWhereClause" };
		test.setConfigKeys(fiels);
		test.validateConfig(map, "en").getFormSnippet();

		// Map map = new Map();

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void testGetPopulatedConfigForm() {

	}

}
