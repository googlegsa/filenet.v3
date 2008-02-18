package com.google.enterprise.connector.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileConnectorType implements ConnectorType {

	private static final String HIDDEN = "hidden";

	private static final String VALUE = "value";

	private static final String NAME = "name";

	private static final String TEXT = "text";

	private static final String TYPE = "type";

	private static final String INPUT = "input";

	private static final String CLOSE_ELEMENT = "/>";

	private static final String OPEN_ELEMENT = "<";

	private static final String PASSWORD = "Password";

	private static final String TR_END = "</tr>\r\n";

	private static final String TD_END = "</td>\r\n";

	private static final String TD_START = "<td>";

	private static final String TR_START = "<tr>\r\n";

	private static final String FNCLASS = "object_factory";

	private static final String FILEPATH = "path_to_WcmApiConfig";

	private static final String AUTHENTICATIONTYPE = "authentication_type";

	private static final String WHERECLAUSE = "additional_where_clause";

	private static final String ISPUBLIC = "is_public";

	private static final String CHECKBOX = "CHECKBOX";

	private static final String CHECKED = "CHECKED";

	private static Logger logger = null;

	private List keys = null;

	private Set keySet = null;

	private String initialConfigForm = null;

	private ResourceBundle resource;

	static {
		logger = Logger.getLogger(FileConnectorType.class.getName());
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
	 *            A String snippet of html - see the Configurer interface
	 */
	public void setInitialConfigForm(String formSnippet) {
		if (this.initialConfigForm != null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = formSnippet;
	}

	public ConfigureResponse getConfigForm(Locale language) {

		try {
			resource = ResourceBundle.getBundle("FileConnectorResources",
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}
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
			Locale language) {
		try {
			resource = ResourceBundle.getBundle("FileConnectorResources",
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}
		ConfigureResponse response = new ConfigureResponse("",
				makeConfigForm(configMap));
		return response;
	}

	private String validateConfigMap(Map configData) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = (String) configData.get(key);
			if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
					&& !key.equals(WHERECLAUSE) && !key.equals(FILEPATH)
					&& !key.equals(ISPUBLIC)
					&& (val == null || val.length() == 0)) {

				return key;
			}
		}
		return "";
	}

	public ConfigureResponse validateConfig(Map configData, Locale language,
			ConnectorFactory connectorFactory) {
		try {
			resource = ResourceBundle.getBundle("FileConnectorResources",
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}
		String form = null;
		String validation = validateConfigMap(configData);
		FileSession session;
		if (validation.equals("")) {
			try {
				logger.log(Level.INFO, "test connection to the object store "
						+ (String) configData.get("object_store"));

				Properties p = new Properties();
				p.putAll(configData);
				String isPublic = (String) configData.get(ISPUBLIC);
				if (isPublic == null) {
					p.put(ISPUBLIC, "false");
				}
				Resource res = new ClassPathResource(
						"config/connectorInstance.xml");

				XmlBeanFactory factory = new XmlBeanFactory(res);
				PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
				cfg.setProperties(p);
				cfg.postProcessBeanFactory(factory);
				FileConnector conn = (FileConnector) factory
						.getBean("FileConnectorInstance");
				session = (FileSession) conn.login();
				// test on the objectStore name
				session.getTraversalManager();
				logger.log(Level.INFO, "test connection to Workplace server "
						+ (String) configData.get("workplace_display_url"));
				testWorkplaceUrl((String) configData
						.get("workplace_display_url"));
			} catch (RepositoryException e) {
				String extractErrorMessage = e.getCause().getClass().getName();
				String bundleMessage;
				try {
					bundleMessage = resource.getString(extractErrorMessage);
				} catch (MissingResourceException mre) {
					bundleMessage = resource.getString("DEFAULT_ERROR_MESSAGE")
							+ " " + e.getMessage();
				}
				form = makeConfigForm(configData);
				logger.severe(e.getMessage() + " "
						+ e.getCause().getClass().getName());
				e.printStackTrace();
				return new ConfigureResponse("<p><font color=\"#FF0000\">"
						+ bundleMessage + "</font></p>",
						"<p><font color=\"#FF0000\">" + bundleMessage
								+ "</font></p>" + "<br>" + form);

			}
			return null;
		}
		form = makeConfigForm(configData);
		return new ConfigureResponse(resource.getString(validation + "_error"),
				"<p><font color=\"#FF0000\">"
						+ resource.getString(validation + "_error")
						+ "</font></p>" + "<br>" + form);
	}

	private void testWorkplaceUrl(String workplaceServerUrl)
			throws RepositoryException {
		logger.log(Level.INFO, "test connection to the workplace server : "
				+ workplaceServerUrl);

		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(workplaceServerUrl);
		try {
			int status = client.executeMethod(getMethod);
			if (status != 200) {
				logger.log(Level.INFO, "status " + status);

				throw new RepositoryException("status Http request returned a "
						+ status + " status", new HttpException("status is "
						+ status));
			}
		} catch (HttpException e) {
			RepositoryException re = new RepositoryException("HttpException", e);
			throw new RepositoryException(re);
		} catch (IOException e) {
			RepositoryException re = new RepositoryException("IOException", e);
			throw new RepositoryException(re);
		}

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
				appendEndRow(buf);
				value = "";
			} else {
				if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
						&& !key.equals(WHERECLAUSE) && !key.equals(FILEPATH)) {
					appendStartRow(buf, resource.getString(key));
				} else {
					appendStartHiddenRow(buf);
				}

				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				if (key.equalsIgnoreCase(PASSWORD)) {
					appendAttribute(buf, TYPE, PASSWORD);
				} else if (key.equals(FNCLASS)
						|| key.equals(AUTHENTICATIONTYPE)
						|| key.equals(WHERECLAUSE) || key.equals(FILEPATH)) {
					appendAttribute(buf, TYPE, HIDDEN);
				} else {
					appendAttribute(buf, TYPE, TEXT);
				}

				appendAttribute(buf, NAME, key);
				appendAttribute(buf, VALUE, value);
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
		if (attrName == TYPE && attrValue == TEXT) {
			buf.append(" size=\"50\"");
		}
	}

	private void appendCheckBox(StringBuffer buf, String key, String label,
			String value) {
		buf.append(TR_START);
		buf.append(TD_START);
		buf.append(OPEN_ELEMENT);
		buf.append(INPUT);
		buf.append(" " + TYPE + "=" + CHECKBOX);
		buf.append(" " + NAME + "=\"" + key + "\" ");
		if (value != null && value.equals("on")) {
			buf.append(CHECKED);
		}
		buf.append(CLOSE_ELEMENT);
		buf.append(label + TD_END);

		buf.append(TR_END);

	}

}
