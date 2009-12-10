package com.google.enterprise.connector.fileWhereClause;

import java.util.HashMap;
import java.util.Locale;

import com.google.enterprise.connector.file.FileConnectorType;
import com.google.enterprise.connector.spi.ConfigureResponse;


import junit.framework.TestCase;

public class FileConnectorTypeTest extends TestCase {

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getConfigForm(String)'
	 */
	public void testGetConfigForm() {
		FileConnectorType testConnectorType = new FileConnectorType();

		String[] fields = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		testConnectorType.setConfigKeys(fields);
		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"object_factory\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" />Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td>Additional WHERE clause</td>\r\n<td><input type=\"text\" size=\"50\" name=\"additional_where_clause\" value=\"\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"\"/></td>\r\n</tr>\r\n";

		System.out.println(testConnectorType.getConfigForm(Locale.US).getFormSnippet());
		assertEquals(expectedForm, testConnectorType.getConfigForm(Locale.US)
				.getFormSnippet());
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	public void testValidateConfigWithBlankWhereClause() {
		HashMap map = new HashMap();
		String[] fields = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", "");
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", "");
		map.put("workplace_display_url", FileNETConnection.displayUrl);
		map.put("additional_where_clause", "");//NOT COMPULSORY
		map.put("authentication_type", "");
		FileConnectorType testConnectorType = new FileConnectorType();
		testConnectorType.setConfigKeys(fields);
		ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, null);//ConnectorFactory - 3 PARAM UNUSED
		assertNull(resp);//NULL - IF NO ERROR VALUE FOUND

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	
	public void testValidateConfigWithNonBlankWhereClause() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", "");
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", "");
		map.put("workplace_display_url", FileNETConnection.displayUrl);
		map.put("additional_where_clause",FileNETConnection.additionalWhereClause );
		map.put("authentication_type", "");
		FileConnectorType testConnectorType = new FileConnectorType();
		testConnectorType.setConfigKeys(fiels);
		ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, null);//ConnectorFactory - 3 PARAM UNUSED
		assertNull(resp);//NULL - IF NO ERROR VALUE FOUND

	}

	
	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void testGetPopulatedConfigFormWithNonBlankWhereClause() {
		FileConnectorType testConnectorType = new FileConnectorType();
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		testConnectorType.setConfigKeys(fiels);
		map.put("login", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", FileNETConnection.objectFactory);
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FileNETConnection.pathToWcmApiConfig);
		map.put("workplace_display_url",
				FileNETConnection.displayUrl);
		map.put("is_public", "on");
		map.put("additional_where_clause", FileNETConnection.additionalWhereClause);
		map.put("authentication_type", "API");
		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"FNCE_gdc02\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"Admin1234\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"DemoObjectStore\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"WcmApiConfig.properties\"/></td>\r\n</tr>\r\n<tr>\r\n"
				+ "<td><input type=\"hidden\" name=\"object_factory\" value=\"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"http://gdc02.persistent.co.in:8080//Workplace/getContent\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" CHECKED/>Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td>Additional WHERE clause</td>\r\n<td><input type=\"text\" size=\"50\" name=\"additional_where_clause\" value=\"and Document.This INSUBFOLDER '/testdata'\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"API\"/></td>\r\n</tr>\r\n";
		assertEquals(expectedForm, testConnectorType.getPopulatedConfigForm(map, Locale.US)
				.getFormSnippet());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void testGetPopulatedConfigFormWithBlankWhereClause() {
		FileConnectorType testConnectorType = new FileConnectorType();
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		testConnectorType.setConfigKeys(fiels);
		map.put("login", FileNETConnection.userName);
		map.put("Password", FileNETConnection.password);
		map.put("object_factory", FileNETConnection.objectFactory);
		map.put("object_store", FileNETConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FileNETConnection.pathToWcmApiConfig);
		map.put("workplace_display_url",FileNETConnection.displayUrl);
		map.put("additional_where_clause", "");
		map.put("authentication_type", "API");

		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"FNCE_gdc02\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"Admin1234\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"DemoObjectStore\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"WcmApiConfig.properties\"/></td>\r\n</tr>\r\n<tr>\r\n"
				+ "<td><input type=\"hidden\" name=\"object_factory\" value=\"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"http://gdc02.persistent.co.in:8080//Workplace/getContent\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" />Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td>Additional WHERE clause</td>\r\n<td><input type=\"text\" size=\"50\" name=\"additional_where_clause\" value=\"\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"API\"/></td>\r\n</tr>\r\n";
		assertEquals(expectedForm, testConnectorType.getPopulatedConfigForm(map, Locale.US)
				.getFormSnippet());

	}

}
