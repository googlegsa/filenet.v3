package com.google.enterprise.display;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

public class ContentCrawler extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {

		/*
		 * Load the displayConf.properties to get configuration parameters and
		 * credentials
		 */
		InputStream is = getClass().getResourceAsStream("displayConf.properties");
		Properties config = new Properties();
		config.load(is);

		ContentTransfer contentTransfer = getContent(req.getParameter("vsId"), req.getParameter("objectStoreName"), config.getProperty("username"), config.getProperty("password"), config.getProperty("content_engine_uri"), config.getProperty("wasplocation"));
		res.setContentType(contentTransfer.get_ContentType());
		byte[] content = new byte[contentTransfer.get_ContentSize().intValue()];
		InputStream ip = contentTransfer.accessContentStream();
		ip.read(content);

		res.setHeader("Content-Disposition", "attachment; filename=\""
				+ contentTransfer.get_RetrievalName() + "\"");
		res.getOutputStream().write(content);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		doGet(req, res);
	}

	/**
	 * Get the document content as inputstream.
	 * 
	 * @param docId
	 * @param objectStore
	 * @param username
	 * @param password
	 * @param contentEngURL
	 * @param waspLocation
	 * @return
	 */
	private ContentTransfer getContent(String vsId, String objectStore,
			String username, String password, String contentEngURL,
			String waspLocation) {
		System.setProperty("wasp.location", waspLocation);

		Connection conn = null;
		Domain domain = null;
		conn = Factory.Connection.getConnection(contentEngURL);
		domain = Factory.Domain.getInstance(conn, null);

		UserContext uc = UserContext.get();
		Subject s = UserContext.createSubject(conn, username, password, null);
		uc.pushSubject(s);

		ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectStore, null);
		System.out.println(os.get_Name());

		// VersionSeries vs = (VersionSeries)
		// os.getObject(ClassNames.VERSION_SERIES, new Id(
		// vsId));
		VersionSeries vs = Factory.VersionSeries.fetchInstance(os, new Id(vsId), null);
		Document doc = (Document) vs.get_ReleasedVersion();
		// Document doc = Factory.Document.fetchInstance(os, new Id(docId),
		// null);
		ContentTransfer content = (ContentTransfer) doc.get_ContentElements().get(0);
		return content;
	}
}
