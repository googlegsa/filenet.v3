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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.enterprise.connector.filenet4.api.IConnection;
import com.google.enterprise.connector.filenet4.api.IDocument;
import com.google.enterprise.connector.filenet4.api.IObjectFactory;
import com.google.enterprise.connector.filenet4.api.IObjectStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.security.User;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import org.junit.Before;
import org.junit.Test;

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
    FileDocument fd = new FileDocument(new Id(TestConnection.docId1), ios,
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

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.FileDocument.getPropertyNames()'
   */
  @Test
  public void testGetPropertyNames() throws RepositoryException {
    FileDocument fd = new FileDocument(new Id(TestConnection.docId2), ios,
        connec, new SimpleTraversalContext());
    Set<String> propNames = fd.getPropertyNames();
    assertFalse(propNames.isEmpty());
    assertFalse(TestConnection.included_meta.isEmpty());

    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLUSERS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLDENYUSERS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLGROUPS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLDENYGROUPS));
  }

  /** Tests that if pushAcls is false, ACL properties are not advertised. */
  @Test
  public void testGetPropertyNamesPushAclsFalse() throws RepositoryException {
    connec.setPushAcls(false);
    FileDocument fd = new FileDocument(new Id(TestConnection.docId2), ios,
        connec, new SimpleTraversalContext());
    Set<String> propNames = fd.getPropertyNames();
    assertFalse(propNames.isEmpty());
    assertFalse(TestConnection.included_meta.isEmpty());

    assertFalse(propNames.contains(SpiConstants.PROPNAME_ACLUSERS));
    assertFalse(propNames.contains(SpiConstants.PROPNAME_ACLDENYUSERS));
    assertFalse(propNames.contains(SpiConstants.PROPNAME_ACLGROUPS));
    assertFalse(propNames.contains(SpiConstants.PROPNAME_ACLDENYGROUPS));
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
}
