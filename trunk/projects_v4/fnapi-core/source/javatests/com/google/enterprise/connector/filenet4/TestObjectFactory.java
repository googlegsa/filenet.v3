// Copyright 2014 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.mock.AccessPermissionListMock;
import com.google.enterprise.connector.filenet4.mock.AccessPermissionMock;
import com.google.enterprise.connector.filenet4.mockjavawrap.MockBaseObject;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class TestObjectFactory {
  private static final DateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  public static IBaseObject newBaseObject(String guid, String timeStr,
      boolean isReleasedVersion, AccessPermissionList perms)
          throws ParseException, RepositoryException {
    Date createdTime = dateFormatter.parse(timeStr);
    return new MockBaseObject(new Id(guid), new Id(guid), createdTime,
        false, isReleasedVersion, perms);
  }

  public static FileConnector newFileConnector() throws Exception {
    FileConnector connector = new FileConnector();
    connector.setUsername(TestConnection.adminUsername);
    connector.setPassword(TestConnection.adminPassword);
    connector.setObject_store(TestConnection.objectStore);
    connector.setWorkplace_display_url(TestConnection.displayURL);
    connector.setObject_factory(TestConnection.objectFactory);
    connector.setContent_engine_url(TestConnection.uri);
    return connector;
  }

  // TODO(tdnguyen): Combine this method with similar methods in
  // PermissionsTest.
  public static AccessPermission newPermission(String granteeName,
      SecurityPrincipalType principalType, AccessType accessType,
      int accessMask, int inheritableDepth, PermissionSource permSrc) {
    AccessPermissionMock perm = new AccessPermissionMock(permSrc);
    perm.set_GranteeType(principalType);
    perm.set_AccessType(accessType);
    perm.set_AccessMask(accessMask);
    perm.set_InheritableDepth(inheritableDepth);
    perm.set_GranteeName(granteeName);
    return perm;
  }

  @SafeVarargs
  public static AccessPermissionList newPermissionList(
      List<AccessPermission>... aceLists) {
    AccessPermissionListMock perms = new AccessPermissionListMock();
    for (List<AccessPermission> aceList : aceLists) {
      perms.addAll(aceList);
    }
    return perms;
  }

  public static List<AccessPermission> generatePermissions(int allowUserCount,
          int denyUserCount, int allowGroupCount, int denyGroupCount,
          int accessMask, int inheritableDepth, PermissionSource permSrc) {
    String prefix = permSrc.toString();
    List<AccessPermission> aces = new ArrayList<AccessPermission>();
    aces.addAll(generatePermissions(prefix + "_allow_user", allowUserCount,
        SecurityPrincipalType.USER, AccessType.ALLOW, accessMask,
        inheritableDepth, permSrc));
    aces.addAll(generatePermissions(prefix + "_deny_user", denyUserCount,
        SecurityPrincipalType.USER, AccessType.DENY, accessMask,
        inheritableDepth, permSrc));
    aces.addAll(generatePermissions(prefix + "_allow_group", allowGroupCount,
        SecurityPrincipalType.GROUP, AccessType.ALLOW, accessMask,
        inheritableDepth, permSrc));
    aces.addAll(generatePermissions(prefix + "_deny_group", denyGroupCount,
        SecurityPrincipalType.GROUP, AccessType.DENY, accessMask,
        inheritableDepth, permSrc));
    return aces;
  }

  public static List<AccessPermission> generatePermissions(
      String granteePrefix, int count, SecurityPrincipalType principalType,
      AccessType accessType, int accessMask, int inheritableDepth,
      PermissionSource permSrc) {
    List<AccessPermission> aces = new ArrayList<AccessPermission>();
    for (int i = 0; i < count; i++) {
      aces.add(TestObjectFactory.newPermission(granteePrefix + "_" + i,
          principalType, accessType, accessMask, inheritableDepth, permSrc));
    }
    return aces;
  }
}
