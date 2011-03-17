// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4.filejavawrap;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import com.filenet.api.constants.ClassNames;
import com.google.enterprise.connector.filenet4.FileConnector;
import com.google.enterprise.connector.filenet4.FileNetTestCase;
import com.google.enterprise.connector.filenet4.FileSession;
import com.google.enterprise.connector.filenet4.TestConnection;
import com.google.enterprise.connector.filenet4.filejavawrap.FnUserContext;
import com.google.enterprise.connector.filenet4.filewrap.IConnection;
import com.google.enterprise.connector.filenet4.filewrap.IDocument;
import com.google.enterprise.connector.filenet4.filewrap.IObjectFactory;
import com.google.enterprise.connector.filenet4.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet4.filewrap.IPermissions;
import com.google.enterprise.connector.filenet4.filewrap.IUserContext;
import com.google.enterprise.connector.filenet4.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import junit.framework.TestCase;

/**
 * JUnit TestCases related to Core Document class.
 * @author pankaj_chouhan
 *
 */
public class FnDocumentTest extends FileNetTestCase{
	FileSession fs;
	IObjectStore ios;
	IConnection conn;
	IObjectFactory iof;
	IDocument fd, fd2;
	IVersionSeries vs;
	IUserContext uc;

	protected void setUp() throws RepositoryLoginException, RepositoryException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		FileConnector connec = new FileConnector();
		connec.setUsername(TestConnection.adminUsername);
		connec.setPassword(TestConnection.adminPassword);
		connec.setObject_store(TestConnection.objectStore);
		connec.setWorkplace_display_url(TestConnection.displayURL);
		connec.setObject_factory(TestConnection.objectFactory);
		connec.setContent_engine_url(TestConnection.uri);

		fs = (FileSession)connec.login();



		iof= (IObjectFactory) Class.forName(TestConnection.objectFactory).newInstance();
		IConnection conn = iof.getConnection(TestConnection.uri);
//		Domain domain = Factory.Domain.getInstance(conn.getConnection(), null);
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
	public void ftestGetPropertyName() throws RepositoryException {

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
	public void ftestGetPropertyType() throws RepositoryException {

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
	public void ftestGetVersionSeries() throws RepositoryException {
		IVersionSeries vs = fd.getVersionSeries();
		assertEquals("{"+TestConnection.docVsId1+"}", vs.getId(ActionType.ADD));
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getId()'
	 */
	public void ftestGetId() throws RepositoryException {
		assertEquals("{"+TestConnection.docId1+"}", fd.getId(ActionType.ADD));
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPermissions()'
	 */
	public void ftestGetPermissions() throws RepositoryException {
		IPermissions perm = fd2.getPermissions();
		boolean test = perm.authorize("");
		assertEquals(false, test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getContent()'
	 */
	public void ftestGetContent() throws RepositoryException {
		uc.authenticate(TestConnection.adminUsername, TestConnection.adminPassword);
		InputStream is = fd.getContent();
		assertNotNull(is);
		assertTrue(is instanceof InputStream);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyStringValue(String)'
	 */
	public void testGetPropertyStringValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyStringValue("Name", list);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}

//		assertEquals("Doc1", test);
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyGuidValue(String)'
	 */
	public void ftestGetPropertyGuidValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyGuidValue("MyID", list);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyLongValue(String)'
	 */
	public void ftestGetPropertyLongValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyLongValue("MyInteger", list);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyDoubleValue(String)'
	 */
	public void ftestGetPropertyDoubleValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyDoubleValue("MyFloat", list);
		Double value = new Double(2998306.0);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyDateValue(String)'
	 */
	public void ftestGetPropertyDateValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyDateValue("MyDate", list);
		Date testSetted = new Date(1183555393920L);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyBooleanValue(String)'
	 */
	public void ftestGetPropertyBooleanValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyBooleanValue("MyBoolean", list);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

	/*
	 * Test method for 'com.google.enterprise.connector.file.filejavawrap.FnDocument.getPropertyBinaryValue(String)'
	 */
	public void ftestGetPropertyBinaryValue() throws RepositoryException {
		LinkedList list = new LinkedList();
		fd.getPropertyBinaryValue("MyBinary", list);
		try{
			assertEquals("Either Metadata is not found or Metadata is MultiValued.", 1, list.size());
		}catch(AssertionError e){
			System.out.println(e.getLocalizedMessage());
			assertEquals("Metadata is neither Multivalued nor it is found", 0, list.size());
		}
	}

}
