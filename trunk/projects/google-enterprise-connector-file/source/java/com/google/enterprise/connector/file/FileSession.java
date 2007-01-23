package com.google.enterprise.connector.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.enterprise.connector.file.filejavawrap.IFileObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class FileSession implements Session {

	private ISession fileSession;

	private IObjectFactory fileObjectFactory;

	private IObjectStore objectStore;

	private String pathToWcmApiConfig;

	// Constructor never called except for tests
	public FileSession() throws RepositoryException {
		try {
			System.out.println("sesion.clear " + com.filenet.wcm.api.Session.CLEAR);
			fileObjectFactory = new IFileObjectFactory();
			fileSession = fileObjectFactory.getSession("file-connector", null,
					"P8Admin", "UnDeuxTrois456");
			this.pathToWcmApiConfig = "C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties";
			fileSession
					.setConfiguration(new FileInputStream(
							"C:\\_dev\\google\\connector\\connector-file\\projects\\third_party\\WcmApiConfig.properties"));
//			fileSession.setRemoteServerUrl("http://swp-srv-gsaecm.sword.fr:8008/ApplicationEngine/xcmisasoap.dll");
//			fileSession.setRemoteServerDownloadUrl("http://swp-srv-gsaecm.sword.fr:8008/ApplicationEngine/doccontent.dll");
//			fileSession.setRemoteServerUploadUrl("http://swp-srv-gsaecm.sword.fr:8008/ApplicationEngine/doccontent.dll");
			fileSession.verify();
			objectStore = fileObjectFactory.getObjectStore("GSA_Filenet",
					fileSession);
			
			objectStore
					.setDisplayUrl("http://swp-vm-fnet352:8080/Workplace/getContent"+"?objectType=document"+"&objectStoreName="+"GSA_Filenet"+"&id=");
//			fileSession.setObjectStore(objectStore);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// this.client.setSession(session);
	}

	public FileSession(String iObjectFactory, String userName,
			String userPassword, String appId, String credTag,
			String objectStoreName, String pathToWcmApiConfig, String displayUrl)
			throws RepositoryException {
		try {
			System.out.println("fileSession "  + userName + " "+userPassword);
			setFileObjectFactory(iObjectFactory);
			if(credTag.equals("")){
				credTag = null;
			}
			fileSession = fileObjectFactory.getSession(appId, credTag,
					userName, userPassword);
			this.pathToWcmApiConfig = pathToWcmApiConfig;
			fileSession.setConfiguration(new FileInputStream(
					this.pathToWcmApiConfig));
			fileSession.verify();
			objectStore = fileObjectFactory.getObjectStore(objectStoreName,
					fileSession);
			objectStore.setDisplayUrl(displayUrl+"getContent?objectType=document&objectStoreName="+objectStoreName+"&id=");
//			fileSession.setObjectStore(objectStore);
		} catch (FileNotFoundException de) {
			RepositoryException re = new LoginException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}catch(Exception e){
			System.out.println("exception in FileSession()");
			e.printStackTrace();
			RepositoryException re = new LoginException(e.getMessage(), e
					.getCause());
			re.setStackTrace(e.getStackTrace());
			throw re;
		}
		// this.client.setSession(session);
	}

	private void setFileObjectFactory(String objectFactory) {
		
		try {
			fileObjectFactory = (IObjectFactory) Class.forName(objectFactory).newInstance();
		} catch (InstantiationException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Root Cause : " + e.getCause() + " ; Message : " + e.getMessage());
		}
		
	}

	public QueryTraversalManager getQueryTraversalManager()
			throws RepositoryException {
		FileQueryTraversalManager fileQTM = new FileQueryTraversalManager(
				fileObjectFactory, objectStore, fileSession);
		return fileQTM;
	}

	public AuthenticationManager getAuthenticationManager()
			throws RepositoryException {
		FileAuthenticationManager fileAm = new FileAuthenticationManager(
				fileObjectFactory, pathToWcmApiConfig);
		return fileAm;
	}

	public AuthorizationManager getAuthorizationManager()
			throws RepositoryException {
		System.out.println("getAuthorizationManager");
		FileAuthorizationManager fileAzm = new FileAuthorizationManager(fileObjectFactory, pathToWcmApiConfig, objectStore);
		return fileAzm;
	}

}
