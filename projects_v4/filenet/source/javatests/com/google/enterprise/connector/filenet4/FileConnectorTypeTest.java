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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.SearchWrapper;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.util.XmlParseUtil;

import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class FileConnectorTypeTest {
  private HttpServer server;
  private String mockWorkplaceUrl;
  private HashMap<String, String> map;
  private FileConnectorType testConnectorType;

  /** This code is copied from GsaFeedConnectionTest with local changes. */
  @Before
  public void startHttpServer() throws IOException {
    HttpHandler handler = new WorkplaceHandler();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", handler);
    server.start();
    int port = server.getAddress().getPort();
    mockWorkplaceUrl = "http://localhost:" + port;
  }

  @After
  public void stopHttpServer() {
    server.stop(0);
  }

  private static class WorkplaceHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      byte[] response = "hello, world".getBytes(UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      OutputStream body = exchange.getResponseBody();
      body.write(response);
      exchange.close();
    }
  }

  @Before
  public void setUp() {
    map = new HashMap<>();
    map.put("username", TestConnection.adminUsername);
    map.put("Password", TestConnection.adminPassword);
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
    assertEquals("", response.getMessage());
    assertResponseContains("Object Store", "Did not find English", response);
  }

  @Test
  public void testGetConfigForm_cachedLocale() {
    ConfigureResponse response = testConnectorType.getConfigForm(Locale.US);
    assertResponseContains("Object Store", "Did not find English", response);
    assertResponseNotContains("Banque d'objets",
        "Found French instead of English", response);

    response = testConnectorType.getConfigForm(Locale.FRANCE);
    assertEquals("", response.getMessage());
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
    assertEquals("", response.getMessage());
    assertResponseNotContains("<input type=\"hidden\" value=\"Default\" "
        + "name=\"googleGlobalNamespace\"/>", "Found hidden field",
        response);
  }

  @Test
  public void testGetPopulatedConfigForm_obsoleteProperties() {
    ImmutableSet<String> obsoleteProperties = ImmutableSet.of(
        "object_factory", "authentication_type", "path_to_WcmApiConfig");
    for (String property : obsoleteProperties) {
      map.put(property, "somevalue");
    }
    ConfigureResponse response =
        testConnectorType.getPopulatedConfigForm(map, Locale.US);
    assertEquals("", response.getMessage());
    for (String property : obsoleteProperties) {
      assertResponseNotContains(property, "Found obsolete property " + property,
          response);
    }
  }

  /** Tests the Spring-instantiation of the connector from the XML files. */
  @Test
  public void testConnectorFactory() throws RepositoryException {
    FileConnector connector =
        (FileConnector) new FileNetConnectorFactory().makeConnector(map);
    assertEquals(TestConnection.adminUsername, connector.getUsername());
    assertEquals(TestConnection.objectFactory, connector.getObjectFactory());
  }

  /** Sets the object_factory on the connector to MockObjectFactory. */
  /*
   * The alternative implementation is to inject object_factory in
   * connectorInstance.xml.
   */
  private static class MockConnectorFactory extends FileNetConnectorFactory {
    @Override
    public Connector makeConnector(Map<String, String> config)
        throws RepositoryException {
      FileConnector connector =
          (FileConnector) super.makeConnector(config);
      connector.setObject_factory(MockObjectFactory.class.getName());
      return connector;
    }
  }

  /**
   * Returns null or empty nice mocks. This class itself must be
   * public and instantiable from a class name by FileSession.
   */
  public static class MockObjectFactory implements IObjectFactory {
    @Override
    public IConnection getConnection(String contentEngineUri, String userName,
        String userPassword) {
      IConnection connection = createNiceMock(IConnection.class);
      replay(connection);
      return connection;
    }

    @Override
    public IObjectStore getObjectStore(String objectStoreName,
        IConnection connection, String userId, String password) {
      return null;
    }

    @Override
    public PropertyDefinitionList getPropertyDefinitions(
        IObjectStore objectStore, Id objectId, PropertyFilter filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SearchWrapper getSearch(IObjectStore objectStore) {
      SearchWrapper search = createNiceMock(SearchWrapper.class);
      replay(search);
      return search;
    }
  }

  @Test
  public void testValidateConfig() {
    map.put("workplace_display_url", mockWorkplaceUrl);
    ConfigureResponse response = testConnectorType.validateConfig(map,
        Locale.US, new MockConnectorFactory());
    if (response != null) {
      fail(response.getMessage());
    }
  }

  @Test
  public void testValidateConfig_missingRequired() {
    map.remove("workplace_display_url");
    ConfigureResponse response = testConnectorType.validateConfig(map,
        Locale.US, new MockConnectorFactory());
    assertTrue(response.getMessage(),
        response.getMessage().contains("cannot be blank"));
    assertRequiredError("Workplace URL", response);
  }

  @Test
  public void testValidateConfig_stickyMarkedError() {
    map.remove("workplace_display_url");
    ConfigureResponse response = testConnectorType.validateConfig(map,
        Locale.US, new MockConnectorFactory());
    assertTrue(response.getMessage(),
        response.getMessage().contains("cannot be blank"));
    assertRequiredError("Workplace URL", response);

    response = testConnectorType.getPopulatedConfigForm(map, Locale.US);
    assertEquals("", response.getMessage());
    assertResponseNotContains(REQUIRED_ERROR + "Workplace URL",
        "Workplace URL error", response);
  }

  private static final String REQUIRED_ERROR =
      "<div style='float: left;color: red;font-weight: bold'>";

  private void assertRequiredError(String label, ConfigureResponse response) {
    assertResponseContains(REQUIRED_ERROR + label, label + " error", response);
  }

  private void testQuery(String query, String label, String expectedMessage) {
    map.put("workplace_display_url", mockWorkplaceUrl);
    map.put("additional_where_clause", query);
    ConfigureResponse response = testConnectorType.validateConfig(map,
        Locale.US, new MockConnectorFactory());
    if (expectedMessage == null) {
      if (response != null) {
        fail(response.getMessage());
      }
    } else {
      assertNotNull(response);
      assertTrue("Expected:<" + expectedMessage + ">, but got:<"
          + response.getMessage() + ">",
          response.getMessage().contains(expectedMessage));
      assertResponseContains("<span style='color: red'>" + label,
          label + " error", response);
    }
  }

  @Test
  public void testValidateConfig_invalidSelectQuery() {
    testQuery(FileConnectorType.SELECT + " 42 from document where 1=1",
        "Additional Where Clause", "should start with SELECT ID");
  }

  @Test
  public void testValidateConfig_invalidConditionQuery() {
    testQuery(FileConnectorType.QUERYFORMAT + " from document where 1=1",
        "Additional Where Clause", "Query should contain WHERE");
  }

  @Test
  public void testValidateConfig_validQuery() {
    testQuery(FileConnectorType.QUERYFORMAT + " Document "
        + FileConnectorType.VERSIONQUERY + " or 1=1", null, null);
  }

  @Test
  public void testValidateConfig_sameQueries() {
    String query = FileConnectorType.QUERYFORMAT + " Document "
        + FileConnectorType.VERSIONQUERY;
    map.put("delete_additional_where_clause", query);
    testQuery(query, "Additional Delete Clause", "should not be same");
  }

  @Test
  public void testValidateConfig_differentQueries() {
    String query = FileConnectorType.QUERYFORMAT + " Document "
        + FileConnectorType.VERSIONQUERY;
    map.put("delete_additional_where_clause", query + " and 1=1");
    testQuery(query + " and 0=0", null, null);
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
