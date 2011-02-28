package com.google.enterprise.connector.filenet4.filewrap;

public interface IPermissions {

	public boolean authorize(String Username);

	public boolean authorizeMarking(String Username);
}
