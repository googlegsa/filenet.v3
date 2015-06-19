// Copyright 2015 Google Inc. All Rights Reserved.
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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.enterprise.connector.filenet4.EngineSetMocks.SecurityPolicySetMock;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectSet;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.filenet4.api.ISearch;
import com.google.enterprise.connector.filenet4.api.MockObjectStore;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.security.AccessPermission;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Some JVMs require this class to be public in order for JUnit to
// call newInstance on the subclasses.
public class TraverserFactoryFixture {
  private final List<Object> mocksToVerify = new ArrayList<>();

  protected void replayAndSave(Object... mocks) {
    replay(mocks);
    Collections.addAll(mocksToVerify, mocks);
  }

  /**
   * Verify all saved mocks. It is an error if there are no saved
   * mocks to verify (this method should just not be called), and also
   * an error if there are saved mocks but this method is not called.
   */
  protected void verifyAll() {
    assertFalse("No mocks to verify!", mocksToVerify.isEmpty());
    for (Object mock : mocksToVerify) {
      verify(mock);
    }
    mocksToVerify.clear();
  }

  /** Ensures that all saved mocks have been verified. */
  @After
  public void quisCustodietIpsosCustodes() {
    assertTrue("Unverified mocks: " + mocksToVerify, mocksToVerify.isEmpty());
  }

  private static final String CREATE_TABLE_DELETION_EVENT =
      "create table DeletionEvent("
      + PropertyNames.ID + " varchar, "
      + PropertyNames.DATE_CREATED + " timestamp, "
      + PropertyNames.SOURCE_OBJECT_ID + " varchar, "
      + PropertyNames.VERSION_SERIES_ID + " varchar)";

  private static final String CREATE_TABLE_DOCUMENT =
      "create table Document("
      + PropertyNames.ID + " varchar, "
      + PropertyNames.DATE_LAST_MODIFIED + " timestamp, "
      + PropertyNames.CONTENT_SIZE + " int, "
      + PropertyNames.NAME + " varchar, "
      + PropertyNames.RELEASED_VERSION + " varchar, "
      + PropertyNames.SECURITY_FOLDER + " varchar, "
      + PropertyNames.SECURITY_POLICY + " varchar, "
      + PropertyNames.VERSION_STATUS + " int)";

  private static final String CREATE_TABLE_FOLDER =
      "create table Folder("
      + PropertyNames.ID + " varchar, "
      + PropertyNames.DATE_LAST_MODIFIED + " timestamp)";

  private static final String CREATE_TABLE_SECURITY_POLICY =
      "create table SecurityPolicy("
      + PropertyNames.ID + " varchar, "
      + PropertyNames.DATE_LAST_MODIFIED + " timestamp)";

  protected JdbcFixture jdbcFixture = new JdbcFixture();

  @Before
  public void createTables() throws SQLException {
    jdbcFixture.executeUpdate(CREATE_TABLE_DELETION_EVENT,
        CREATE_TABLE_DOCUMENT, CREATE_TABLE_FOLDER,
        CREATE_TABLE_SECURITY_POLICY);
  }

  @After
  public void tearDown() throws Exception {
    jdbcFixture.tearDown();
  }

