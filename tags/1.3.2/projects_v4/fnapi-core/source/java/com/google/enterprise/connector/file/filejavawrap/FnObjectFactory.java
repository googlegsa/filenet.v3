package com.google.enterprise.connector.file.filejavawrap;

import javax.security.auth.Subject;

import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

public class FnObjectFactory implements IObjectFactory {
	
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
		
		Domain domain = Factory.Domain.getInstance((Connection) conn
				.getConnection(), null);
		UserContext uc = UserContext.get();
		
		ObjectStore os=null;
		try {
			Subject s = UserContext.createSubject((Connection) conn
					.getConnection(), userName, userPassword, "FileNetP8");
			uc.pushSubject(s);
		
			os = Factory.ObjectStore.fetchInstance(domain,
				objectStoreName, null);
			os.refresh();
		} catch (Exception e) {
			throw new RepositoryLoginException(e);
		}
		return new FnObjectStore(os, conn, userName, userPassword);
	}

	public ISearch getSearch(IObjectStore objectStore)
			throws RepositoryException {
		SearchScope search = new SearchScope(objectStore.getObjectStore());

		return new FnSearch(search);
	}

}
