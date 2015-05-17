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

package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IFolder;
import com.google.enterprise.connector.filenet4.filewrap.IId;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.property.Properties;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

public class FnFolder implements IFolder {
  private static final Logger logger =
      Logger.getLogger(FnFolder.class.getName());

  private final Folder folder;

  public FnFolder(Folder folder) {
    this.folder = folder;
    logger.finer("Constructing FnFolder object for " + folder.get_FolderName());
  }

  @Override
  public String get_FolderName() {
    return folder.get_FolderName();
  }

  @Override
  public IId get_Id() throws RepositoryDocumentException {
    return new FnId(folder.get_Id());
  }

  @Override
  public Date getModifyDate() throws RepositoryDocumentException {
    return folder.get_DateLastModified();
  }

  @Override
  public IId getVersionSeriesId() throws RepositoryDocumentException {
    return get_Id();
  }

  @Override
  public Date getPropertyDateValueDelete(String name)
          throws RepositoryDocumentException {
    Properties props = folder.getProperties();
    return props.getDateTimeValue(name);
  }

  @Override
  public boolean isDeletionEvent() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public boolean isReleasedVersion() throws RepositoryDocumentException {
    return false;
  }

  @Override
  public IObjectSet get_ContainedDocuments() throws RepositoryException {
    try {
      ImmutableList.Builder<IDocument> builder = ImmutableList.builder();
      DocumentSet docSet = folder.get_ContainedDocuments();
      for (Iterator<?> iter = docSet.iterator(); iter.hasNext();) {
        builder.add(new FnDocument((Document) iter.next()));
      }
      return new FnObjectList(builder.build());
    } catch (EngineRuntimeException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public IObjectSet get_SubFolders() throws RepositoryException {
    try {
      ImmutableList.Builder<IFolder> builder = ImmutableList.builder();
      FolderSet folderSet = folder.get_SubFolders();
      for (Iterator<?> iter = folderSet.iterator(); iter.hasNext();) {
        builder.add(new FnFolder((Folder) iter.next()));
      }
      return new FnObjectList(builder.build());
    } catch (EngineRuntimeException e) {
      throw new RepositoryException(e);
    }
  }
}