  protected FileDocumentTraverser getFileDocumentTraverser(
      FileConnector connector, MockObjectStore os, IObjectSet objectSet,
      Capture<String> capture) throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);

    // The first search result is for added and update documents, and
    // the second and optional third results (both empty) are for
    // deleted documents.
    ISearch searcher = createMock(ISearch.class);
    expect(searcher.execute(capture(capture))).andReturn(objectSet)
        .andReturn(new EmptyObjectSet()).times(1, 2);

    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher);
    replayAndSave(connection, searcher, objectFactory);

    return new FileDocumentTraverser(connection, objectFactory, os, connector);
  }

  protected SecurityFolderTraverser getSecurityFolderTraverser(
      FileConnector connector, FolderSet folderSet)
      throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = new SearchMock(ImmutableMap.of("Folder", folderSet));
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    replayAndSave(connection, os, objectFactory);

    return new SecurityFolderTraverser(connection, objectFactory, os,
        connector);
  }

  protected SecurityPolicyTraverser getSecurityPolicyTraverser(
      FileConnector connector, SecurityPolicySetMock secPolicySet,
      DocumentSet docSet) throws RepositoryException {
    IConnection connection = createNiceMock(IConnection.class);
    IObjectStore os = createNiceMock(IObjectStore.class);
    ISearch searcher = new SearchMock(
        (secPolicySet.isEmpty())
        ? ImmutableMap.of("SecurityPolicy", secPolicySet)
        : ImmutableMap.of("SecurityPolicy", secPolicySet, "Document", docSet));
    IObjectFactory objectFactory = createMock(IObjectFactory.class);
    expect(objectFactory.getSearch(os)).andReturn(searcher).atLeastOnce();
    replayAndSave(connection, os, objectFactory);

    return new SecurityPolicyTraverser(connection, objectFactory, os,
        connector);
  }

  protected AccessPermissionList getPermissions(PermissionSource source) {
    List<AccessPermission> aces = TestObjectFactory.generatePermissions(
        1, 1, 1, 1, (AccessRight.READ_AS_INT | AccessRight.VIEW_CONTENT_AS_INT),
        0, source);
    return TestObjectFactory.newPermissionList(aces);
  }

  /**
   * Smoke tests the queries against H2 but returns mock results.
   */
  protected static class SearchMock implements ISearch {
    /** A map with case-insensitive keys for natural table name matching. */
    private final ImmutableSortedMap<String, IndependentObjectSet> results;

    /** Used by FileDocumentTraverser to just smoke test the queries. */
    protected SearchMock() {
      this.results = ImmutableSortedMap.of();
    }

    /**
     * Constructs a mock to return the given results for each table.
     *
     * @param results a map from table names to the object sets to
     *     return as results for queries against those tables
     */
    protected SearchMock(
        ImmutableMap<String, ? extends IndependentObjectSet> results) {
      this.results = ImmutableSortedMap.<String, IndependentObjectSet>orderedBy(
          String.CASE_INSENSITIVE_ORDER).putAll(results).build();
    }

    @Override
    public IObjectSet execute(String query) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IndependentObjectSet execute(String query, int pageSize,
        int maxRecursion) {
      return executeSql(query);
    }

    public IndependentObjectSet executeSql(String query) {
      // Rewrite queries for H2. Replace GUIDs with table names. Quote
      // timestamps. Rewrite Object(guid) as 'guid'.
      String h2Query = query
          .replace(
              GuidConstants.Class_DeletionEvent.toString(), "DeletionEvent")
          .replace(GuidConstants.Class_Document.toString(), "Document")
          .replace(GuidConstants.Class_Folder.toString(), "Folder")
          .replace(
              GuidConstants.Class_SecurityPolicy.toString(), "SecurityPolicy")
          .replaceAll("([-:0-9]{10}T[-:\\.0-9]{18})", "'$1'")
          .replaceAll("Object\\((\\{[-0-9A-F]{36}\\})\\)", "'$1'");

      // Execute the queries.
      try (Statement stmt = JdbcFixture.getConnection().createStatement();
          ResultSet rs = stmt.executeQuery(h2Query)) {
        // Look up the results to return by table name.
        String tableName = rs.getMetaData().getTableName(1);
        IndependentObjectSet set = results.get(tableName);

        // If we results map is empty, this is a pure smoke test, but
        // otherwise we expect the map to contain an object set to
        // return.
        if (set == null && !results.isEmpty()) {
          fail("Unexpected query for " + tableName + ": " + query);
        }
        return set;
      } catch (SQLException e) {
        // TODO(jlacey): Test this with null arguments.
        throw new EngineRuntimeException(e, null, null);
      }
    }
  }
}
