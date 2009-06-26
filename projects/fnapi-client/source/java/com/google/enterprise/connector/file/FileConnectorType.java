package com.google.enterprise.connector.file;

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

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Represents FileNetConnectortype. Contains methods for creating and validating FileNet user form.
 * @author amit_kagrawal
 * */
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
	private static final String CHECKBOX = "checkbox";
	private static final String CHECKED = "checked='checked'";
	private static final String LOCALE_FILE = "FileConnectorResources";
//	private static final String CONNECTOR_INSTANCE_XML = "config/connectorInstance.xml";
//	private static final String FILE_CONNECTOR_INSTANCE = "FileConnectorInstance";
	private static final int BUFFER_SIZE = 2048;
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
			resource = ResourceBundle.getBundle(LOCALE_FILE,
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}
		if (initialConfigForm != null) {
			return new ConfigureResponse("", initialConfigForm);
		}
		if (keys == null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = makeConfigForm(null,this.validation);
		return new ConfigureResponse("", initialConfigForm);
	}

	public ConfigureResponse getPopulatedConfigForm(Map configMap,
			Locale language) {
		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE,
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}
		ConfigureResponse response = new ConfigureResponse("",
				makeConfigForm(configMap,this.validation));
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

	public ConfigureResponse validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory) {

		logger.log(Level.FINEST, "Entering into function validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory)");
		try {
			resource = ResourceBundle.getBundle(LOCALE_FILE,
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle(LOCALE_FILE);
		}

		String form = null;
		String validation = validateConfigMap(configData);
		this.validation = validation;
		FileSession session;
		if (validation.equals("")) {
			try {
				logger.log(Level.CONFIG, "Start Test Connection to the object store ..."+ (String) configData.get(OBJECT_STORE));

				logger.info("attempting to create FileNet connector instance for verification");
				FileConnector conn =(FileConnector) connectorFactory.makeConnector(configData);

				if(null!=conn){
					logger.info("able to craete FileNet connector instance. Trying to login with provided credentials.");
					session = (FileSession) conn.login();
					logger.info("login succeeded. Trying to retrieve the traversal manager.");
					session.getTraversalManager();// test on the objectStore name
					logger.info("got traversal manager");
				}else{
					logger.severe("Unable to craete a FileNet connector instance");
				}

				logger.log(Level.INFO, "Connecttion to Object Store "+ (String) configData.get(OBJECT_STORE)+ " is Successful");
				logger.log(Level.CONFIG, "Start Test Connection to Workplace server ..."+ (String) configData.get(WORKPLACE_URL));
				testWorkplaceUrl((String) configData.get(WORKPLACE_URL));

			} catch (RepositoryException e) {
				String extractErrorMessage = null;
				String bundleMessage;
				try {
					extractErrorMessage = e.getCause().getClass().getName();
					logger.info("extractErrorMessage: "+extractErrorMessage);
					if(extractErrorMessage.equalsIgnoreCase("com.filenet.wcm.api.InvalidCredentialsException")){
						bundleMessage = resource.getString("invalid_credentials_error");
						logger.log(Level.SEVERE, bundleMessage);
					}else if(extractErrorMessage.equalsIgnoreCase("com.filenet.wcm.api.RemoteServerException")){
						bundleMessage = resource.getString("object_store_invalid");
						logger.log(Level.SEVERE, bundleMessage);
					}else if(extractErrorMessage.equalsIgnoreCase("com.filenet.wcm.api.InsufficientPermissionException")){
						bundleMessage = resource.getString("invalid_credentials_error");
						logger.log(Level.SEVERE, bundleMessage+" Insufficient permission to user on a given ObjectStore.");
					}else{
						bundleMessage = resource.getString(extractErrorMessage);
						logger.log(Level.SEVERE, bundleMessage);
					}
				} catch (MissingResourceException mre) {
					//bundleMessage = resource.getString("required_field_error");
					bundleMessage = e.getLocalizedMessage();//This will get prin
					logger.log(Level.SEVERE, mre.getLocalizedMessage());
				}catch(NullPointerException npe){
					bundleMessage = e.getLocalizedMessage();
					logger.log(Level.SEVERE, bundleMessage);
				}
				form = makeConfigForm(configData, validation);
				logger.log(Level.SEVERE, e.getLocalizedMessage());

				return new ConfigureResponse(bundleMessage ,form);
			}
			return null;
		}
		form = makeConfigForm(configData, validation);
		String errorMsg =resource.getString(validation + "_error"); //added here
		logger.log(Level.FINEST, "Exiting from function validateConfig(Map configData, Locale language, ConnectorFactory connectorFactory)");
		return new ConfigureResponse(errorMsg,form);
	}

	private void testWorkplaceUrl(String workplaceServerUrl)
	throws RepositoryException {

		//Added by Pankaj on 04/05/2009 to remove the dependency of Httpclient.jar file
		try {
			new FileUrlValidator().validate(workplaceServerUrl);
			logger.log(Level.INFO, "Connecttion to Workplace server is Successful");
		} catch (FileUrlValidatorException e) {
			logger.log(Level.SEVERE, resource.getString("workplace_url_error"));
			throw new RepositoryException(resource.getString("workplace_url_error"));
		} catch (Throwable t) {
			logger.log(Level.SEVERE, resource.getString("workplace_url_error"));
			throw new RepositoryException(resource.getString("workplace_url_error"));
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
		logger.log(Level.FINEST, "Entering into function makeConfigForm(Map configMap, String validate)");
		StringBuffer buf = new StringBuffer(BUFFER_SIZE);
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
			} else {
				if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
						/*&& !key.equals(WHERECLAUSE)*/ && !key.equals(FILEPATH)) {
					if(validate.equals(key)){
						appendStartRow(buf, key, validate);
					}else{
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
						/*|| key.equals(WHERECLAUSE)*/ || key.equals(FILEPATH)) {
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
			appendStartHiddenRow(buf);
			Iterator i = new TreeSet(configMap.keySet()).iterator();
			
			//added: NULL checks
			if(null!=i){
				while (i.hasNext()) {
					String key = (String) i.next();
					if (!keySet.contains(key)) {
						String val = (String) configMap.get(key);// add another hidden field to preserve this data
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
		logger.log(Level.FINEST, "Exiting from function makeConfigForm(Map configMap, String validate)");
		return buf.toString();
	}

	private void appendStartHiddenRow(StringBuffer buf) {
		buf.append(TR_START_HIDDEN);
		buf.append(TD_START);

	}

	private void appendStartRow(StringBuffer buf, String key, String validate) {
		buf.append(TR_START);
		buf.append(TD_START_LABEL);
		buf.append(TD_WHITE_SPACE);
		if(isRequired(key)){

			buf.append(TD_END_START_LABEL);
			buf.append(DIV_START_LABEL);
			buf.append(TD_FLOAT_LEFT);
			buf.append(TD_DELIMITER);
			if(!validate.equals("")){
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
		}
		else{
			buf.append(TD_END_START_LABEL);
			buf.append(resource.getString(key));
			buf.append(TD_END);
		}
		buf.append(TD_START);
	}

	private void appendEndRow(StringBuffer buf) {
		buf.append(TD_END);
		buf.append(TR_END);
	}

	private void appendAttribute(StringBuffer buf, String attrName, String attrValue) {
		buf.append(" ");
		buf.append(attrName);
		buf.append("=\"");
		buf.append(attrValue);
		buf.append("\"");
		if (attrName == TYPE && attrValue == TEXT) {
			buf.append(" size=\"50\"");
		}
	}

	private void appendCheckBox(StringBuffer buf, String key, String label,	String value) {
		buf.append(TR_START);
		buf.append(TD_START_COLSPAN);
		buf.append(OPEN_ELEMENT);
		buf.append(INPUT);
		buf.append(" " + TYPE + "=\"" +CHECKBOX+'"');
		buf.append(" " + NAME + "=\"" + key + "\" ");
		if (value != null && value.equals("on")) {
			buf.append(CHECKED);
		}
		buf.append(CLOSE_ELEMENT);
		buf.append(label + TD_END);

		buf.append(TR_END);

	}

	private boolean isRequired(final String configKey){
		final boolean bValue = false;
		if(configKey.equals(OBJECT_STORE) || configKey.equals(WORKPLACE_URL) || configKey.equals(PASSWORD_KEY) || configKey.equals(USERNAME)){
			return true;
		}		
		return bValue;
	}

}
