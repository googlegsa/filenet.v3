package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileInputStream;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import com.filenet.wcm.api.Session;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IGettableObject;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.mock.jcr.MockJcrSession;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnSessionAndObjectStore implements IObjectStore, ISession {
	private MockJcrRepository repo;

	private String mockRepositoryEventList;

	private String userId;

	private String password;

	private boolean isAuthenticated = false;

	// Note : This super class implements two key interfaces : Session and
	// ObjectStore.
	// Assumes that verify() method is called only when an ObjectStoreName has
	// been defined. Indeed FileNet authentifies session before it defines an
	// object Store (because it only needs LDAP and this mechnism cannot be
	// reproduced for the Mock (the users are defined in an EventList which
	// actually is the ObjectStoreName therefore we merged those two classes for
	// the mock so that we can get away with this asynchronism problem.

	protected MockFnSessionAndObjectStore(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}

	/**
	 * Retrieve object by its ID.
	 */
	public IDocument getObject(String guidOrPath) {

		MockRepositoryDocument doc = this.repo.getRepo().getStore().getDocByID(
				guidOrPath);
		return new MockFnDocument(doc);
	}

	/**
	 * The MockRepositoryDocumentStore is needed in this package (at least in
	 * MockFnSearch class) then implement a protected getter for it.
	 * 
	 * @return this.mockRepositoryEventList
	 */
	protected MockRepositoryDocumentStore getMockRepositoryDocumentStore() {
		return this.repo.getRepo().getStore();
	}

	protected boolean hasBeenAuthenticated() {
		return isAuthenticated;
	}

	public IUser verify() throws RepositoryException, RepositoryLoginException {
		Credentials creds = new SimpleCredentials(this.userId, this.password
				.toCharArray());
		valuateEventList("SwordEventLog.txt");
		// valuateEventList(this.mockRepositoryEventList);
		this.isAuthenticated = false;
		try {
			MockJcrSession session = null;// The connector is not able to deal
			// with more than one session instance.
			session = (MockJcrSession) repo.login(creds);
			if (session == null) {
				throw new RepositoryLoginException(
						"MockJcrRepository.login() returned null session => RepositoryLoginException manually thrown"
								+ " from MockFnSessionAndObjectStore.verify(). Cause of this failure among the three following "
								+ "parameters :\n\t- EventList = "
								+ this.mockRepositoryEventList
								+ "\n\t- userId = "
								+ this.userId
								+ "\n\t- password = " + this.password);
			}
			this.isAuthenticated = true;
			return new MockFnUser(userId);
//		} catch (javax.jcr.LoginException e) {
//			throw new RepositoryLoginException(e);
//		} catch (javax.jcr.RepositoryException e) {
//			throw new com.google.enterprise.connector.spi.RepositoryException(e);
		}catch(Exception e){
			throw new RepositoryException(e);
		}
	}

	/**
	 * FileInputStream that reads paths to dlls and stuff like that. No
	 * equivalence in mock then do nothing.
	 */
	public void setConfiguration(FileInputStream stream) {
	}

	protected void valuateEventList(String evntLst) {
		this.mockRepositoryEventList = evntLst;
		this.repo = new MockJcrRepository(new MockRepository(
				new MockRepositoryEventList(evntLst)));
	}

	public String getName() throws RepositoryException {
		return mockRepositoryEventList;
	}

	public IGettableObject getObject(int type, String guidOrPath) {
		if (type == 1140) {
			MockRepositoryDocument doc = this.repo.getRepo().getStore()
					.getDocByID(guidOrPath);
			return new MockFnVersionSeries(doc);
		} else if (type == 1) {
			MockRepositoryDocument doc = this.repo.getRepo().getStore()
					.getDocByID(guidOrPath);
			return new MockFnDocument(doc);
		}
		return null;
	}

	public Session getSession() {
		// TODO Auto-generated method stub
		return null;
	}

}
