package com.google.enterprise.connector.file;

import java.util.List;

import com.filenet.wcm.api.BaseObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SpiConstants;

public class FileAuthorizationManager implements AuthorizationManager {
	IObjectFactory objectFactory;
	IObjectStore objectStore;
	String pathToWcmApiConfig;
	
	public FileAuthorizationManager(IObjectFactory fileObjectFactory, String pathToWcmApiConfig, IObjectStore objectStore){
		objectFactory = fileObjectFactory;
		this.pathToWcmApiConfig =pathToWcmApiConfig;
		this.objectStore = objectStore;
		System.out.println("constructeur authoriza");
		
	}

	public ResultSet authorizeDocids(List docidList, String username)
			throws RepositoryException {
		
		System.out.println("FileAuthorize method authorizeDocids");
		FileResultSet result = new FileResultSet();
		SimplePropertyMap map = null;
		IDocument doc = null;
		
		for(int i = 0; i< docidList.size(); i++){
			map = new SimplePropertyMap();
			try {
				doc = objectStore.getObject(BaseObject.TYPE_DOCUMENT,URLDecoder.decode((String)docidList.get(i),"UTF-8"));
				map.putProperty(new FileSimpleProperty(SpiConstants.PROPNAME_DOCID,(String)docidList.get(i)));
			} catch (UnsupportedEncodingException e) {
				RepositoryException re = new RepositoryException(e.getMessage(),e.getCause());
				re.setStackTrace(e.getStackTrace());
				throw re;
				
			} catch (RepositoryException e) {
				throw e;
			}
			if(doc.getPermissions().asMask(username) == 1){
				
				map.putProperty(new FileSimpleProperty(SpiConstants.PROPNAME_AUTH_VIEWPERMIT, true));
			}else{
				map.putProperty(new FileSimpleProperty(SpiConstants.PROPNAME_AUTH_VIEWPERMIT, false));
			}
			result.add(map);
			System.out.println("hasRight? " +doc.getPermissions().asMask(username));
		}
		
		System.out.println("end of FileAuthorize method authorieDocIds");
		return result;
	}

	public ResultSet authorizeTokens(List tokenList, String username)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
