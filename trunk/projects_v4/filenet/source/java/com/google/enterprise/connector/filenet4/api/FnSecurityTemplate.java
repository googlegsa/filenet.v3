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

package com.google.enterprise.connector.filenet4.api;

import com.google.enterprise.connector.filenet4.Permissions;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.security.SecurityTemplate;
import com.filenet.api.util.Id;

import java.util.logging.Logger;

public class FnSecurityTemplate implements ISecurityTemplate {
  private static final Logger LOGGER =
      Logger.getLogger(FnSecurityTemplate.class.getName());

  private final SecurityTemplate secTemplate;

  public FnSecurityTemplate(SecurityTemplate securityTemplate) {
    this.secTemplate = securityTemplate;
  }

  @Override
  public String get_DisplayName() throws RepositoryException {
    try {
      return secTemplate.get_DisplayName();
    } catch (EngineRuntimeException e) {
      throw new RepositoryException(
          "Unable to get security template display name", e);
    }
  }

  @Override
  public Permissions get_TemplatePermissions() throws RepositoryException {
    try {
      LOGGER.finest("Getting security template permissions");
      return new Permissions(secTemplate.get_TemplatePermissions());
    } catch (EngineRuntimeException e) {
      throw new RepositoryException("Unable to get permissions from "
          + get_DisplayName() + " security template", e);
    }
  }

  @Override
  public Id get_ApplyStateID() throws RepositoryException {
    try {
      return secTemplate.get_ApplyStateID();
    } catch (EngineRuntimeException e) {
      throw new RepositoryException("Unable to get security template state ID",
          e);
    }
  }
}
