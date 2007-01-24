package com.google.enterprise.connector.file.filejavawrap;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.wcm.api.BaseObject;
import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.file.Field;
import com.google.enterprise.connector.file.FileResultSet;
import com.google.enterprise.connector.file.FileSimpleProperty;
import com.google.enterprise.connector.file.FileSimpleValue;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;


public class FnSearch implements ISearch {

	Search search;

	public FnSearch(Search search) {
		this.search = search;
	}

	public ResultSet executeXml(String query, IObjectStore objectStore, Field[] fields)
			throws RepositoryException {
		FnObjectStore fileObjectStore = (FnObjectStore) objectStore;
		String result = search.executeXML(query);
		Document resultDoc = stringToDom(result);
		LinkedHashMap fieldsMap = new LinkedHashMap(fields.length * 2);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].fieldName != null)
                fieldsMap.put(fields[i].fieldName, fields[i]);
        }
		ResultSet resultSet = buildResultSetFromDocument(resultDoc,
				fileObjectStore, fieldsMap);
		return resultSet;
	}

	private ResultSet buildResultSetFromDocument(Document resultDoc,
			FnObjectStore objectStore, LinkedHashMap fields) throws RepositoryException {
		System.out.println("in buildResultSetFromDocument");
		// IFileObjectStore objectStore = fileSession.getIObjectStore();
		FileResultSet resultSet = new FileResultSet();
		SimplePropertyMap simplePm;
		NodeList data = resultDoc.getElementsByTagName("rs:data").item(0)
				.getChildNodes();
		System.out.println(data.getLength());
		for (int i = 0; i < data.getLength(); i++) {
			simplePm = new SimplePropertyMap();
			NamedNodeMap nodeMap = data.item(i).getAttributes();
			FnDocument fileObject = null;
			String id = null;
			if (nodeMap != null) {
				
				for (int j = 0; j < nodeMap.getLength(); j++) {
					
					if(fields.containsKey(nodeMap.item(j).getNodeName())){
						Field f = (Field) fields.get(nodeMap.item(j).getNodeName());
						simplePm.putProperty(new FileSimpleProperty(
								f.propertyName, new FileSimpleValue(
										f.fieldType, nodeMap.item(j)
												.getNodeValue())));
						if(f.fieldName.equals("Id")){
							id  = nodeMap.item(j)
							.getNodeValue();
						}
					}	
				}
				fileObject = (FnDocument) objectStore.getObject(
						BaseObject.TYPE_DOCUMENT, id);

				simplePm.putProperty(new FileSimpleProperty(
						SpiConstants.PROPNAME_CONTENT,
						new FileSimpleValue(ValueType.BINARY,
								fileObject)));
				simplePm
				.putProperty(new FileSimpleProperty(
						SpiConstants.PROPNAME_ISPUBLIC,
						new FileSimpleValue(ValueType.BOOLEAN,
								"false")));
				simplePm.putProperty(new FileSimpleProperty(
						SpiConstants.PROPNAME_DISPLAYURL,
						new FileSimpleValue(ValueType.STRING, objectStore
								.getDisplayUrl()
								+ id)));
				
//				simplePm.putProperty(new FileSimpleProperty(
//						SpiConstants.PROPNAME_SECURITYTOKEN,
//						new FileSimpleValue(ValueType.STRING, fileObject.getPropertyStringValue(Property.PERMISSIONS)
//								)));
				resultSet.add(simplePm);

			}
		}
		System.out.println("end of BuildResultSet " + data.getLength());
		return resultSet;
	}

	public Document stringToDom(String xmlSource) throws RepositoryException {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			builder = factory.newDocumentBuilder();

			return builder.parse(new InputSource(new StringReader(xmlSource)));

		} catch (ParserConfigurationException de) {
			RepositoryException re = new LoginException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		} catch (SAXException de) {
			RepositoryException re = new LoginException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		} catch (IOException de) {
			RepositoryException re = new LoginException(de.getMessage(), de
					.getCause());
			re.setStackTrace(de.getStackTrace());
			throw re;
		}

	}

}
