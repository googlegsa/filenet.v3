// Copyright 2009 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileConnectorTypeTest {
  private HashMap<String, String> map;
  private FileConnectorType testConnectorType;

  @Before
  public void setUp() {
    map = new HashMap<>();
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
    map.put("object_factory", TestConnection.objectFactory);
    map.put("object_store", TestConnection.objectStore);
    map.put("workplace_display_url", TestConnection.displayURL);
    map.put("content_engine_url", TestConnection.uri);
    map.put("additional_where_clause", "");//NOT COMPULSORY
    map.put("delete_additional_where_clause", "");
    map.put("check_marking", "");
    map.put("googleGlobalNamespace", "Default");
    testConnectorType = new FileConnectorType();
  }

  private void assertResponseContains(String expected, String label,
      ConfigureResponse response) {
    assertEquals("", response.getMessage());
    assertEquals(null, response.getConfigData());
    assertTrue(label + ": " + response.getFormSnippet(),
        response.getFormSnippet().contains(expected));
  }

  private void assertResponseNotContains(String expected, String label,
      ConfigureResponse response) {
    assertFalse(label + ": " + response.getFormSnippet(),
        response.getFormSnippet().contains(expected));
    }

  @Test
  public void testGetConfigForm() throws Exception {
    ConfigureResponse response = testConnectorType.getConfigForm(Locale.US);
    System.out.println("testGetConfgForm:\n" + response.getFormSnippet());
    assertEquals("", response.getMessage());
    assertEquals(null, response.getConfigData());
    XmlParseUtil.validateXhtml(response.getFormSnippet());
  }

  @Test
  public void testGetConfigForm_invalidLocale() {
    ConfigureResponse response =
        testConnectorType.getConfigForm(new Locale("klingon"));
    assertResponseContains("Object Store", "Did not find English", response);
  }

  @Test
  public void testGetConfigForm_cachedLocale() {
    ConfigureResponse response = testConnectorType.getConfigForm(Locale.US);
    assertResponseContains("Object Store", "Did not find English", response);
    assertResponseNotContains("Banque d'objets",
        "Found French instead of English", response);

    response = testConnectorType.getConfigForm(Locale.FRANCE);
    assertResponseContains("Banque d'objets", "Did not find French", response);
    assertResponseNotContains("Object Store",
        "Found English instead of French", response);
  }

  @Test
  public void testGetPopulatedConfigForm() throws Exception {
    ConfigureResponse response =
        testConnectorType.getPopulatedConfigForm(map, Locale.US);
    System.out.println("testGetPopulatedConfgForm:\n"
        + response.getFormSnippet());
    assertEquals("", response.getMessage());
    assertEquals(null, response.getConfigData());
    XmlParseUtil.validateXhtml(response.getFormSnippet());
  }

  @Test
  public void testGetPopulatedConfigForm_hidden() {
    ConfigureResponse response =
        testConnectorType.getPopulatedConfigForm(map, Locale.US);
    assertResponseContains("<input type=\"hidden\" value=\"Default\" "
        + "name=\"googleGlobalNamespace\"/>", "Did not find hidden field",
        response);
  }

  @Test
  public void testValidateConfigWithBlankWhereClause() {
    assumeTrue(TestConnection.isLiveConnection());

    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }

  @Test
  public void testValidateConfigIncorrectWhereClause() {
    assumeTrue(TestConnection.isLiveConnection());

    map.put("additional_where_clause", "and Document.this INSUBFOLDER");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertTrue(resp.getMessage(),
        resp.getMessage().indexOf("There was a syntax error") != -1);
  }

  @Test
  public void testValidateConfigCorrectWhereClause() {
    assumeTrue(TestConnection.isLiveConnection());

    map.put("additional_where_clause",
        "and Document.this INSUBFOLDER '/TestFolder'");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }

  @Test
  public void testInvalidWorkplaceURL() throws MalformedURLException {
    assumeTrue(TestConnection.isLiveConnection());

    URL displayUrl = new URL(TestConnection.displayURL);
    map.put("workplace_display_url",
        displayUrl.toString().replace(displayUrl.getPath(), "/xyggy"));
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertEquals(ResourceBundle.getBundle("FileConnectorResources")
        .getString("workplace_url_error"), resp.getMessage());
  }

  @Test
  public void testRepeatedSlashContentEngineURL() {
    assumeTrue(TestConnection.isLiveConnection());

    map.put("content_engine_url", TestConnection.uri + "///////");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }
}
