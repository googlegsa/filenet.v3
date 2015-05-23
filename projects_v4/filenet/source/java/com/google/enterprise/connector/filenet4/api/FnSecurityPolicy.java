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

/*
 * TODO(jlacey): This class looks trivial to remove, except that
 * instances are created by FnObjectFactory and it must extend
 * IBaseObject (which it does through ISecurityPolicy).
 */
public class FnSecurityPolicy implements ISecurityPolicy {
  private static final Logger LOGGER =
      Logger.getLogger(FnSecurityPolicy.class.getName());

  private final SecurityPolicy secPolicy;
  private final Id id;

  public FnSecurityPolicy(SecurityPolicy securityPolicy) {
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
  public String get_Name() {
    return secPolicy.get_Name();
  }

  @Override
  public SecurityTemplateList get_SecurityTemplates() {
    return secPolicy.get_SecurityTemplates();
  }

  @Override
  public Date getModifyDate() {
    return secPolicy.get_DateLastModified();
  }

  @Override
  public Id getVersionSeriesId() {
    return id;
  }

  @Override
  public Date getPropertyDateValueDelete(String name) {
    return null;
  }

  @Override
  public boolean isDeletionEvent() {
    return false;
  }

  @Override
  public boolean isReleasedVersion() {
    return false;
  }
}
