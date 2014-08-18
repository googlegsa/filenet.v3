// Copyright 2014 Google Inc.  All Rights Reserved.
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

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.Value;

import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class AclDocument implements Document {
  private static final Logger LOGGER =
      Logger.getLogger(AclDocument.class.getName());

  public static final String SEC_POLICY_POSTFIX = "-TMPL";
  public static final String SEC_FOLDER_POSTFIX = "-FLDR";

  private static final ImmutableSet<String> propNames = ImmutableSet.of(
          SpiConstants.PROPNAME_ACTION,
          SpiConstants.PROPNAME_DOCID,
          SpiConstants.PROPNAME_ACLUSERS,
          SpiConstants.PROPNAME_ACLGROUPS,
          SpiConstants.PROPNAME_ACLDENYUSERS,
          SpiConstants.PROPNAME_ACLDENYGROUPS,
          SpiConstants.PROPNAME_ACLINHERITANCETYPE,
          SpiConstants.PROPNAME_ACLINHERITFROM_DOCID
      );

  private final String docId;
  private final String parentId;
  private final AclInheritanceType inheritanceType;
  private final String googleGlobalNamespace;
  private final Set<String> allowUsers;
  private final Set<String> denyUsers;
  private final Set<String> allowGroups;
  private final Set<String> denyGroups;

  public AclDocument(String docId, String parentId,
      AclInheritanceType inheritanceType, String googleGlobalNamespace,
      Set<String> allowUsers, Set<String> denyUsers, Set<String> allowGroups,
      Set<String> denyGroups) {
    this.docId = docId;
    this.parentId = parentId;
    this.inheritanceType = inheritanceType;
    this.googleGlobalNamespace = googleGlobalNamespace;
    this.allowUsers = allowUsers;
    this.denyUsers = denyUsers;
    this.allowGroups = allowGroups;
    this.denyGroups = denyGroups;
  }

  @Override
  public Property findProperty(String name) throws RepositoryException {
    LinkedList<Value> list = new LinkedList<Value>();

    LOGGER.log(Level.FINEST, "Find ACL property: {0}", name);
    if (SpiConstants.PROPNAME_DOCUMENTTYPE.equals(name)) {
      list.add(Value.getStringValue(SpiConstants.DocumentType.ACL.toString()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
      list.add(Value.getStringValue(ActionType.ADD.toString()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      list.add(Value.getStringValue(docId));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLINHERITFROM_DOCID.equals(name)) {
      if (parentId == null) {
        return null;
      } else {
        list.add(Value.getStringValue(parentId));
        return new SimpleProperty(list);
      }
    } else if (SpiConstants.PROPNAME_ACLINHERITANCETYPE.equals(name)) {
      list.add(Value.getStringValue(inheritanceType.toString()));
      return new SimpleProperty(list);
    } else if (SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
      if (allowUsers.isEmpty()) {
        return null;
      } else {
        FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
            googleGlobalNamespace, allowUsers,
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
        return new SimpleProperty(list);
      }
    } else if (SpiConstants.PROPNAME_ACLDENYUSERS.equals(name)) {
      if (denyUsers.isEmpty()) {
        return null;
      } else {
        FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
            googleGlobalNamespace, denyUsers,
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
        return new SimpleProperty(list);
      }
    } else if (SpiConstants.PROPNAME_ACLGROUPS.equals(name)) {
      if (allowGroups.isEmpty()) {
        return null;
      } else {
        FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
            googleGlobalNamespace, allowGroups,
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
        return new SimpleProperty(list);
      }
    } else if (SpiConstants.PROPNAME_ACLDENYGROUPS.equals(name)) {
      if (denyGroups.isEmpty()) {
        return null;
      } else {
        FileUtil.addPrincipals(list, PrincipalType.UNKNOWN,
            googleGlobalNamespace, denyGroups,
            CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
        return new SimpleProperty(list);
      }
    } else {
      return null;
    }
  }

  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    return propNames;
  }
}
