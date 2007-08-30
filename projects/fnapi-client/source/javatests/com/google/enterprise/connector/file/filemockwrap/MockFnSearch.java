package com.google.enterprise.connector.file.filemockwrap;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.google.enterprise.connector.file.filewrap.IObjectStore;
import com.google.enterprise.connector.file.filewrap.ISearch;
import com.google.enterprise.connector.jcr.JcrDocument;
import com.google.enterprise.connector.jcr.JcrDocumentList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockFnSearch implements ISearch {

	protected MockFnSearch() {
		// nothing
	}

	private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT = "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

	private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT = "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= "
			+ "''{0}''] order by @jcr:lastModified, @jcr:uuid";

	public String executeXml(String query, IObjectStore objectStore) {
		MockFnSessionAndObjectStore a = (MockFnSessionAndObjectStore) objectStore;
		MockJcrQueryManager mrQueryMger = new MockJcrQueryManager(a
				.getMockRepositoryDocumentStore());
		Query q;
		try {
			q = mrQueryMger.createQuery(buildConvenientQuery(query), "xpath");
			QueryResult qr = q.execute();
			String result = "<rs:data>";

			JcrDocumentList DocumentListFromJcr = new JcrDocumentList(qr
					.getNodes());
			JcrDocument jcrDocument = null;
			while ((jcrDocument = (JcrDocument) DocumentListFromJcr
					.nextDocument()) != null) {
				result += "\n<z:row Id='"
						+ jcrDocument.findProperty("google:docid").nextValue()
								.toString() + "'/>";
			}
			result += "\n</rs:data>";
			return result;
		} catch (InvalidQueryException e) {
			e.printStackTrace();
		} catch (javax.jcr.RepositoryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {

			e.printStackTrace();
		}
		return null;

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
		// date = date.replaceAll(" ","T");
		if (date == null) {
			return XPATH_QUERY_STRING_UNBOUNDED_DEFAULT;
		} else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
			SimpleDateFormat simpleDateFormat = null;
			Date d1 = null;
			try {
				d1 = df.parse(date);
				simpleDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss'Z'", new Locale("EN"));

			} catch (ParseException e) {

				e.printStackTrace();
			}

			return MessageFormat.format(XPATH_QUERY_STRING_BOUNDED_DEFAULT,
					new Object[] { simpleDateFormat.format(d1) });
		}
	}

	private String extractDate(String query) {
		int lb = query.indexOf(" AND DateLastModified >= ")
				+ " AND DateLastModified >= ".length();
		int ub = "1970-01-01 01:00:00.010".length();
		if (lb != " AND DateLastModified >= ".length() - 1 && ub != -1) {
			return query.substring(lb, ub + lb);
		} else {
			return null;
		}
	}

}
