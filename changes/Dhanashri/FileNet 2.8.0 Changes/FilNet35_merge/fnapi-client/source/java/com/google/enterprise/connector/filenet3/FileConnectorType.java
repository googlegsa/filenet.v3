/*
 * Copyright 2009 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.filenet3.filewrap.ISearch;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.XmlUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents FileNetConnectortype. Contains methods for creating and validating
 * FileNet user form.
 * 
 * @author amit_kagrawal
 */
public class FileConnectorType implements ConnectorType {

	private static final String HIDDEN = "hidden";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String TEXT = "text";
	private static final String TEXTAREA = "textarea";
	private static final String TYPE = "type";
	private static final String INPUT = "input";
	private static final String CLOSE_TAG = ">";
	private static final String CLOSE_ELEMENT = "/>";
	private static final String OPEN_ELEMENT = "<";
	private static final String PASSWORD = "password";
	private static final String PASSWORD_KEY = "Password";
	private static final String USERNAME = "username";
	private static final String OBJECT_STORE = "object_store";
	private static final String WORKPLACE_URL = "workplace_display_url";
	private static final String DIV_START_LABEL = "<div style='";
	private static final String DIV_END = "</div>\r\n";
	private static final String TR_END = "</tr>\r\n";
	private static final String TD_END = "</td>\r\n";
	private static final String TD_START = "<td>";
	private static final String TD_START_LABEL = "<td style='";
	private static final String TD_END_START_LABEL = "'>";
	private static final String TD_WHITE_SPACE = "white-space: nowrap";
	private static final String TD_DELIMITER = ";";
	private static final String TD_FONT_WEIGHT = "font-weight: bold";
	private static final String ASTERISK = "*";
	private static final String TD_FONT_COLOR = "color: red";
	private static final String TD_TEXT_ALIGN_RIGHT = "text-align: right";
	private static final String TD_FLOAT_LEFT = "float: left";
	private static final String TD_START_COLSPAN = "<td colspan='2'>";
	private static final String TR_START = "<tr>\r\n";
	private static final String TR_START_HIDDEN = "<tr style='display: none'>\r\n";
	private static final String FNCLASS = "object_factory";
	private static final String FILEPATH = "path_to_WcmApiConfig";
	private static final String AUTHENTICATIONTYPE = "authentication_type";
	private static final String WHERECLAUSE = "additional_where_clause";
	private static final String DELETEWHERECLAUSE = "additional_delete_where_clause";
	private static final String LOCALE_FILE = "FileConnectorResources";
	private static final int BUFFER_SIZE = 2048;
	private static final String SELECT = "SELECT";
	private static final String QUERYFORMAT = "SELECT ID,DATELASTMODIFIED FROM ";

	private static Logger LOGGER = Logger.getLogger(FileConnectorType.class.getName());
	private List keys = null;
	private Set keySet = null;
	private String initialConfigForm = null;
	private ResourceBundle resource;
	private String validation = "";

	/**
	 * Set the keys that are required for configuration. One of the overloadings
	 * of this method must be called exactly once before the SPI methods are
	 * used.
	 * 
	 * @param keys A list of String keys
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
	 * @param keys An array of String keys
	 */
	public void setConfigKeys(String[] keys) {
		setConfigKeys(Arrays.asList(keys));
	}

	/**
	 * Sets the form to be used by this configurer. This is optional. If this
	 * method is used, it must be called before the SPI methods are used.
	 * 
	 * @param formSnippet A String snippet of html - see the Configurer
	 *            interface
	 */
	public void setInitialConfigForm(String formSnippet) {
		if (this.initialConfigForm != null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = formSnippet;
	}

	/**
	 * The getConfigForm method creates the configuration form and returns a
	 * ConfigureResponse class object containing the form.
	 * 
	 * @see com.google.enterprise.connector.spi.ConnectorType#getConfigForm(java.
	 *      util.Locale)
	 */

	public ConfigureResponse getConfigForm(Locale language) {

		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE, language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}
		if (initialConfigForm != null) {
			return new ConfigureResponse("", initialConfigForm);
		}
		if (keys == null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = makeConfigForm(null, this.validation);
		return new ConfigureResponse("", initialConfigForm);
	}

	/**
	 * getPopulatedConfigForm method is called when an administrator clicks
	 * Add/Edit for a connector in the Connector Administration > Connectors
	 * page in the Admin Console to display populated form to allow to edit the
	 * connector connector configuration.
	 */

	public ConfigureResponse getPopulatedConfigForm(Map configMap,
			Locale language) {
		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE, language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}
		ConfigureResponse response = new ConfigureResponse("",
				makeConfigForm(configMap, this.validation));
		return response;
	}

