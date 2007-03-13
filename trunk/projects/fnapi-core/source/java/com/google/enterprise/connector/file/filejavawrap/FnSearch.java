package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;

public class FnSearch implements ISearch {

	Search search;

	public FnSearch(Search search) {
		this.search = search;
	}

	public String executeXml(String query, IObjectStore objectStore) {
		return search.executeXML(query);
	}
}
