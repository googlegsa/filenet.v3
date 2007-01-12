package com.google.enterprise.connector.file.filejavawrap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.wcm.api.BaseObject;
import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Property;
import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.file.FileResultSet;
import com.google.enterprise.connector.file.FileSimpleProperty;
import com.google.enterprise.connector.file.FileSimpleValue;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

public class IFileSearch implements ISearch {

	Search search;

	public IFileSearch(Search search) {
		this.search = search;
	}

	public ResultSet executeXml(String query, IObjectStore objectStore)
			throws RepositoryException {
		IFileObjectStore fileObjectStore = (IFileObjectStore) objectStore;
		String result = search.executeXML(query);
		Document resultDoc = stringToDom(result);
		ResultSet resultSet = buildResultSetFromDocument(resultDoc, fileObjectStore);
		return resultSet;
	}

	private ResultSet buildResultSetFromDocument(Document resultDoc,
			IFileObjectStore objectStore) throws RepositoryException {
		FileResultSet resultSet = new FileResultSet();
		SimplePropertyMap simplePm;
		NodeList data = resultDoc.getElementsByTagName("rs:data").item(0)
				.getChildNodes();
		for (int i = 0; i < data.getLength(); i++) {
			simplePm = new SimplePropertyMap();
			NamedNodeMap nodeMap = data.item(i).getAttributes();
			if (nodeMap != null) {
				for (int j = 0; j < nodeMap.getLength(); j++) {
					

					if (nodeMap.item(j).getNodeName().equals("Id")) {
						simplePm.putProperty(new FileSimpleProperty(
								SpiConstants.PROPNAME_DOCID, new SimpleValue(
										ValueType.STRING, nodeMap.item(j)
												.getNodeValue())));
						simplePm
								.putProperty(new FileSimpleProperty(
										SpiConstants.PROPNAME_DISPLAYURL,
										new SimpleValue(
												ValueType.STRING,
												objectStore.getDisplayUrl()+ nodeMap.item(j)
																.getNodeValue())));
						IFileDocument fileObject = (IFileDocument) objectStore
								.getObject(BaseObject.TYPE_DOCUMENT, nodeMap
										.item(j).getNodeValue());
						simplePm
								.putProperty(new FileSimpleProperty(
										SpiConstants.PROPNAME_MIMETYPE,
										new SimpleValue(
												ValueType.STRING,
												fileObject
														.getPropertyStringValue(Property.MIME_TYPE))));
						simplePm.putProperty(new FileSimpleProperty(
								SpiConstants.PROPNAME_CONTENT,
								new FileSimpleValue(ValueType.BINARY,
										fileObject)));
						simplePm.putProperty(new FileSimpleProperty(
								SpiConstants.PROPNAME_SECURITYTOKEN,
								new FileSimpleValue(ValueType.STRING,
										fileObject.getPermissionsXML())));
						
					}else if (nodeMap.item(j).getNodeName().equals("DateLastModified")) {
						
							simplePm
									.putProperty(new FileSimpleProperty(
											SpiConstants.PROPNAME_LASTMODIFY,
											new FileSimpleValue(
													ValueType.DATE,
													nodeMap.item(j)
													.getNodeValue())));
						
					}

				}
				resultSet.add(simplePm);
			}
		}
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