	/**
	 * Loops on keys and return a key name only if it finds one with a null or
	 * blank value, unless key.equals(FNCLASS) or key.equals(AUTHENTICATIONTYPE)
	 * or key.equals(WHERECLAUSE) or key.equals(FILEURI)or
	 * key.equals(CHECKMARKING)
	 */
	private String validateConfigMap(Map configData) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = (String) configData.get(key);
			if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
					&& !key.equals(WHERECLAUSE)
					&& !key.equals(DELETEWHERECLAUSE) && !key.equals(FILEPATH)
					&& (val == null || val.length() == 0)) {
				return key;
			}
		}
		return "";
	}

	/**
	 * The validateConfig method ensures that the administrator fills in all
	 * required information. This method also instantiates the connector to
	 * ensure that the connector instance is available for access.
	 */
	public ConfigureResponse validateConfig(Map configData, Locale language,
			ConnectorFactory connectorFactory) {

		LOGGER.log(Level.FINEST, "Entering into function validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory)");
		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE, language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}

		String form = null;
		String validation = validateConfigMap(configData);
		this.validation = validation;
		FileSession session = null;
		if (validation.equals("")) {

			FileConnector conn = null;
			try {
				LOGGER.log(Level.CONFIG, "Start Test Connection to the object store ..."
						+ (String) configData.get(OBJECT_STORE));

				LOGGER.info("Attempting to create FileNet connector instance for verification");
				conn = (FileConnector) connectorFactory.makeConnector(configData);

				if (null != conn) {
					LOGGER.info("Able to create FileNet connector instance. Trying to login with provided credentials.");
					session = (FileSession) conn.login();
				}
			} catch (RepositoryException e) {
				form = makeConfigForm(configData, this.validation);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage());

				return new ConfigureResponse(
						resource.getString("invalid_credentials_error"), form);
			}
			LOGGER.info("Login succeeded. Trying to retrieve the traversal manager.");

			try {
				if (session != null) {
					session.getTraversalManager();// test on the objectStore//
					// name
				}
			} catch (Exception e) {
				this.validation = OBJECT_STORE;

				form = makeConfigForm(configData, this.validation);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage());

				return new ConfigureResponse(
						resource.getString("object_store_invalid"), form);
			}

			LOGGER.log(Level.INFO, "Connecttion to Object Store "
					+ (String) configData.get(OBJECT_STORE) + " is Successful");

			if (null != conn) {
				LOGGER.info("Got traversal manager");
				StringBuffer query = new StringBuffer(
						"<?xml version=\"1.0\" ?><request>");
				query.append("<objectstores mergeoption=\"none\"><objectstore id=\"");
				query.append((String) configData.get(OBJECT_STORE));
				query.append("\"/></objectstores>");
				if (((String) configData.get(WHERECLAUSE)).trim().toUpperCase().startsWith(this.SELECT)) {
					if ((((String) configData.get(WHERECLAUSE)).trim().toUpperCase().startsWith(this.QUERYFORMAT))) {

						query.append("<querystatement>"
								+ ((String) configData.get(WHERECLAUSE)).trim());
					} else {
						this.validation = WHERECLAUSE;
						form = makeConfigForm(configData, this.validation);
						return new ConfigureResponse(
								resource.getString("additional_where_clause_invalid"),
								form);
					}
				} else {
					query.append("<querystatement>SELECT Id,DateLastModified FROM Document WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
					query.append(((String) configData.get(WHERECLAUSE)).trim());
				}

				query.append("</querystatement><options maxrecords='100' objectasid=\"false\"/></request>");

				try {
					if (session != null) {
						ISearch search = session.getSearch();
						search.executeXml(query.toString(), session.getObjectStore());
					}
				} catch (Exception e) {
					this.validation = WHERECLAUSE;
					form = makeConfigForm(configData, this.validation);
					return new ConfigureResponse(
							resource.getString("additional_where_clause_invalid"),
							form);
				}
				StringBuffer deleteQuery = new StringBuffer(
						"<?xml version=\"1.0\" ?><request>");
				deleteQuery.append("<objectstores mergeoption=\"none\"><objectstore id=\"");
				deleteQuery.append((String) configData.get(OBJECT_STORE));
				deleteQuery.append("\"/></objectstores>");

				if (((String) configData.get(DELETEWHERECLAUSE)).trim().toUpperCase().startsWith(this.SELECT)) {
					if ((((String) configData.get(DELETEWHERECLAUSE)).trim().toUpperCase().startsWith(this.QUERYFORMAT))) {
						deleteQuery.append("<querystatement>"
								+ ((String) configData.get(DELETEWHERECLAUSE)).trim());
					} else {
						this.validation = DELETEWHERECLAUSE;
						form = makeConfigForm(configData, this.validation);
						return new ConfigureResponse(
								resource.getString("additional_delete_where_clause_invalid"),
								form);
					}
				} else {
					deleteQuery.append("<querystatement>SELECT Id,DateLastModified FROM Document WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
					deleteQuery.append(((String) configData.get(DELETEWHERECLAUSE)).trim());
				}

				deleteQuery.append("</querystatement><options maxrecords='100' objectasid=\"false\"/></request>");
				try {
					if (session != null) {
						ISearch search = session.getSearch();
						search.executeXml(deleteQuery.toString(), session.getObjectStore());
					}
				} catch (Exception e) {
					this.validation = DELETEWHERECLAUSE;
					form = makeConfigForm(configData, this.validation);
					return new ConfigureResponse(
							resource.getString("additional_delete_where_clause_invalid"),
							form);
				}
			} else {
				LOGGER.severe("Unable to create a FileNet connector instance");
			}

			try {
				LOGGER.log(Level.CONFIG, "Start Test Connection to Workplace server ..."
						+ (String) configData.get(WORKPLACE_URL));
				configData.put(WORKPLACE_URL, getFQDNHostNameURL((String) configData.get(WORKPLACE_URL)));
				testWorkplaceUrl((String) configData.get(WORKPLACE_URL));
			} catch (RepositoryException e) {
				form = makeConfigForm(configData, this.validation);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage());

				return new ConfigureResponse(
						resource.getString("workplace_url_error"), form);
			}
			return null;
		}

		form = makeConfigForm(configData, this.validation);
		String errorMsg = resource.getString(validation + "_error"); // added
		// here
		LOGGER.log(Level.FINEST, "Exiting from function validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory)");
		return new ConfigureResponse(errorMsg, form);
	}

	/**
	 * This method validates WorkPlace URL used to configure connector.
	 * 
	 * @param workplaceServerUrl
	 * @throws RepositoryException
	 */

	private void testWorkplaceUrl(String workplaceServerUrl)
			throws RepositoryException {

		// Added by Pankaj on 04/05/2009 to remove the dependency of
		// Httpclient.jar file
		try {
			new FileUrlValidator().validate(workplaceServerUrl);
			LOGGER.log(Level.INFO, "Connecttion to Workplace server is Successful");
		} catch (FileUrlValidatorException e) {
			this.validation = WORKPLACE_URL;
			LOGGER.log(Level.SEVERE, resource.getString("workplace_url_error"));
			throw new RepositoryException(
					resource.getString("workplace_url_error"));
		} catch (Throwable t) {
			this.validation = WORKPLACE_URL;
			LOGGER.log(Level.SEVERE, resource.getString("workplace_url_error"));
			throw new RepositoryException(
					resource.getString("workplace_url_error"));
		}

	}

	/**
	 * Make a config form snippet using the keys (in the supplied order) and, if
	 * passed a non-null config map, pre-filling values in from that map.
	 * 
	 * @param configMap
	 * @return config form snippet
	 */
	private String makeConfigForm(Map configMap, String validate) {
		LOGGER.log(Level.FINEST, "Entering into function makeConfigForm(Map configMap, String validate)");
		LOGGER.log(Level.FINEST, "validate: " + validate);
		StringBuffer buf = new StringBuffer(BUFFER_SIZE);
		String value = "";
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (configMap != null) {
				value = (String) configMap.get(key);
			}

			if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
			/* && !key.equals(WHERECLAUSE) */&& !key.equals(FILEPATH)) {
				if (validate.equals(key)) {
					LOGGER.log(Level.FINEST, "key: " + key);
					appendStartRow(buf, key, validate);
				} else {
					appendStartRow(buf, key, "");
				}

			} else {
				appendStartHiddenRow(buf);
			}

			buf.append(OPEN_ELEMENT);
			if (key.equals(WHERECLAUSE) || key.equals(DELETEWHERECLAUSE)) {
				buf.append(TEXTAREA);
				appendAttribute(buf, TYPE, TEXTAREA);
				// buf.append(" ");
				appendAttribute(buf, NAME, key);
				// appendAttribute(buf, VALUE, value);
				buf.append(CLOSE_TAG);
				if (value == null) {
					value = "";
				}
				buf.append(value);
				buf.append(OPEN_ELEMENT);
				buf.append("/" + TEXTAREA);
				buf.append(CLOSE_TAG);

			} else {
				buf.append(INPUT);
				if (key.equalsIgnoreCase(PASSWORD_KEY)) {
					appendAttribute(buf, TYPE, PASSWORD);
				} else if (key.equals(FNCLASS)
						|| key.equals(AUTHENTICATIONTYPE)
						/* || key.equals(WHERECLAUSE) */|| key.equals(FILEPATH)) {
					appendAttribute(buf, TYPE, HIDDEN);
				} else {
					appendAttribute(buf, TYPE, TEXT);
				}

				appendAttribute(buf, NAME, key);
				appendAttribute(buf, VALUE, value);
				buf.append(CLOSE_ELEMENT);
			}

			appendEndRow(buf);
			value = "";
		}

		if (configMap != null) {
			appendStartHiddenRow(buf);
			Iterator i = new TreeSet(configMap.keySet()).iterator();

			// added: NULL checks
			if (null != i) {
				while (i.hasNext()) {
					String key = (String) i.next();
					if (!keySet.contains(key)) {
						String val = (String) configMap.get(key);// add another
						// hidden
						// field to
						// preserve
						// this data
						buf.append("<input type=\"hidden\" value=\"");
						buf.append(val);
						buf.append("\" name=\"");
						buf.append(key);
						buf.append("\"/>\r\n");
					}
				}
			}
			appendEndRow(buf);
		}
		LOGGER.log(Level.FINEST, "Exiting from function makeConfigForm(Map configMap, String validate)"
				+ buf.toString());
		return buf.toString();
	}

	/**
	 * To append table row start (TR_START) and table column start (TD_START)
	 * tags to the configuration form for the hidden form elements.
	 * 
	 * @param buf
	 */
	private void appendStartHiddenRow(StringBuffer buf) {
		buf.append(TR_START_HIDDEN);
		buf.append(TD_START);

	}

	/**
	 * To creates a new table row in the configuration form.
	 * 
	 * @param buf
	 * @param key
	 * @param validate
	 */
	private void appendStartRow(StringBuffer buf, String key, String validate) {
		buf.append(TR_START);
		buf.append(TD_START_LABEL);
		buf.append(TD_WHITE_SPACE);
		if (isRequired(key)) {

			buf.append(TD_END_START_LABEL);
			buf.append(DIV_START_LABEL);
			buf.append(TD_FLOAT_LEFT);
			buf.append(TD_DELIMITER);
			if (!validate.equals("")) {
				buf.append(TD_FONT_COLOR);
				buf.append(TD_DELIMITER);
			}
			buf.append(TD_FONT_WEIGHT);
			buf.append(TD_END_START_LABEL);
			buf.append(resource.getString(key));
			buf.append(DIV_END);

			buf.append(DIV_START_LABEL);
			buf.append(TD_TEXT_ALIGN_RIGHT);
			buf.append(TD_DELIMITER);
			buf.append(TD_FONT_WEIGHT);
			buf.append(TD_DELIMITER);
			buf.append(TD_FONT_COLOR);
			buf.append(TD_END_START_LABEL);
			buf.append(ASTERISK);
			buf.append(DIV_END);
			buf.append(TD_END);
		} else {
			buf.append(TD_END_START_LABEL);
			if (key.equals(WHERECLAUSE)) {
				buf.append(DIV_START_LABEL);
				buf.append(TD_FLOAT_LEFT);
				buf.append(TD_DELIMITER);
				if (!validate.equals("")) {
					buf.append(TD_FONT_COLOR);
					buf.append(TD_DELIMITER);
					buf.append(TD_FONT_WEIGHT);
				}
				buf.append(TD_END_START_LABEL);
				buf.append(resource.getString(key));
				buf.append(DIV_END);
				buf.append(TD_END);
			} else if (key.equals(DELETEWHERECLAUSE)) {
				buf.append(DIV_START_LABEL);
				buf.append(TD_FLOAT_LEFT);
				buf.append(TD_DELIMITER);
				if (!validate.equals("")) {
					buf.append(TD_FONT_COLOR);
					buf.append(TD_DELIMITER);
					buf.append(TD_FONT_WEIGHT);
				}
				buf.append(TD_END_START_LABEL);
				buf.append(resource.getString(key));
				buf.append(DIV_END);
				buf.append(TD_END);
			} else {
				buf.append(resource.getString(key));
				buf.append(TD_END);
			}
		}
		buf.append(TD_START);
	}

	/**
	 * To append close element (CLOSE_ELEMENT), table column end (TD_END), and
	 * table row end (TR_END) tags to the current table row.
	 * 
	 * @param buf
	 */
	private void appendEndRow(StringBuffer buf) {
		buf.append(TD_END);
		buf.append(TR_END);
	}

	/**
	 * To append an attribute to the connector configuration form.
	 * 
	 * @param buf
	 * @param attrName
	 * @param attrValue
	 */
	private void appendAttribute(StringBuffer buf, String attrName,
			String attrValue) {
		buf.append(" ");

		if (attrName == TYPE && attrValue == TEXTAREA) {
			buf.append(" cols=\"50\"");
			buf.append(" rows=\"5\"");
		} else {
			buf.append(attrName);
			buf.append("=\"");
			try {
				// XML-encode the special characters (< > " etc.)
				// Check the basic requirement mentioned in ConnectorType as
				// part of
				// CM-Issue 186
				XmlUtils.xmlAppendAttrValue(attrValue, buf);
			} catch (IOException e) {
				String msg = new StringBuffer(
						"Exceptions while constructing the config form for attribute : ").append(attrName).append(" with value : ").append(attrValue).toString();
				LOGGER.log(Level.WARNING, msg, e);
			}
			buf.append("\"");
		}
		if (attrName == TYPE && attrValue == TEXT) {
			buf.append(" size=\"50\"");
		}

	}

	private boolean isRequired(final String configKey) {
		final boolean bValue = false;
		if (configKey.equals(OBJECT_STORE) || configKey.equals(WORKPLACE_URL)
				|| configKey.equals(PASSWORD_KEY) || configKey.equals(USERNAME)) {
			return true;
		}
		return bValue;
	}

	private String getFQDNHostNameURL(String strUrl) {

		InetAddress ia = null;
		URL url = null;
		try {
			url = new URL(strUrl);
			ia = InetAddress.getByName(url.getHost());
		} catch (final UnknownHostException e) {
			LOGGER.log(Level.WARNING, "Exception occurred while converting to FQDN.", e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "URL is not in a correct format.", e);
		}
		if (ia != null && url != null) {
			return strUrl = strUrl.replaceAll(url.getHost(), ia.getCanonicalHostName());
		}

		return strUrl;
	}

}
