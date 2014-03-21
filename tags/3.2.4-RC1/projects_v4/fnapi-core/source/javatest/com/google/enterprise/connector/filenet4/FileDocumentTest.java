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

import com.google.enterprise.connector.filenet4.filejavawrap.FnPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IUser;
import com.google.enterprise.connector.filenet4.mock.MockUtil;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;

import java.util.Iterator;
import java.util.Set;

public class FileDocumentTest extends FileNetTestCase {

  FileSession fs;
  IObjectStore ios;
  IConnection conn;
  UserContext uc;
  IObjectFactory iof;
  IUser adminUser;

  protected void setUp() throws Exception {

    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession) connec.login();

    iof = (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
    IConnection conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);

    adminUser = MockUtil.createAdministratorUser();
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.FileDocument.findProperty(String)'
   */
  public void testFindProperty() throws RepositoryException {

    FileDocument fd = new FileDocument(TestConnection.docId1, null, ios,
            false, TestConnection.displayURL, TestConnection.included_meta,
            TestConnection.excluded_meta, ActionType.ADD);

    Property prop = fd.findProperty("Id");
    assertEquals(TestConnection.docId1, prop.nextValue().toString());

    // TODO(tdnguyen) Revisit these test cases after fixing FnPermissions
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
  public void testGetPropertyNames() throws RepositoryException {

    FileDocument fd = new FileDocument(TestConnection.docId2, null, ios,
            false, TestConnection.displayURL, TestConnection.included_meta,
            TestConnection.excluded_meta, ActionType.ADD);
    Iterator<String> properties = fd.getPropertyNames().iterator();

    int counter = 0;
    while (properties.hasNext()) {
      properties.next();
      counter++;
    }
    assertTrue(counter > 0);
    assertTrue(TestConnection.included_meta.size() > 0);

    Set<String> propNames = fd.getPropertyNames();
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLUSERS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLDENYUSERS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLGROUPS));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACLDENYGROUPS));
  }

  /*
   * Test document permissions such that Administrator user is not in the
   * document ACL but is the creator and owner and the #CREATOR-OWNER ACE must
   * be present in the ACL with AccessLevel.VIEW_AS_INT access or above.
   */
  public void testCreatorOwnerPermissions() throws RepositoryException {
    Document doc = Factory.Document.fetchInstance(ios.getObjectStore(),
        TestConnection.docId4, null);
    FnPermissions perms = new FnPermissions(doc.get_Permissions(),
        doc.get_Owner());
    assertEquals(doc.get_Owner(), adminUser.getName());
    assertTrue(perms.authorize(adminUser));

    FnPermissions perms2 = new FnPermissions(doc.get_Permissions(), null);
    assertFalse(perms2.authorize(adminUser));
  }
}
