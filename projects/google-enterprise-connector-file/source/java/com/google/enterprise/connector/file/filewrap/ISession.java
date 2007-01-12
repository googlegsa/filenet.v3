package com.google.enterprise.connector.file.filewrap;

import java.io.FileInputStream;

import com.filenet.wcm.api.Session;

public interface ISession {
	
	public IUser verify();

	public void setConfiguration(FileInputStream stream);

	public Session getSession();

}
