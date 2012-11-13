package com.google.enterprise.connector.filenet4.filejavawrap;

import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectSet;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import com.filenet.api.util.UserContext;

import java.util.Iterator;

import junit.framework.TestCase;

public class FnObjectFactoryTest extends TestCase {

  FileSession fs;
  IObjectStore ios;
  IConnection conn;
  UserContext uc;
  IObjectFactory iof;
  IDocument fd;

  protected void setUp() throws RepositoryLoginException,
          RepositoryException, InstantiationException,
          IllegalAccessException, ClassNotFoundException {
    FileConnector connec = new FileConnector();
    connec.setUsername(TestConnection.adminUsername);
    connec.setPassword(TestConnection.adminPassword);
    connec.setObject_store(TestConnection.objectStore);
    connec.setWorkplace_display_url(TestConnection.displayURL);
    connec.setObject_factory(TestConnection.objectFactory);
    connec.setContent_engine_url(TestConnection.uri);

    fs = (FileSession) connec.login();

    iof = (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
    conn = iof.getConnection(TestConnection.uri, TestConnection.adminUsername, TestConnection.adminPassword);
    // Domain domain = Factory.Domain.getInstance(conn.getConnection(),
    // "P8.V4");
    ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.username, TestConnection.password);

  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getConnection(String)'
   */
  public void testGetConnection() throws RepositoryException {
    assertNotNull(conn);
    assertEquals(TestConnection.uri, conn.getConnection().getURI());
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getObjectStore(String,
   * IConnection, String, String)'
   */
  public void testGetObjectStore() throws RepositoryLoginException,
          RepositoryException {
    assertNotNull(ios);
    assertEquals("GED", ios.getName());
  }

  /*
   * Test method for
   * 'com.google.enterprise.connector.file.filejavawrap.FnObjectFactory.getSearch(IObjectStore)'
   */
  public void testGetSearch() throws RepositoryException {
    ISearch is = iof.getSearch(ios);
    IObjectSet test = is.execute("SELECT TOP 50 d.Id, d.DateLastModified FROM Document AS d WHERE d.Id='3811870F-410F-4C25-B853-CAC56014C552' and VersionStatus=1 and ContentSize IS NOT NULL  AND (ISCLASS(d, Document) OR ISCLASS(d, WorkflowDefinition))  ORDER BY DateLastModified,Id");
    assertEquals(1, test.getSize());
    Iterator it = test.getIterator();
    while (it.hasNext()) {
      IBaseObject ibo = (IBaseObject) it.next();
      assertEquals("3811870F-410F-4C25-B853-CAC56014C552", ibo.getId(ActionType.ADD));
    }
  }

}
