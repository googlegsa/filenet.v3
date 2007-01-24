package com.google.enterprise.connector.file.filewrap;

import java.io.FileInputStream;

public interface ISession {
	
	public IUser verify();

	public void setConfiguration(FileInputStream stream);

	public void setObjectStore(IObjectStore objectStore);


}
