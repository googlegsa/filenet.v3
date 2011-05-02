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
package com.google.enterprise.connector.filenet4.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.filenet4.FileUtil;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

/**
 *FileNet object factory. 
 **/
public class FnObjectFactory implements IObjectFactory {
	private static Logger logger = null;
	
	static {
		logger = Logger.getLogger(FnObjectFactory.class.getName());
}
	
	public FnObjectFactory() {
		super();
	}

	public IConnection getConnection(String contentEngineUri)
			throws RepositoryException {
		return new FnConnection(contentEngineUri);
	}

	public IObjectStore getObjectStore(String objectStoreName,
			IConnection conn, String userName, String userPassword)
			throws RepositoryException, RepositoryLoginException {

		Domain domain = null;
		UserContext uc = null;
		try{
			logger.info("getting the instance of domain from connection");
			domain = Factory.Domain.getInstance((Connection) conn.getConnection(), null);
			
			logger.info("getting the usercontext..");
			uc = UserContext.get();
		}catch(Throwable e){
			logger.log(Level.SEVERE,"Unable to get domain or usercontext",e);
			throw new RepositoryLoginException(e);
		}
		
		ObjectStore os=null;
		try {
			os = getRawObjectStore(userName, userPassword, conn, domain, objectStoreName, uc);
		} catch (Throwable e) {
			logger.log(Level.WARNING,"Unable to conenct to the Object Store with user: "+ userName);
			String shortName = FileUtil.getShortName(userName);
			logger.log(Level.INFO,"Trying to connect Object Store with user: "+ shortName+ " in short name format.");
			try{
				os = getRawObjectStore(shortName, userPassword, conn, domain, objectStoreName, uc);
			}catch(Throwable th){
				logger.log(Level.SEVERE,"Problems while connecting to FileNet object store. Got Exception: ",th);
				throw new RepositoryLoginException(e);
			}
		}
		return new FnObjectStore(os, conn, userName, userPassword);
	}

	public ISearch getSearch(IObjectStore objectStore)
			throws RepositoryException {
		SearchScope search = new SearchScope(objectStore.getObjectStore());

		return new FnSearch(search);
	}

	private ObjectStore getRawObjectStore(String userName, String userPassword, IConnection conn, Domain domain, String objectStoreName, UserContext uc) throws RepositoryException{
		logger.info("creating the subject for user: "+userName);
		Subject s = UserContext.createSubject((Connection) conn.getConnection(), userName, userPassword, "FileNetP8");
		uc.pushSubject(s);
		logger.info("Creating the FileNet object store instance..");
		ObjectStore os = Factory.ObjectStore.fetchInstance(domain,objectStoreName, null);
		os.refresh();
		logger.config("Connection to FileNet ObjectStore is successful...");
		return os;
	}
}
