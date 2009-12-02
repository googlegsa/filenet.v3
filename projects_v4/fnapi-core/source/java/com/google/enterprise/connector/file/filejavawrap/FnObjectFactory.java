package com.google.enterprise.connector.file.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 *FileNet object factory. 
 **/
public class FnObjectFactory implements IObjectFactory {
	private static Logger logger = null;
	
	static {
		logger = Logger.getLogger(FnObjectFactory.class.getName());
//		System.setProperty("wasp.location", "E:\\Filenet connector binaries\\FileNet 4 dependency\\wsi");
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
			logger.info("creating the subject for user: "+userName);
			Subject s = UserContext.createSubject((Connection) conn.getConnection(), userName, userPassword, "FileNetP8");
			uc.pushSubject(s);
		
//			logger.info("Creating the FileNet object store instance..[domain= "+domain.get_Name()+", objectStoreName= "+objectStoreName+"]");
			logger.info("Creating the FileNet object store instance..");
			os = Factory.ObjectStore.fetchInstance(domain,objectStoreName, null);
			logger.info("FileNet object store creation succeeded..");
			os.refresh();
			logger.config("FileNet object store is refreshed..");
		} catch (Throwable e) {
			logger.log(Level.SEVERE,"Problems while connecting to FileNet object store. Got Exception: ",e);
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
