package com.google.enterprise.connector.filenet4.filewrap;

public interface IActiveMarkingList {

	// public boolean authorize(String Username);
	public IActiveMarkingList getActiveMarkings();

	public boolean authorize(String Username);
}
