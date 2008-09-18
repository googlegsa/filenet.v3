package com.google.enterprise.connector.file.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import com.filenet.api.constants.ClassNames;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.property.Property;
import com.filenet.api.util.UserContext;
import com.google.enterprise.connector.file.FileConnector;
import com.google.enterprise.connector.file.FileDocument;
import com.google.enterprise.connector.file.FileSession;
import com.google.enterprise.connector.file.filejavawrap.FnUserContext;
import com.google.enterprise.connector.file.filewrap.IConnection;
import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.file.filewrap.IObjectFactory;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.IPermissions;
import com.google.enterprise.connector.file.filewrap.IUserContext;
import com.google.enterprise.connector.file.filewrap.IVersionSeries;
import com.google.enterprise.connector.file.TestConnection;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import junit.framework.TestCase;

public class FnDocumentTest extends TestCase {

	FileSession fs;
	IObjectStore ios;
	IConnection conn;
	IObjectFactory iof;
	IDocument fd, fd2;
	IVersionSeries vs;
	IUserContext uc;

	protected void setUp() throws RepositoryLoginException, RepositoryException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		FileConnector connec = new FileConnector();
		connec.setLogin(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_uri(TestConnection.uri);
		
		fs = (FileSession)connec.login();

		
		
		iof= (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
		IConnection conn = iof.getConnection(TestConnection.uri);
		Domain domain = Factory.Domain.getInstance(conn.getConnection(), null);
		ios = iof.getObjectStore(TestConnection.objectStore, conn, TestConnection.adminUsername, TestConnection.adminPassword);

		fd = (IDocument) ios.getObject(ClassNames.DOCUMENT, TestConnection.docId1);
		fd.fetch(TestConnection.included_meta);
		
		uc = new FnUserContext(conn);
		uc.authenticate(TestConnection.username,TestConnection.password);
		vs = (IVersionSeries) ios.getObject(ClassNames.VERSION_SERIES, TestConnection.docVsId1);
		fd2 = vs.getReleasedVersion();
		

	}
	
	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyName()'
	 */
	public void testGetPropertyName() throws RepositoryException {

		Set test = fd.getPropertyName();
		assertNotNull(test);
		Set Test2 = TestConnection.included_meta;
		Test2.add("Id");
		Test2.add("ClassDescription");
		Test2.add("ContentElements");
		Test2.add("DateLastModified");
		Test2.add("MimeType");
		Test2.add("VersionSeries");
		Test2.remove("ContentRetentionDate");
		Test2.remove("CurrentState");
		Test2.remove("LockTimeout");
		Test2.remove("LockToken");
		Test2.remove("StorageLocation");
		Iterator it = Test2.iterator();
		
		while (it.hasNext()) {
			String comp1 = it.next().toString();
			String comp2 = null;
//			System.out.println("reel : "+comp1);
			Iterator it2 = test.iterator();
			while (it2.hasNext()) {
				comp2 = it2.next().toString();
//				System.out.println("test : "+comp2);
				if (comp2.equals(comp1)){
					break;
				}
			}
//			System.out.println("==========");
			assertEquals(comp1, comp2);
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyType(String)'
	 */
	public void testGetPropertyType() throws RepositoryException {
		
		String[][] typeArray = TestConnection.type;
		
		
		Set meta = TestConnection.included_meta;
		meta.add("Id");
		meta.add("ClassDescription");
		meta.add("ContentElements");
		meta.add("DateLastModified");
		meta.add("MimeType");
		meta.add("VersionSeries");
		Iterator metaIt = meta.iterator();
		while (metaIt.hasNext()) {
			String property = metaIt.next().toString();
			String typeGet = fd.getPropertyType(property);
			String typeSet = null;
			for(int i = 0; i<typeArray.length; i++){
				if(typeArray[i][0] == property){
					typeSet = typeArray[i][1];
					break;
				}
			}
			assertEquals(typeGet, typeSet);
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getVersionSeries()'
	 */
	public void testGetVersionSeries() throws RepositoryException {
		IVersionSeries vs = fd.getVersionSeries();
		assertEquals("{"+TestConnection.docVsId1+"}", vs.getId(ActionType.ADD));
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getId()'
	 */
	public void testGetId() throws RepositoryException {
		assertEquals("{"+TestConnection.docId1+"}", fd.getId(ActionType.ADD)); 
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPermissions()'
	 */
	public void testGetPermissions() throws RepositoryException {
		IPermissions perm = fd2.getPermissions();
		boolean test = perm.authorize("");
		assertEquals(false, test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getContent()'
	 */
	public void testGetContent() throws RepositoryException {
		uc.authenticate(TestConnection.adminUsername, TestConnection.adminPassword);
		InputStream is = fd.getContent();
		assertNotNull(is);
		assertTrue(is instanceof InputStream);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyStringValue(String)'
	 */
	public void testGetPropertyStringValue() throws RepositoryException {
		String test = fd.getPropertyStringValue("DocumentTitle");
		assertEquals("Doc1", test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyGuidValue(String)'
	 */
	public void testGetPropertyGuidValue() throws RepositoryException {
		String test = fd.getPropertyGuidValue("Id");
		assertEquals(TestConnection.docId1, test);

	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyLongValue(String)'
	 */
	public void testGetPropertyLongValue() throws RepositoryException {
		long test = fd.getPropertyLongValue("MajorVersionNumber");
		assertEquals(1, test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyDoubleValue(String)'
	 */
	public void testGetPropertyDoubleValue() throws RepositoryException {
		Double test = new Double(fd.getPropertyDoubleValue("ContentSize"));
		Double value = new Double(2998306.0);
		assertEquals(value, test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyDateValue(String)'
	 */
	public void testGetPropertyDateValue() throws RepositoryException {
		Date testGetted = fd.getPropertyDateValue("DateCreated");
		Date testSetted = new Date(1183555393920L);

		assertEquals(testSetted, testGetted);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyBooleanValue(String)'
	 */
	public void testGetPropertyBooleanValue() throws RepositoryException {
		boolean test = fd.getPropertyBooleanValue("IsCurrentVersion");
		assertEquals(true, test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyBinaryValue(String)'
	 */
	public void testGetPropertyBinaryValue() throws RepositoryException {
		byte[] test = fd.getPropertyBinaryValue("PublicationInfo");
		if(test != null)
		{
			assertTrue(test instanceof byte[]);
		}
		
	}

}
