package com.google.enterprise.connector.file.filewrap;

import java.io.FileInputStream;

import com.google.enterprise.connector.spi.LoginException;

public interface ISession {

	public IUser verify() throws LoginException;

	public void setConfiguration(FileInputStream stream);

	public void setObjectStore(IObjectStore objectStore);

}
