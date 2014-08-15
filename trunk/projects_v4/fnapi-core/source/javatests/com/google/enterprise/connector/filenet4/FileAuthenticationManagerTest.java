// Copyright 2007-2010 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.filenet4.FileAuthenticationManager;
import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

public class FileAuthenticationManagerTest extends FileNetTestCase {
  /*
   * Test method for 'com.google.enterprise.connector.file.FileAuthenticationManager.authenticate(AuthenticationIdentity)'
   */
  public void testAuthenticate() throws RepositoryLoginException, RepositoryException  {
    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    FileSession fs = (FileSession) connec.login();
    FileAuthenticationManager fatm = (FileAuthenticationManager) fs.getAuthenticationManager();

    //    Check FileAuthenticationManager
    SimpleAuthenticationIdentity fai = new SimpleAuthenticationIdentity(
        TestConnection.username, TestConnection.password);
    AuthenticationResponse ar = fatm.authenticate(fai);
    assertEquals(true, ar.isValid());
    assertTrue(ar.getGroups().size() > 0);

    //    Check FileAuthenticationManager for a wrong user
    SimpleAuthenticationIdentity faiWrong = new SimpleAuthenticationIdentity(TestConnection.username, TestConnection.wrongPassword);
    AuthenticationResponse arWrong = fatm.authenticate(faiWrong);
    assertEquals(false, arWrong.isValid());
  }
}
