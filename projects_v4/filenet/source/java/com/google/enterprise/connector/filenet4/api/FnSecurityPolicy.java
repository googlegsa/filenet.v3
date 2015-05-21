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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.SecurityTemplateList;
import com.filenet.api.security.SecurityPolicy;
import com.filenet.api.security.SecurityTemplate;
import com.filenet.api.util.Id;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FnSecurityPolicy implements ISecurityPolicy {
  private static final Logger LOGGER =
      Logger.getLogger(FnSecurityPolicy.class.getName());

  private final SecurityPolicy secPolicy;
  private final Id id;

  public FnSecurityPolicy(SecurityPolicy securityPolicy)
      throws RepositoryException {
    this.secPolicy = securityPolicy;
    this.id = secPolicy.get_Id();
    LOGGER.log(Level.FINEST,
        "Constructing a new FnSecurityPolicy object [ID: {0}, Name: {1}]",
        new Object[] {id, secPolicy.get_Name()});
  }

  @Override
  public Id get_Id() {
    return id;
  }

  @Override
  public String get_Name() throws RepositoryException {
    return secPolicy.get_Name();
  }

  @Override
  public Iterable<ISecurityTemplate> getSecurityTemplates()
      throws RepositoryException {
    SecurityTemplateList templates = secPolicy.get_SecurityTemplates();
    if (templates != null && templates.size() > 0) {
      LOGGER.log(Level.FINEST,
          "Found {0} security templates for {1} security policy",
          new Object[] {templates.size(), id});
      ImmutableList.Builder<ISecurityTemplate> builder =
              ImmutableList.builder();
      @SuppressWarnings("unchecked")
      Iterator<SecurityTemplate> templatesIter = templates.iterator();
      while (templatesIter.hasNext()) {
        SecurityTemplate template = templatesIter.next();
        builder.add(new FnSecurityTemplate(template));
        LOGGER.log(Level.FINEST, "Adding {0} security template to list",
            template.get_DisplayName());
      }
      return builder.build();
    } else {
      LOGGER.log(Level.FINEST,
          "No security template is found for {0} security policy", id);
      return null;
    }
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return secPolicy.get_DateLastModified();
  }

  @Override
  public Id getVersionSeriesId() throws RepositoryDocumentException {
    return id;
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
      throws RepositoryDocumentException {
    return null;
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return false;
  }
}
