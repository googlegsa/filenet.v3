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

package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.filenet4.filewrap.ISearch;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.XmlUtils;

import org.idoox.util.RuntimeWrappedException;
import org.idoox.wasp.UnknownProtocolException;
import org.idoox.xmlrpc.MessageCreatingException;

import com.filenet.api.exception.EngineRuntimeException;

import java.io.IOException;
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

import javax.net.ssl.SSLHandshakeException;

/**
 *Represents FileNet connector type information. Contains methods for creating
 * and validating user form.
 * 
 * @author pankaj_chouhan
 **/
public class FileConnectorType implements ConnectorType {

	private static final String HIDDEN = "hidden";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String TEXT = "text";
	private static final String TYPE = "type";
	private static final String INPUT = "input";
	private static final String CLOSE_ELEMENT = "/>";
	private static final String OPEN_ELEMENT = "<";
	private static final String PASSWORD = "password";
	private static final String PASSWORD_KEY = "Password";
	private static final String USERNAME = "username";
	private static final String OBJECT_STORE = "object_store";
	private static final String WORKPLACE_URL = "workplace_display_url";
	private static final String CONTENT_ENGINE_URL = "content_engine_url";
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
	private static final String ISPUBLIC = "is_public";
	private static final String CHECKMARKING = "check_marking";
	private static final String CHECKBOX = "checkbox";
	private static final String CHECKED = "checked='checked'";
	private static final String MARKINGCHECKBOX = "checkbox";
	private static final String MARKINGCHECKED = "checked='checked'";
	private static final String LOCALE_FILE = "FileConnectorResources";
	private static Logger logger = null;
	private List keys = null;
	private Set keySet = null;
	private String initialConfigForm = null;
	private ResourceBundle resource;
	private String validation = "";

	static {
		logger = Logger.getLogger(FileConnectorType.class.getName());
	}

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

	public ConfigureResponse getConfigForm(Locale language) {

		try {
			logger.info("language used " + language.getLanguage());
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
	 * or key.equals(WHERECLAUSE) or key.equals(FILEURI) or key.equals(ISPUBLIC)
	 */
	private String validateConfigMap(Map configData) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = (String) configData.get(key);
			// TODO remove unrelevant FILEURI
			if (!key.equals(FNCLASS)
					&& !key.equals(AUTHENTICATIONTYPE)
					&& !key.equals(WHERECLAUSE) // && !key.equals(FILEURI)
					&& !key.equals(ISPUBLIC) && !key.equals(CHECKMARKING)
					&& (val == null || val.length() == 0)) {

				return key;
			}
		}
		return "";
	}

