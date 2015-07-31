// Copyright 2007-2011 Google Inc. All Rights Reserved.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.EngineCollectionMocks.PropertyDefinitionListMock;
import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.MarkingSet;
import com.filenet.api.security.User;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FileDocumentTest {
  FileConnector connec;
  FileSession fs;
  IObjectStore ios;
  UserContext uc;
  IObjectFactory iof;
  User adminUser;

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestConnection.isLiveConnection());

    // Clear the statically cached hasMarkings state.
    Field field = FileDocument.class.getDeclaredField("hasMarkings");
    field.setAccessible(true);
    field.set(null, null);

    connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);
    connec.setIncluded_meta(TestConnection.included_meta);
    connec.setExcluded_meta(TestConnection.excluded_meta);
    connec.setIs_public("true");

    fs = (FileSession) connec.login();

    iof = (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
    IConnection conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);

    adminUser = SecurityPrincipalMocks.createAdministratorUser();
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.FileDocument.findProperty(String)'
   */
  @Test
  public void testFindProperty() throws RepositoryException {
    FileDocument fd = new FileDocument(new Id(TestConnection.docId1), iof, ios,
        connec, new SimpleTraversalContext());

    Property prop = fd.findProperty("Id");
    assertEquals(TestConnection.docId1, prop.nextValue().toString());

    // TODO(tdnguyen) Revisit these test cases after fixing Permissions
    // to avoid hard-coding users' names for allow and deny.
    Property allowUsers = fd.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(allowUsers);

    Property denyUsers = fd.findProperty(SpiConstants.PROPNAME_ACLDENYUSERS);
    assertNotNull(denyUsers);

    Property allowGroups = fd.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(allowGroups);

    Property denyGroups = fd.findProperty(SpiConstants.PROPNAME_ACLDENYGROUPS);
    assertNotNull(denyGroups);
  }

  private void testGetPropertyNames(String id, boolean expectAclProperties)
      throws RepositoryException {
    FileDocument fd = new FileDocument(new Id(id), iof, ios, connec,
        new SimpleTraversalContext());
    Set<String> propNames = fd.getPropertyNames();
    assertFalse(propNames.isEmpty());
    assertFalse(TestConnection.included_meta.isEmpty());

    assertEquals(expectAclProperties,
        propNames.contains(SpiConstants.PROPNAME_ACLUSERS));
    assertEquals(expectAclProperties,
        propNames.contains(SpiConstants.PROPNAME_ACLDENYUSERS));
    assertEquals(expectAclProperties,
        propNames.contains(SpiConstants.PROPNAME_ACLGROUPS));
    assertEquals(expectAclProperties,
        propNames.contains(SpiConstants.PROPNAME_ACLDENYGROUPS));
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.FileDocument.getPropertyNames()'
   */
  @Test
  public void testGetPropertyNames() throws RepositoryException {
    testGetPropertyNames(TestConnection.docId2, true);
  }

  /** Tests that if pushAcls is false, ACL properties are not advertised. */
  @Test
  public void testGetPropertyNamesPushAclsFalse() throws RepositoryException {
    connec.setPushAcls(false);
    testGetPropertyNames(TestConnection.docId2, false);
  }

  /**
   * Tests that if checkMarking is off, ACL properties are advertised,
   * even if the document has markings.
   */
  @Test
  public void testGetPropertyNamesCheckMarkingOff() throws RepositoryException {
    connec.setCheck_marking("off");
    testGetPropertyNames(TestConnection.docVsId1, true);
  }

  /**
   * Tests that if checkMarking is on, ACL properties are not advertised
   * if the document has markings.
   */
  @Test
  public void testGetPropertyNamesCheckMarkingOn() throws RepositoryException {
    connec.setCheck_marking("on");
    testGetPropertyNames(TestConnection.docVsId1, false);
  }

  /*
   * Test document permissions such that Administrator user is not in the
   * document ACL but is the creator and owner and the #CREATOR-OWNER ACE must
   * be present in the ACL with AccessLevel.VIEW_AS_INT access or above.
   */
  @Test
  public void testCreatorOwnerPermissions() throws RepositoryException {
    IDocument doc = (IDocument) ios.fetchObject(ClassNames.DOCUMENT,
        new Id(TestConnection.docId4), null);
    List<Value> ownerValue = new ArrayList<Value>();
    doc.getPropertyStringValue("Owner", ownerValue);
    assertEquals(adminUser.get_Name(),
        FileUtil.convertDn(ownerValue.get(0).toString()));
    Permissions perms = new Permissions(doc.get_Permissions());
    assertTrue(perms.authorize(adminUser));
  }

  // The following marking set tests were adapted from those in
  // FileAuthorizationHandlerTest.

  /**
   * @param boolean expectedCheckMarkings the expected return from checkMarkings
   * @param markingSet the marking set on a Document class attribute,
   *     or {@code null} to specify no marking sets
   */
  private void testCheckForMarkings(boolean expectedCheckForMarkings,
      MarkingSet markingSet) throws RepositoryException {
    boolean checkMarkings = connec.checkMarking();

    IObjectFactory factory = createMock(IObjectFactory.class);
    PropertyDefinition otherProperty = createMock(PropertyDefinition.class);
    PropertyDefinitionString stringProperty =
        createMock(PropertyDefinitionString.class);

    if (checkMarkings) {
      // We expect these calls iff we are going to check for marking sets.
      expect(factory.getPropertyDefinitions(isNull(IObjectStore.class),
              isA(Id.class), isNull(PropertyFilter.class)))
          .andReturn(
              new PropertyDefinitionListMock(otherProperty, stringProperty));
      expect(stringProperty.get_MarkingSet()).andReturn(markingSet);
    }
    replay(factory, otherProperty, stringProperty);
    if (markingSet != null) {
      replay(markingSet);
    }

    FileDocument doc = new FileDocument(new Id(TestConnection.docVsId1),
        factory, null, connec, new SimpleTraversalContext());

    assertEquals(expectedCheckForMarkings, doc.checkForMarkings());
  }

  @Test
  public void testCheckForMarkings_on_with() throws RepositoryException {
    connec.setCheck_marking("on");
    testCheckForMarkings(true, createMock(MarkingSet.class));
  }

  @Test
  public void testCheckForMarkings_on_without() throws RepositoryException {
    connec.setCheck_marking("on");
    testCheckForMarkings(false, null);
  }

  @Test
  public void testCheckForMarkings_off_with() throws RepositoryException {
    connec.setCheck_marking("off");
    testCheckForMarkings(false, createMock(MarkingSet.class));
  }

  @Test
  public void testCheckForMarkings_off_without() throws RepositoryException {
    connec.setCheck_marking("off");
    testCheckForMarkings(false, null);
  }

  @Test
  public void testCheckForMarkings_pushAcls_false() throws RepositoryException {
    connec.setPushAcls(false);
    connec.setCheck_marking("on");
    testCheckForMarkings(false, createMock(MarkingSet.class));
  }

  @Test
  public void testCheckForMarkings_on_without_additionalWhereClause_Select()
      throws RepositoryException {
    connec.setAdditional_where_clause("select foo from bar");
    connec.setCheck_marking("on");
    testCheckForMarkings(true, null);
  }

  @Test
  public void testCheckForMarkings_on_with_additionalWhereClause_Select()
      throws RepositoryException {
    connec.setAdditional_where_clause("select foo from bar");
    connec.setCheck_marking("on");
    testCheckForMarkings(true, createMock(MarkingSet.class));
  }

  @Test
  public void testCheckForMarkings_on_without_additionalWhereClause_Other()
      throws RepositoryException {
    connec.setAdditional_where_clause("foo <> bar");
    connec.setCheck_marking("on");
    testCheckForMarkings(false, null);
  }

  @Test
  public void testCheckForMarkings_on_with_additionalWhereClause_Other()
      throws RepositoryException {
    connec.setAdditional_where_clause("foo <> bar");
    connec.setCheck_marking("on");
    testCheckForMarkings(true, createMock(MarkingSet.class));
  }

  @Test
  public void testCheckForMarkings_exception() throws RepositoryException {
    IObjectFactory factory = createMock(IObjectFactory.class);
    expect(factory.getPropertyDefinitions(isNull(IObjectStore.class),
            isA(Id.class), isNull(PropertyFilter.class)))
        .andThrow(new RepositoryException("pretend something bad happened"));
    replay(factory);

    connec.setCheck_marking("on");

    FileDocument doc = new FileDocument(new Id(TestConnection.docId1),
        factory, null, connec, new SimpleTraversalContext());

    assertEquals(true, doc.checkForMarkings());
    verify(factory);
  }
}
