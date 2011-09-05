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
package com.google.enterprise.connector.filenet4;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

/**
 * Implementation of Property, to provide support for Multivalued Property as well
 * @author pankaj_chouhan
 *
 */
public class FileDocumentProperty implements Property {

	private static Logger logger = Logger.getLogger(FileDocumentList.class
			.getName());
	private String name;

	private Iterator iter;

	public FileDocumentProperty(String name) {
		this.name = name;
	}

	public FileDocumentProperty(String name, List value) {
		this.name = name;
		this.iter = value.iterator();
	}

	public Value nextValue() throws RepositoryException {
		Value value = null;
		if (this.iter.hasNext()) {
			Object object = this.iter.next();
			if(object instanceof Value){
				value = (Value)object;
			}
		}
		return value;
	}

	public String getName() throws RepositoryException {
		return name;
	}

}