	public ConfigureResponse validateConfig(Map configData, Locale language,
			ConnectorFactory connectorFactory) {
		logger.log(Level.FINEST, "Entering into function validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory)");
		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE, language);
		} catch (MissingResourceException e) {
			logger.log(Level.SEVERE, "Unable to find the resource bundle file for language "
					+ language, e);
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}

		if (configData == null) {
			logger.severe("No configuration information is available");
			return null;
		}

		String form = null;

		logger.info("validating the configuration data...");
		String validation = validateConfigMap(configData);
		this.validation = validation;

		logger.info("Configuration data validation.. succeeded");

		FileSession session = null;
		if (validation.equals("")) {
			try {
				logger.info("Attempting to create FileNet4 connector instance");
				configData.put(CONTENT_ENGINE_URL, rightTrim((String) configData.get(CONTENT_ENGINE_URL), '/'));// Removing
				// the
				// extra
				// slashes
				// at
				// the
				// right
				// end
				// of
				// content
				// engine
				// url

				FileConnector conn = (FileConnector) connectorFactory.makeConnector(configData);
				if (null == conn) {
					logger.severe("Unable to establish connection with FileNet server");
					return null;
				}

				logger.info("FileNet4 connector instance creation succeeded. Trying to Login into FileNet server.");
				session = (FileSession) conn.login();

				if (session != null) {
					logger.log(Level.INFO, "Connection to Content Engine URL is Successful");
					session.getTraversalManager();// test on the objectStore
					// name
					logger.log(Level.INFO, "Connection to Object Store "
							+ (String) configData.get("object_store")
							+ " is Successful");
				} else {
					logger.log(Level.INFO, "Connection to Content Engine URL Failed");
				}

				testWorkplaceUrl((String) configData.get("workplace_display_url"));

				StringBuffer query = new StringBuffer();
				query.append("SELECT TOP 1 Id, DateLastModified FROM Document WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
				query.append((String) configData.get(WHERECLAUSE));

				try {
					if (session != null) {
						ISearch search = session.getSearch();
						search.execute(query.toString());
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
					this.validation = WHERECLAUSE;
					form = makeConfigForm(configData, this.validation);
					return new ConfigureResponse(
							resource.getString("additional_where_clause_invalid"),
							form);
				}
			} catch (EngineRuntimeException e) {
				String errorKey = e.getExceptionCode().getKey();
				String bundleMessage;
				try {
					if (errorKey.equalsIgnoreCase("E_NULL_OR_INVALID_PARAM_VALUE")) {
						bundleMessage = resource.getString("content_engine_url_invalid")
								+ " " + e.getLocalizedMessage();
						logger.log(Level.SEVERE, bundleMessage, e);
					} else {
						// bundleMessage =
						// resource.getString("required_field_error") +" "+
						// e.getLocalizedMessage();
						bundleMessage = e.getLocalizedMessage();
						logger.log(Level.SEVERE, bundleMessage, e);
					}
				} catch (MissingResourceException mre) {
					// bundleMessage =
					// resource.getString("required_field_error") +" "+
					// e.getLocalizedMessage();
					bundleMessage = e.getLocalizedMessage();
					logger.log(Level.SEVERE, bundleMessage, mre);
				}
				form = makeConfigForm(configData, validation);
				return new ConfigureResponse(bundleMessage, form);
			} catch (RepositoryException e) {
				String bundleMessage;
				try {
					if (e.getCause() instanceof EngineRuntimeException) {
						EngineRuntimeException ere = (EngineRuntimeException) e.getCause();
						String errorKey = ere.getExceptionCode().getKey();
						if (errorKey.equalsIgnoreCase("E_OBJECT_NOT_FOUND")) {
							bundleMessage = resource.getString("object_store_invalid")
									+ " " + ere.getLocalizedMessage();
						} else if (errorKey.equalsIgnoreCase("E_NOT_AUTHENTICATED")) {
							bundleMessage = resource.getString("invalid_credentials_error")
									+ " " + ere.getLocalizedMessage();
						} else if (errorKey.equalsIgnoreCase("E_UNEXPECTED_EXCEPTION")) {
							String errorMsg = ere.getCause().getClass().getName();
							if (ere.getCause() instanceof NoClassDefFoundError) {
								NoClassDefFoundError ncdf = (NoClassDefFoundError) ere.getCause();
								errorMsg = ncdf.getMessage();
								if (errorMsg.indexOf("activation") != -1) {
									bundleMessage = resource.getString("activation_jar_error");
								} else {
									bundleMessage = resource.getString("content_engine_url_invalid");
								}
							} else if (ere.getCause() instanceof RuntimeWrappedException) {
								errorMsg = ere.getCause().getMessage();
								if (errorMsg.indexOf("Jetty") != -1) {
									bundleMessage = resource.getString("jetty_jar_error");
								} else if (ere.getCause() != null
										&& ere.getCause().getCause() != null
										&& ere.getCause().getCause().getCause() != null
										&& ere.getCause().getCause().getCause() instanceof SSLHandshakeException) {
									bundleMessage = resource.getString("content_engine_url_invalid");
								} else if (ere.getCause() != null
										&& ere.getCause().getCause() != null
										&& ere.getCause().getCause().getCause() != null
										&& ere.getCause().getCause().getCause() instanceof MessageCreatingException) {
									bundleMessage = resource.getString("builtin_serialization_jar_error");
								} else {
									bundleMessage = ere.getLocalizedMessage();
								}
							} else if (ere.getCause() instanceof ExceptionInInitializerError) {
								bundleMessage = resource.getString("jaxrpc_jar_error");
							} else if (ere.getCause() instanceof UnknownProtocolException) {
								bundleMessage = resource.getString("saaj_jar_error");
							} else {
								bundleMessage = resource.getString("content_engine_url_invalid");
							}
						} else if (errorKey.equalsIgnoreCase("API_INVALID_URI")) {
							bundleMessage = resource.getString("content_engine_url_invalid");
						} else if (errorKey.equalsIgnoreCase("TRANSPORT_WSI_LOOKUP_FAILURE")) {
							bundleMessage = resource.getString("wsdl_api_jar_error");
						} else {
							bundleMessage = resource.getString("required_field_error")
									+ " " + e.getLocalizedMessage();
						}
					} else {
						bundleMessage = e.getLocalizedMessage();
					}
					logger.log(Level.SEVERE, bundleMessage, e);
				} catch (MissingResourceException mre) {
					bundleMessage = resource.getString("required_field_error")
							+ " " + e.getLocalizedMessage();
					// logger.severe(bundleMessage);
					logger.log(Level.SEVERE, bundleMessage, mre);
				} catch (NullPointerException npe) {
					// bundleMessage =
					// resource.getString("required_field_error") +" "+
					// e.getMessage();
					bundleMessage = npe.getLocalizedMessage();
					logger.log(Level.SEVERE, "Unable to connect to FileNet server. Got exception: ", npe);
				} catch (Throwable th) {
					bundleMessage = th.getLocalizedMessage();
					logger.log(Level.SEVERE, "Unable to connect to FileNet server. Got exception: ", th);
				}

				logger.info("request to make configuration form..");
				form = makeConfigForm(configData, validation);
				return new ConfigureResponse(bundleMessage, form);
			}
			return null;
		}
		form = makeConfigForm(configData, validation);
		return new ConfigureResponse(resource.getString(validation + "_error"),
				form);
	}

	private void testWorkplaceUrl(String workplaceServerUrl)
			throws RepositoryException {
		// Added by Pankaj on 04/05/2009 to remove the dependency of
		// Httpclient.jar file
		try {
			new FileUrlValidator().validate(workplaceServerUrl);
			logger.log(Level.INFO, "Connection to Workplace URL is Successful");
		} catch (FileUrlValidatorException e) {
			logger.log(Level.WARNING, resource.getString("workplace_url_error"));
			throw new RepositoryException(
					resource.getString("workplace_url_error"), e);
		} catch (Throwable t) {
			logger.log(Level.WARNING, resource.getString("workplace_url_error"));
			throw new RepositoryException(
					resource.getString("workplace_url_error"), t);
		}
	}

	/**
	 * Make a config form snippet using the keys (in the supplied order) and, if
	 * passed a non-null config map, pre-filling values in from that map
	 * 
	 * @param configMap
	 * @return config form snippet
	 */
	private String makeConfigForm(Map configMap, String validate) {
		StringBuffer buf = new StringBuffer(2048);
		String value = "";
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (configMap != null) {
				value = (String) configMap.get(key);
			}
			if (key.equals(ISPUBLIC)) {
				appendCheckBox(buf, key, resource.getString(key), value);
				appendStartHiddenRow(buf);
				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				appendAttribute(buf, TYPE, HIDDEN);
				appendAttribute(buf, VALUE, "false");
				appendAttribute(buf, NAME, key);
				buf.append(CLOSE_ELEMENT);
				appendEndRow(buf);
				value = "";
			} else if (key.equals(CHECKMARKING)) {
				appendMarkingCheckBox(buf, key, resource.getString(key), value);
				appendStartHiddenRow(buf);
				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				appendAttribute(buf, TYPE, HIDDEN);
				appendAttribute(buf, VALUE, "false");
				appendAttribute(buf, NAME, key);
				buf.append(CLOSE_ELEMENT);
				appendEndRow(buf);
				value = "";
			} else {
				if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
				/* && !key.equals(WHERECLAUSE) */&& !key.equals(FILEPATH)) {
					if (validate.equals(key)) {
						appendStartRow(buf, key, validate);
					} else {
						appendStartRow(buf, key, "");
					}

				} else {
					appendStartHiddenRow(buf);
				}

				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				if (key.equalsIgnoreCase(PASSWORD_KEY)) {
					appendAttribute(buf, TYPE, PASSWORD);
				} else if (key.equals(FNCLASS)
						|| key.equals(AUTHENTICATIONTYPE)
				// || key.equals(WHERECLAUSE)
				// || key.equals(FILEURI)
				) {
					appendAttribute(buf, TYPE, HIDDEN);
				} else {
					appendAttribute(buf, TYPE, TEXT);
				}

				appendAttribute(buf, NAME, key);
				appendAttribute(buf, VALUE, value);
				buf.append(CLOSE_ELEMENT);
				appendEndRow(buf);
				value = "";
			}
		}
		if (configMap != null) {
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
		}
		return buf.toString();
	}

	private void appendStartHiddenRow(StringBuffer buf) {
		// buf.append(TR_START);
		buf.append(TR_START_HIDDEN);
		buf.append(TD_START);

	}

	private void appendStartRow(StringBuffer buf, String key, String validate) {
		buf.append(TR_START);
		// buf.append(TD_START);
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
			buf.append(resource.getString(key));
			buf.append(TD_END);
		}
		buf.append(TD_START);
	}

	private void appendEndRow(StringBuffer buf) {
		// buf.append(CLOSE_ELEMENT);
		buf.append(TD_END);
		buf.append(TR_END);
	}

	private void appendAttribute(StringBuffer buf, String attrName,
			String attrValue) {
		buf.append(" ");
		buf.append(attrName);
		buf.append("=\"");
		// buf.append(attrValue);
		try {
			// XML-encode the special characters (< > " etc.)
			// Check the basic requirement mentioned in ConnectorType as part of
			// CM-Issue 186
			XmlUtils.xmlAppendAttrValue(attrValue, buf);
		} catch (IOException e) {
			String msg = new StringBuffer(
					"Exceptions while constructing the config form for attribute : ").append(attrName).append(" with value : ").append(attrValue).toString();
			logger.log(Level.WARNING, msg, e);
		}
		buf.append("\"");
		if (attrName == TYPE && attrValue == TEXT) {
			buf.append(" size=\"50\"");
		}
	}

	private void appendCheckBox(StringBuffer buf, String key, String label,
			String value) {
		buf.append(TR_START);
		// buf.append(TD_START);
		buf.append(TD_START_COLSPAN);
		buf.append(OPEN_ELEMENT);
		buf.append(INPUT);
		buf.append(" " + TYPE + "=\"" + CHECKBOX + '"');
		buf.append(" " + NAME + "=\"" + key + "\" ");
		if (value != null && value.equals("on")) {
			buf.append(CHECKED);
		}
		buf.append(CLOSE_ELEMENT);
		buf.append(label + TD_END);

		buf.append(TR_END);

	}

	private void appendMarkingCheckBox(StringBuffer buf, String key,
			String label, String value) {
		buf.append(TR_START);
		// buf.append(TD_START);
		buf.append(TD_START_COLSPAN);
		buf.append(OPEN_ELEMENT);
		buf.append(INPUT);
		buf.append(" " + TYPE + "=\"" + MARKINGCHECKBOX + '"');
		buf.append(" " + NAME + "=\"" + key + "\" ");
		if (value != null && value.equals("on")) {
			buf.append(MARKINGCHECKED);
		}
		buf.append(CLOSE_ELEMENT);
		buf.append(label + TD_END);

		buf.append(TR_END);

	}

	private boolean isRequired(final String configKey) {
		final boolean bValue = false;
		if (configKey.equals(OBJECT_STORE) || configKey.equals(WORKPLACE_URL)
				|| configKey.equals(PASSWORD_KEY) || configKey.equals(USERNAME)
				|| configKey.equals(CONTENT_ENGINE_URL)) {
			return true;
		}
		return bValue;
	}

	private String rightTrim(String strTarget, char separator) {
		String regex = separator + "+";
		strTarget = strTarget.replaceAll(regex, "/");
		strTarget = strTarget.replaceFirst(":/", "://");
		return strTarget;
	}
}