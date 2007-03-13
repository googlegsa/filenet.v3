package com.google.enterprise.connector.file.filewrap;

public interface IObjectStore {

	public IDocument getObject(String guidOrPath);

	public String getName();


}
