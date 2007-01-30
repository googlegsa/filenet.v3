package com.google.enterprise.connector.file.filemockwrap;

import java.io.FileInputStream;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISession;
import com.google.enterprise.connector.file.filewrap.IUser;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.mock.jcr.MockJcrSession;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnObjectStore implements IObjectStore, ISession {
	private MockJcrRepository repo;

	private String mockRepositoryEventList;

	private String isPublic;

	private MockJcrSession session;

	private String userId;

	private String password;

	/*
	 * protected MockFnObjectStore(String mockRepEvList, ISession session)
	 * throws RepositoryException { this.mockRepositoryEventList =
	 * mockRepEvList; MockRepositoryEventList mrel = new
	 * MockRepositoryEventList(this.mockRepositoryEventList); this.repo = new
	 * MockJcrRepository(new MockRepository(mrel)); }
	 */

	protected MockFnObjectStore(String appId, String credTag, String userId,
			String password) {
		this.userId = userId;
		this.password = password;
	}

	public IDocument getObject(String guidOrPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDisplayUrl(String displayUrl) {
		// Nothing. If a display URL is implemented for the Mock, return an
		// hard-coded one in getDisplayURL method.
	}

	public String getDisplayUrl() {
		return "http://tomcatURL:8080/someTestServletThatRetrievesContentOfTheEventList?EventList="
				+ mockRepositoryEventList + "&DocID=";
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIsPublic() {
		return this.isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
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

	public IUser verify() throws RepositoryException, LoginException {
		Credentials creds = new SimpleCredentials(this.userId, this.password
				.toCharArray());

		try {
			this.session = (MockJcrSession) repo.login(creds);
		} catch (javax.jcr.LoginException e) {
			throw new LoginException(e);
		} catch (javax.jcr.RepositoryException e) {
			throw new com.google.enterprise.connector.spi.RepositoryException(e);
		}
		return null;
	}

	/**
	 * FileInputStream that reads paths to dlls and stuff like that. No
	 * equivalence in mock then do nothing.
	 */
	public void setConfiguration(FileInputStream stream) {
	}

	public void setObjectStore(IObjectStore objectStore) {
		// TODO Auto-generated method stub

	}

}
