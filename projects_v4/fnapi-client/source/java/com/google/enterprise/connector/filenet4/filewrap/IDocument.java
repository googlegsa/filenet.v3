// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4.filewrap;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface between the Client Document and Core document.
 * @author pankaj_chouhan
 *
 */
public interface IDocument extends IBaseObject {

	public void fetch(Set includedMeta)throws RepositoryDocumentException;

	public IPermissions getPermissions() throws RepositoryException;

	public InputStream getContent() throws RepositoryDocumentException;

	public IVersionSeries getVersionSeries() throws RepositoryDocumentException;;

	public Set getPropertyName() throws RepositoryDocumentException;

	public String getPropertyType(String name) throws RepositoryDocumentException;

	/*public String getPropertyStringValue(String name)
			throws RepositoryDocumentException;
*/
	public void getPropertyStringValue(String name, LinkedList set)
	throws RepositoryDocumentException;

	public void getPropertyGuidValue(String name, LinkedList list) throws RepositoryDocumentException;

	public void getPropertyLongValue(String name, LinkedList list) throws RepositoryDocumentException;

	public void getPropertyDoubleValue(String name, LinkedList list)
			throws RepositoryDocumentException;

	public void getPropertyDateValue(String name, LinkedList list) throws RepositoryDocumentException;

	public void getPropertyBooleanValue(String name, LinkedList list)
			throws RepositoryDocumentException;

	public void getPropertyBinaryValue(String name, LinkedList list)
			throws RepositoryDocumentException;

}
