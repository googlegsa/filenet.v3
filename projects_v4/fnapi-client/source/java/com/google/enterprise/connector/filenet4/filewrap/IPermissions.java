// Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.filenet4.filewrap;

import com.filenet.api.constants.PermissionSource;

import java.util.Set;

/**
 * This interface is responsible to authorize a target user against all the
 * Access Control Entries of a target document for ACL Security policies, and
 * Marking set USE rights.
 *
 * @author Dhanashri_Deshpande
 */
public interface IPermissions {

  boolean authorize(IUser user);

  boolean authorizeMarking(IUser user, Integer constraintMask);

  Set<String> getAllowUsers();
  Set<String> getAllowUsers(PermissionSource permSrc);

  Set<String> getDenyUsers();
  Set<String> getDenyUsers(PermissionSource permSrc);

  Set<String> getAllowGroups();
  Set<String> getAllowGroups(PermissionSource permSrc);

  Set<String> getDenyGroups();
  Set<String> getDenyGroups(PermissionSource permSrc);
}
