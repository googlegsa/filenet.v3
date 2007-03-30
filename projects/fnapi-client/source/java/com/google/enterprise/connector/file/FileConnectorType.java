package com.google.enterprise.connector.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;


public class FileConnectorType implements ConnectorType {

	private static final String HIDDEN = "hidden";

	private static final String STARS = "*****";

	private static final String VALUE = "value";

	private static final String NAME = "name";

	private static final String TEXT = "text";

	private static final String TYPE = "type";

	private static final String INPUT = "input";

	private static final String CLOSE_ELEMENT = "/>";

	private static final String OPEN_ELEMENT = "<";

	private static final String PASSWORD = "password";

	private static final String TR_END = "</tr>\r\n";

	private static final String TD_END = "</td>\r\n";

	private static final String TD_START = "<td>";

	private static final String TR_START = "<tr>\r\n";

	private static final String FNCLASS = "objectFactory";

	private static final String FILEPATH = "pathToWcmApiConfig";

	private static Logger logger = null;

	private static HashMap mapError;

	private List keys = null;

	private Set keySet = null;

	private String initialConfigForm = null;
	static {

		mapError = new HashMap();
		mapError
				.put("com.filenet.wcm.api.InvalidCredentialsException",
						"Some required configuration is missing: Please, check the credentials.");
		mapError
				.put("java.io.FileNotFoundException",
						"Some required configuration is missing: The wcmapiconfig file has not been found on the server. Please, check the path to this file.");
		mapError
				.put(
						"com.filenet.wcm.api.RemoteServerException",
						"Some required configuration is missing: Please check the object Store name.");
		mapError
				.put("com.google.enterprise.connector.spi.RepositoryException",
						"Some required configuration is missing: Please check the workplace server url.");
		mapError
		.put("org.apache.commons.httpclient.HttpException",
				"Some required configuration is missing: Please check the workplace server url.");
		
	}
	/**
	 * Set the keys that are required for configuration. One of the overloadings
	 * of this method must be called exactly once before the SPI methods are
	 * used.
	 * 
	 * @param keys
	 *            A list of String keys
	 */
	public void setConfigKeys(List keys) {
		if (this.keys != null) {
			throw new IllegalStateException();
		}
		this.keys = keys;
		this.keySet = new HashSet(keys);
	}

	/**
	 * Set the keys that are required for configuration. One of the overloadings
	 * of this method must be called exactly once before the SPI methods are
	 * used.
	 * 
	 * @param keys
	 *            An array of String keys
	 */
	public void setConfigKeys(String[] keys) {
		setConfigKeys(Arrays.asList(keys));
	}

	/**
	 * Sets the form to be used by this configurer. This is optional. If this
	 * method is used, it must be called before the SPI methods are used.
	 * 
	 * @param formSnippet
	 *            A String snippet of html - see the COnfigurer interface
	 */
	public void setInitialConfigForm(String formSnippet) {
		if (this.initialConfigForm != null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = formSnippet;
	}

	public ConfigureResponse getConfigForm(String language) {

		if (initialConfigForm != null) {
			return new ConfigureResponse("", initialConfigForm);
		}
		if (keys == null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = makeConfigForm(null);

		return new ConfigureResponse("", initialConfigForm);
	}

	public ConfigureResponse getPopulatedConfigForm(Map configMap,
			String language) {

		return null;
	}

	private boolean validateConfigMap(Map configData) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = (String) configData.get(key);
			if (val == null || val.length() == 0) {
				return false;
			}
		}
		return true;
	}

