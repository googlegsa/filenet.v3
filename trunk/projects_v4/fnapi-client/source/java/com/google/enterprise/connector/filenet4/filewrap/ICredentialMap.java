package com.google.enterprise.connector.filenet4.filewrap;

public interface ICredentialMap {

  public void putUserCred(String name, String password);

  public String getUserCred(String name);
}
