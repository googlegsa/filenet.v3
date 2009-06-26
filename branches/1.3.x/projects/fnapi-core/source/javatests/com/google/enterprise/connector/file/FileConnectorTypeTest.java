package com.google.enterprise.connector.file;

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
		FileConnectorType test = new FileConnectorType();

		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		test.setConfigKeys(fiels);
		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"object_factory\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" />Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" name=\"additional_where_clause\" value=\"\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"\"/></td>\r\n</tr>\r\n";
		String expectedForm1 = "<tr>\r\n<td style='white-space: nowrap'>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td style='white-space: nowrap'>Password</td>\r\n<td><input type=\"password\" name=\"Password\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td style='white-space: nowrap'>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"\"/></td>\r\n</tr>\r\n<tr style='display: none'>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"\"/></td>\r\n</tr>\r\n<tr style='display: none'>\r\n<td><input type=\"hidden\" name=\"object_factory\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td style='white-space: nowrap'>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"\"/></td>\r\n</tr>\r\n<tr>\r\n<td colspan='2'><input type=\"checkbox\" name=\"is_public\" />Make public</td>\r\n</tr>\r\n<tr style='display: none'>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n<tr>\r\n<td style='white-space: nowrap'>Additional Where Clause</td>\r\n<td><input type=\"text\" size=\"50\" name=\"additional_where_clause\" value=\"\"/></td>\r\n</tr>\r\n<tr style='display: none'>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"\"/></td>\r\n</tr>\r\n";

		System.out.println(test.getConfigForm(Locale.US).getFormSnippet());
		assertEquals(expectedForm1, test.getConfigForm(Locale.US)
				.getFormSnippet());
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.validateConfig(Map,
	 * String)'
	 */
	public void estValidateConfig() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", "");
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", "");
		map.put("workplace_display_url", FnConnection.displayUrl);
		map.put("additional_where_clause", "");
		map.put("authentication_type", "");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, Locale.US, null);
		String initialConfigForm = resp.getFormSnippet();
		System.out.println(initialConfigForm);
		assertNull(resp);

	}

	public void estValidateConfigWithWrongObjectStoreName() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.wrongObjectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("workplace_display_url", FnConnection.displayUrl);
		map.put("is_public", "false");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, Locale.US, null);
		assertTrue(resp
				.getMessage()
				.startsWith(
						"<p><font color=\"#FF0000\">Could not connect to the remote server. Please check the Object Store name."));
	}

	public void estValidateConfigWithWrongusername() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", "wrong");
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("workplace_display_url", FnConnection.displayUrl);
		map.put("is_public", "false");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, Locale.US, null);

		assertTrue(resp
				.getMessage()
				.startsWith(
						"<p><font color=\"#FF0000\">Invalid credentials. Please enter correct credentials."));
	}

	public void estValidateConfigWithWrongServerOfWorkplaceUrl() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map
				.put("workplace_display_url",
						"http://swp-vm-fnet35:8080/Workplace/");
		map.put("is_public", "false");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, Locale.US, null);

		assertTrue(resp
				.getMessage()
				.startsWith(
						"<p><font color=\"#FF0000\">Could not connect to the Workplace. Please check the Display URL value"));
	}

	public void estValidateConfigWithWrongWorkplaceUrl() {
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map
				.put("workplace_display_url",
						"http://xyz.co.in:8080/Workplac/");
		map.put("is_public", "false");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");
		FileConnectorType test = new FileConnectorType();
		test.setConfigKeys(fiels);
		ConfigureResponse resp = test.validateConfig(map, Locale.US, null);
		assertTrue(resp
				.getMessage()
				.startsWith(
						"<p><font color=\"#FF0000\">Could not connect to the Workplace. Please check the Display URL value"));
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void estGetPopulatedConfigForm() {
		FileConnectorType test = new FileConnectorType();
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		test.setConfigKeys(fiels);
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("workplace_display_url",
				"http://xyz.co.in:8080/Workplace/");
		map.put("is_public", "on");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");

		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"xyz\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"xyz\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"FilenetObjectStore\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"WcmApiConfig.properties\"/></td>\r\n</tr>\r\n<tr>\r\n"
				+ "<td><input type=\"hidden\" name=\"object_factory\" value=\"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"http://xyz.co.in:8080/Workplace/\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" CHECKED/>Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" name=\"additional_where_clause\" value=\"and Document.This INSUBFOLDER '/testdata'\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"API\"/></td>\r\n</tr>\r\n";
		assertEquals(expectedForm, test.getPopulatedConfigForm(map, Locale.US)
				.getFormSnippet());

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileConnectorType.getPopulatedConfigForm(Map,
	 * String)'
	 */
	public void estGetPopulatedConfigFormWihtoutIsPublic() {
		FileConnectorType test = new FileConnectorType();
		HashMap map = new HashMap();
		String[] fiels = { "login", "Password", "object_store",
				"path_to_WcmApiConfig", "object_factory",
				"workplace_display_url", "is_public",
				"additional_where_clause", "authentication_type" };
		test.setConfigKeys(fiels);
		map.put("login", FnConnection.userName);
		map.put("Password", FnConnection.password);
		map.put("object_factory", FnConnection.objectFactory);
		map.put("object_store", FnConnection.objectStoreName);
		map.put("path_to_WcmApiConfig", FnConnection.pathToWcmApiConfig);
		map.put("workplace_display_url",
				"http://xyz.co.in:8080/Workplace/");
		map.put("additional_where_clause", FnConnection.additionalWhereClause);
		map.put("authentication_type", "API");

		String expectedForm = "<tr>\r\n"
				+ "<td>Username</td>\r\n<td><input type=\"text\" size=\"50\" name=\"login\" value=\"xyz\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td>Password</td>\r\n<td><input type=\"Password\" name=\"Password\" value=\"xyz\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Object store</td>\r\n<td><input type=\"text\" size=\"50\" name=\"object_store\" value=\"FilenetObjectStore\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=\"hidden\" name=\"path_to_WcmApiConfig\" value=\"WcmApiConfig.properties\"/></td>\r\n</tr>\r\n<tr>\r\n"
				+ "<td><input type=\"hidden\" name=\"object_factory\" value=\"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td>Workplace URL</td>\r\n<td><input type=\"text\" size=\"50\" name=\"workplace_display_url\" value=\"http://xyz.co.in:8080/Workplace/\"/></td>"
				+ "\r\n</tr>\r\n<tr>\r\n<td><input type=CHECKBOX name=\"is_public\" />Make public</td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" value=\"false\" name=\"is_public\"/></td>\r\n</tr>\r\n"
				+ "<tr>\r\n<td><input type=\"hidden\" name=\"additional_where_clause\" value=\"and Document.This INSUBFOLDER '/testdata'\"/></td>\r\n</tr>"
				+ "\r\n<tr>\r\n<td><input type=\"hidden\" name=\"authentication_type\" value=\"API\"/></td>\r\n</tr>\r\n";
		assertEquals(expectedForm, test.getPopulatedConfigForm(map, Locale.US)
				.getFormSnippet());

	}

}
