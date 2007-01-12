package com.google.enterprise.connector.file.filewrap;

public interface IObjectStore {
	
	public IDocument getObject(int objectType, String guidOrPath);
	public void setDisplayUrl(String displayUrl);
	public String getDisplayUrl();
	public String getName();

}
