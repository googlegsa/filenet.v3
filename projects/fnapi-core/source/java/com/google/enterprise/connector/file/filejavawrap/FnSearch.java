package com.google.enterprise.connector.file.filejavawrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.file.FileTraversalManager;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;

public class FnSearch implements ISearch {

	Search search;

	private static Logger logger;

	{
		logger = Logger.getLogger(FileTraversalManager.class.getName());
		logger.setLevel(Level.ALL);
	}

	public FnSearch(Search search) {
		this.search = search;
	}

	public String executeXml(String query, IObjectStore objectStore) {
		return search.executeXML(query);
	}
}
