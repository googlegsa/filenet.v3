package com.google.enterprise.connector.file.filemockwrap;

import java.text.MessageFormat;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.jcradaptor.SpiResultSetFromJcr;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class MockFnSearch implements ISearch {

	protected MockFnSearch() {
		// nothing
	}

	private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT = "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

	private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT = "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= "
			+ "''{0}''] order by @jcr:lastModified, @jcr:uuid";

	public ResultSet executeXml(String query, IObjectStore objectStore)
			throws RepositoryException {
		MockFnSessionAndObjectStore a = null;
		a = (MockFnSessionAndObjectStore) objectStore;
		MockJcrQueryManager mrQueryMger = new MockJcrQueryManager(a
				.getMockRepositoryDocumentStore());
		Query q;
		try {
			q = mrQueryMger.createQuery(buildConvenientQuery(query), "xpath");
			QueryResult qr = q.execute();
			return new SpiResultSetFromJcr(qr.getNodes());
		} catch (InvalidQueryException e) {
			throw new RepositoryException(e);
		} catch (javax.jcr.RepositoryException e) {
			throw new RepositoryException(e);
		}

	}

	/**
	 * Mock deals with two queries only: Unbounded and bounded This class aims
	 * to construc the right one according to the FNet query it got from the QTM
	 * 
	 * @param query
	 * @return
	 */
	private String buildConvenientQuery(String query) {
		String date = extractDate(query);
		if (date == null) {
			return XPATH_QUERY_STRING_UNBOUNDED_DEFAULT;
		} else {
			return MessageFormat.format(XPATH_QUERY_STRING_BOUNDED_DEFAULT,
					new Object[] { date });
		}
	}

	private String extractDate(String query) {
		int lb = query.indexOf(" AND DateLastModified > ")
				+ " AND DateLastModified > ".length();
		int ub = query.indexOf("ORDER BY DateLastModified");
		if (lb != " AND DateLastModified > ".length() - 1 && ub != -1) {
			return query.substring(lb, ub);
		} else {
			return null;
		}
	}

}
