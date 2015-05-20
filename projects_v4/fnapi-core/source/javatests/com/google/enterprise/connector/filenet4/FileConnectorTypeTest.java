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

import com.google.enterprise.connector.spi.ConfigureResponse;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileConnectorTypeTest extends FileNetTestCase {
  private HashMap<String, String> map;

  private FileConnectorType testConnectorType;

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
    String[] fields = map.keySet().toArray(String[0]);
    map.put("googleGlobalNamespace", "Default");
    testConnectorType = new FileConnectorType();
    testConnectorType.setConfigKeys(fields);
  }

  public void testValidateConfigWithBlankWhereClause() {
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }

  public void testValidateConfigIncorrectWhereClause() {
    map.put("additional_where_clause", "and Document.this INSUBFOLDER");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertTrue(resp.getMessage(),
        resp.getMessage().indexOf("There was a syntax error") != -1);
  }

  public void testValidateConfigCorrectWhereClause() {
    map.put("additional_where_clause",
        "and Document.this INSUBFOLDER '/TestFolder'");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }

  public void testInvalidWorkplaceURL() throws MalformedURLException {
    URL displayUrl = new URL(TestConnection.displayURL);
    map.put("workplace_display_url",
        displayUrl.toString().replace(displayUrl.getPath(), "/xyggy"));
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    assertEquals(ResourceBundle.getBundle("FileConnectorResources")
        .getString("workplace_url_error"), resp.getMessage());
  }

  public void testRepeatedSlashContentEngineURL() {
    map.put("content_engine_url", TestConnection.uri + "///////");
    ConfigureResponse resp = testConnectorType.validateConfig(map, Locale.US, new FileNetConnectorFactory());
    if (resp != null) {
      fail(resp.getMessage());
    }
  }
}
