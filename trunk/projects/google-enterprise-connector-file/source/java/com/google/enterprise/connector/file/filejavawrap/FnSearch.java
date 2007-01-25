package com.google.enterprise.connector.file.filejavawrap;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.file.FileDocumentPropertyMap;
import com.google.enterprise.connector.file.FileResultSet;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class FnSearch implements ISearch {

	Search search;

	public FnSearch(Search search) {
		this.search = search;
	}

	public ResultSet executeXml(String query, IObjectStore objectStore)
			throws RepositoryException {
		FnObjectStore fileObjectStore = (FnObjectStore) objectStore;
		String result = search.executeXML(query);
		Document resultDoc = stringToDom(result);
		ResultSet resultSet = buildResultSetFromDocument(resultDoc,
				fileObjectStore);
		return resultSet;
	}

	private ResultSet buildResultSetFromDocument(Document resultDoc,
			FnObjectStore objectStore) throws RepositoryException {
		System.out.println("in buildResultSetFromDocument");
		FileResultSet resultSet = new FileResultSet();
		FileDocumentPropertyMap fileDocumentPropertyMap = null;

		NodeList data = resultDoc.getElementsByTagName("rs:data").item(0)
				.getChildNodes();
		for (int i = 0; i < data.getLength(); i++) {

			NamedNodeMap nodeMap = data.item(i).getAttributes();

			if (nodeMap != null) {
				for (int j = 0; j < nodeMap.getLength(); j++) {
					if (nodeMap.item(j).getNodeName().equals("Id")) {
						fileDocumentPropertyMap = new FileDocumentPropertyMap(
								(String) nodeMap.item(1).getNodeValue(),
								objectStore);

						resultSet.add(fileDocumentPropertyMap);
					}
				}

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
