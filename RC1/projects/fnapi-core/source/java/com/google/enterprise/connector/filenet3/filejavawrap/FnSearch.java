package com.google.enterprise.connector.filenet3.filejavawrap;

import com.filenet.wcm.api.Search;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.ISearch;

public class FnSearch implements ISearch {

	Search search;

	public FnSearch(Search refSearch) {
		this.search = refSearch;
	}

	public String executeXml(String query, IObjectStore objectStore) {
		return search.executeXML(query);
	}
}