	public ConfigureResponse validateConfig(Map configData, String language) {
		if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL == 1) {
			logger.log(Level.INFO, "File ValidateConfig");
		}
		String form = null;
		if (validateConfigMap(configData)) {
			try {
				if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL == 1) {
					logger.log(Level.INFO, "test connection to the docbase");
				}
				FileSession session = new FileSession(
						"com.google.enterprise.connector.file.filejavawrap.FnObjectFactory",
						(String) configData.get("login"), (String) configData
								.get("password"), (String) configData
								.get("credTag"), (String) configData
								.get("objectStoreName"), (String) configData
								.get("pathToWcmApiConfig"), (String) configData
								.get("displayUrl"), (String) configData
								.get("isPublic"));

				FileAuthenticationManager authentManager = (FileAuthenticationManager) session
						.getAuthenticationManager();
				authentManager.authenticate((String) configData.get("login"),
						(String) configData.get("password"));
				//test on the objectStore name
				session.getQueryTraversalManager();
				
				testWorkplaceUrl((String) configData.get("displayUrl"));
			} catch (RepositoryException e) {
				String returnMessage = "Some required configuration is missing";
				String extractErrorMessage = e.getCause().getClass().getName();
				if (mapError.containsKey(extractErrorMessage)) {
					returnMessage = (String) mapError.get(extractErrorMessage)
							+ " " + e.getMessage();
				}

				return new ConfigureResponse(returnMessage, form);

			}
			return new ConfigureResponse(null, null);
		}
		form = makeValidatedForm(configData);
		return new ConfigureResponse("Some required configuration is missing",
				form);
	}

	private void testWorkplaceUrl(String workplaceServerUrl) throws RepositoryException {
		if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL == 1) {
			logger.log(Level.INFO, "test connection to the webtop server : "
					+ workplaceServerUrl);
		}
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(workplaceServerUrl);
		try {
			int status = client.executeMethod(getMethod);
			if (status != 200) {
				if (FileConnector.DEBUG && FileConnector.DEBUG_LEVEL == 1) {
					logger.log(Level.INFO, "status " + status);
				}
				throw new RepositoryException(
						"status Http request returned a " + status
								+ " status", new HttpException("status is " + status));
			}
		} catch (HttpException e) {
			RepositoryException re = new RepositoryException("HttpException",
					e);
			throw new RepositoryException(re);
		} catch (IOException e) {
			RepositoryException re = new RepositoryException("IOException", e);
			throw new RepositoryException(re);
		}


	}

	private String makeValidatedForm(Map configMap) {
		StringBuffer buf = new StringBuffer(2048);
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			appendStartRow(buf, key);

			String value = (String) configMap.get(key);
			if (value == null) {
				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				if (key.equalsIgnoreCase(PASSWORD)) {
					appendAttribute(buf, TYPE, PASSWORD);
				} else {
					appendAttribute(buf, TYPE, TEXT);
				}
			} else {
				if (key.equalsIgnoreCase(PASSWORD)) {
					buf.append(STARS);
				} else {
					buf.append(value);
				}
				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				appendAttribute(buf, TYPE, HIDDEN);
				appendAttribute(buf, VALUE, value);
			}
			appendAttribute(buf, NAME, key);
			appendEndRow(buf);
		}
		// toss in all the stuff that's in the map but isn't in the keyset
		// taking care to list them in alphabetic order (this is mainly for
		// testability).
		Iterator i = new TreeSet(configMap.keySet()).iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			if (!keySet.contains(key)) {
				// add another hidden field to preserve this data
				String val = (String) configMap.get(key);
				buf.append("<input type=\"hidden\" value=\"");
				buf.append(val);
				buf.append("\" name=\"");
				buf.append(key);
				buf.append("\"/>\r\n");
			}
		}
		return buf.toString();

	}

	/**
	 * Make a config form snippet using the keys (in the supplied order) and, if
	 * passed a non-null config map, pre-filling values in from that map
	 * 
	 * @param configMap
	 * @return config form snippet
	 */
	private String makeConfigForm(Map configMap) {
		StringBuffer buf = new StringBuffer(2048);
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (!key.equals(FNCLASS) && !key.equals(FILEPATH)) {
				appendStartRow(buf, key);
			} else {
				appendStartHiddenRow(buf);
			}
			buf.append(OPEN_ELEMENT);
			buf.append(INPUT);
			if (key.equalsIgnoreCase(PASSWORD)) {
				appendAttribute(buf, TYPE, PASSWORD);
			} else if (key.equals(FNCLASS) || key.equals(FILEPATH)) {
				appendAttribute(buf, TYPE, HIDDEN);
			} else {
				appendAttribute(buf, TYPE, TEXT);
			}

			appendAttribute(buf, NAME, key);

			if (configMap != null) {
				String value = (String) configMap.get(key);
				if (value != null) {
					appendAttribute(buf, VALUE, value);
				}
			}
			appendEndRow(buf);
		}
		return buf.toString();
	}

	private void appendStartHiddenRow(StringBuffer buf) {
		buf.append(TR_START);
		buf.append(TD_START);

	}

	private void appendStartRow(StringBuffer buf, String key) {
		buf.append(TR_START);
		buf.append(TD_START);
		buf.append(key);
		buf.append(TD_END);
		buf.append(TD_START);
	}

	private void appendEndRow(StringBuffer buf) {
		buf.append(CLOSE_ELEMENT);
		buf.append(TD_END);
		buf.append(TR_END);
	}

	private void appendAttribute(StringBuffer buf, String attrName,
			String attrValue) {
		buf.append(" ");
		buf.append(attrName);
		buf.append("=\"");
		buf.append(attrValue);
		buf.append("\"");
	}

}
