package com.google.enterprise.connector.filenet4.api;

import java.util.HashMap;
import java.util.Map;

class FnCredentialMap {
  private static Map<String, String> authUser = null;

  protected static synchronized void putUserCred(String name, String password) {
    authUser.put(name, password);
  }

  protected static synchronized String getUserCred(String name) {
    return authUser.get(name);
  }

  protected static synchronized boolean containsUserCred(String name) {
    return authUser.containsKey(name);
  }

  protected static synchronized boolean isNull() {
    return (authUser == null);
  }

  protected static synchronized void init() {
    authUser = new HashMap<String, String>();
  }
}
