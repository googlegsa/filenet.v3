// Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.api;

import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Connection;
import com.filenet.api.meta.ClassDescription;
import com.filenet.api.property.Properties;
import com.filenet.api.security.AccessPermission;

public class AccessPermissionMock implements AccessPermission {
  private static final long serialVersionUID = 1L;

  private final PermissionSource permSource;
  private AccessType accessType;
  private String granteeName;
  private SecurityPrincipalType secPrincipalType;
  private Integer inheritableDepth = 1;
  private Integer accessMask;

  public AccessPermissionMock(PermissionSource permissionSrc) {
    this.permSource = permissionSrc;
  }

  @Override
  public AccessType get_AccessType() {
    return this.accessType;
  }

  @Override
  public void set_AccessType(AccessType accessType) {
    this.accessType = accessType;
  }

  @Override
  public String get_GranteeName() {
    return this.granteeName;
  }

  @Override
  public SecurityPrincipalType get_GranteeType() {
    return this.secPrincipalType;
  }

  @Override
  public Integer get_InheritableDepth() {
    return this.inheritableDepth;
  }

  @Override
  public PermissionSource get_PermissionSource() {
    return this.permSource;
  }

  @Override
  public void set_GranteeName(String arg0) {
    this.granteeName = arg0;
  }

  public void set_GranteeType(SecurityPrincipalType secPrincipalType) {
    this.secPrincipalType = secPrincipalType;
  }

  @Override
  public void set_InheritableDepth(Integer arg0) {
    this.inheritableDepth = arg0;
  }

  @Override
  public String getClassName() {
    return AccessPermissionMock.class.getName();
  }

  @Override
  public Connection getConnection() {
    return null;
  }

  @Override
  public Properties getProperties() {
    return null;
  }

  @Override
  public String[] getSuperClasses() {
    return null;
  }

  @Override
  public ClassDescription get_ClassDescription() {
    return null;
  }

  @Override
  public Integer get_AccessMask() {
    return this.accessMask;
  }

  @Override
  public void set_AccessMask(Integer arg0) {
    this.accessMask = arg0;
  }

}
