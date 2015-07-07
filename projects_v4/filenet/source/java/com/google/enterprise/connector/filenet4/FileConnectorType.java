// Copyright 2009 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.filenet4;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.filenet4.api.SearchWrapper;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.XmlUtils;
import com.google.enterprise.connector.util.UrlValidator;

import com.filenet.api.exception.EngineRuntimeException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the FileNet connector configuration form.
 **/
public class FileConnectorType implements ConnectorType {
  private static Logger LOGGER =
      Logger.getLogger(FileConnectorType.class.getName());

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
  private static final String DELETEWHERECLAUSE = "delete_additional_where_clause";
  private static final String CHECKMARKING = "check_marking";
  private static final String MARKINGCHECKBOX = "checkbox";
  private static final String MARKINGCHECKED = "checked='checked'";
  private static final String SELECT = "SELECT";
  private static final String QUERYFORMAT = "SELECT ID,DATELASTMODIFIED FROM ";
  private static final String VERSIONQUERY = "WHERE VersionStatus=1 and ContentSize IS NOT NULL";
  private static final String ACCESS_DENIED_EXCEPTION = "com.filenet.api.exception.EngineRuntimeException: E_ACCESS_DENIED:";
  private static final String RETRIEVE_SQL_SYNTAX_ERROR = "com.filenet.api.exception.EngineRuntimeException: RETRIEVE_SQL_SYNTAX_ERROR:";

  private static final Set<String> keys = ImmutableSet.of(
      "username",
      "Password",
      "object_store",
      "content_engine_url",
      "workplace_display_url",
      "object_factory",
      "additional_where_clause",
      "delete_additional_where_clause",
      "check_marking");

  private String validation = "";

  /**
   * Supply rows in an HTML table as a connector configuration form to the
   * Admin Console so that an administrator can specify parameter values for a
   * connector
   */
  @Override
  public ConfigureResponse getConfigForm(Locale locale) {
    return getPopulatedConfigForm(null, locale);
  }

  /**
   * Supply rows in an HTML table as a connector configuration form to the
   * Admin Console so that an administrator can change parameter values for a
   * connector
   */
  @Override
  public ConfigureResponse getPopulatedConfigForm(Map<String, String> configMap,
      Locale locale) {
    ResourceBundle resource;
    try {
      resource = ResourceBundle.getBundle("FileConnectorResources", locale);
    } catch (MissingResourceException e) {
      LOGGER.log(Level.SEVERE,
          "Unable to find the resource bundle file for locale " + locale, e);
      return new ConfigureResponse(e.getMessage(), "");
    }
    return new ConfigureResponse("",
        makeConfigForm(configMap, this.validation, resource));
  }

  /**
   * Loops on keys and return a key name only if it finds a required
   * key with a null or blank value.
   */
  private String validateConfigMap(Map<String, String> configData) {
    for (Iterator<String> i = keys.iterator(); i.hasNext();) {
      String key = i.next();
      String val = configData.get(key);
      if (!key.equals(FNCLASS)
              && !key.equals(AUTHENTICATIONTYPE)
              && !key.equals(WHERECLAUSE)
              && !key.equals(DELETEWHERECLAUSE)
              && !key.equals(CHECKMARKING)
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
  @Override
  public ConfigureResponse validateConfig(Map<String, String> configData,
      Locale locale, ConnectorFactory connectorFactory) {
    ResourceBundle resource;
    try {
      resource = ResourceBundle.getBundle("FileConnectorResources", locale);
    } catch (MissingResourceException e) {
      LOGGER.log(Level.SEVERE,
          "Unable to find the resource bundle file for locale " + locale, e);
      return new ConfigureResponse(e.getMessage(), "");
    }

    if (configData == null) {
      LOGGER.severe("No configuration information is available");
      return null;
    }

    String form = null;

    LOGGER.info("validating the configuration data...");
    String validation = validateConfigMap(configData);
    this.validation = validation;

    LOGGER.info("Configuration data validation.. succeeded");

    FileSession session = null;
    if (validation.equals("")) {
      try {
        LOGGER.info("Attempting to create FileNet4 connector instance");
        // Removing the extra slashes at the right end of content
        // engine URL.
        configData.put(CONTENT_ENGINE_URL,
            rightTrim((configData.get(CONTENT_ENGINE_URL)).trim(), '/'));

        FileConnector conn = (FileConnector) connectorFactory.makeConnector(configData);
        if (null == conn) {
          LOGGER.severe("Unable to establish connection with FileNet server");
          return null;
        }

        LOGGER.info("FileNet4 connector instance creation succeeded. Trying to Login into FileNet server.");
        session = (FileSession) conn.login();

        if (session != null) {
          LOGGER.log(Level.INFO, "Connection to Content Engine URL is Successful");
          session.getTraversalManager();
          LOGGER.log(Level.INFO, "Connection to Object Store "
              + configData.get("object_store") + " is Successful");
        } else {
          LOGGER.log(Level.INFO, "Connection to Content Engine URL Failed");
        }

        testWorkplaceUrl(configData.get("workplace_display_url").trim(),
            resource);

        StringBuffer query = new StringBuffer();

        if (configData.get(WHERECLAUSE).trim().toUpperCase()
            .startsWith(SELECT)) {
          if (configData.get(WHERECLAUSE).trim().toUpperCase()
              .startsWith(QUERYFORMAT)) {
            if (configData.get(WHERECLAUSE).trim().toUpperCase()
                .contains(VERSIONQUERY.toUpperCase())) {
              query = new StringBuffer(configData.get(WHERECLAUSE).trim());
              LOGGER.fine("Using Custom Query["
                  + configData.get(WHERECLAUSE).trim() + "]");
            } else {
              this.validation = WHERECLAUSE;
              form = makeConfigForm(configData, this.validation, resource);
              return new ConfigureResponse(
                      resource.getString("query_not_having_versionstatus_condition"),
                      form);
            }
          } else {
            this.validation = WHERECLAUSE;
            form = makeConfigForm(configData, this.validation, resource);
            return new ConfigureResponse(
                    resource.getString("query_not_starting_with_SELECT_Id,DateLastModified_FROM_or_with_AND"),
                    form);
          }
        } else {
          query.append("SELECT TOP 1 Id, DateLastModified FROM Document WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
          query.append(configData.get(WHERECLAUSE).trim());
        }

        try {
          if (session != null) {
            SearchWrapper search = session.getSearch();
            search.fetchObjects(query.toString(),
                1, SearchWrapper.noFilter, SearchWrapper.FIRST_ROWS);
          }
        } catch (EngineRuntimeException e) {
          if (e.toString().contains(ACCESS_DENIED_EXCEPTION)) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = OBJECT_STORE;
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(
                    resource.getString("object_store_access_error"),
                    form);
          } else if (e.toString().contains(RETRIEVE_SQL_SYNTAX_ERROR)) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = WHERECLAUSE;
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(
                    resource.getString("additional_where_clause_invalid"),
                    form);
          } else {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = "FileNet exception";
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(e.getLocalizedMessage(),
                    form);
          }
        }

        StringBuffer deleteuery = new StringBuffer();

        if (configData.get(DELETEWHERECLAUSE).trim().toUpperCase()
            .startsWith(SELECT)) {
          if (configData.get(DELETEWHERECLAUSE).trim().toUpperCase()
              .startsWith(QUERYFORMAT)) {
            if (configData.get(DELETEWHERECLAUSE).trim().toUpperCase()
                .contains(((VERSIONQUERY)).toUpperCase())) {
              deleteuery = new StringBuffer(
                      configData.get(DELETEWHERECLAUSE).trim());
              LOGGER.fine("Using Custom Query["
                      + configData.get(DELETEWHERECLAUSE).trim()
                      + "]");
            } else {
              this.validation = DELETEWHERECLAUSE;
              form = makeConfigForm(configData, this.validation, resource);
              return new ConfigureResponse(
                      resource.getString("delete_query_not_having_versionstatus_condition"),
                      form);
            }
          } else {
            this.validation = DELETEWHERECLAUSE;
            form = makeConfigForm(configData, this.validation, resource);
            return new ConfigureResponse(
                    resource.getString("delete_query_not_starting_with_SELECT_Id,DateLastModified_FROM_or_with_AND"),
                    form);
          }
        } else {
          deleteuery.append("SELECT TOP 1 Id, DateLastModified FROM Document WHERE VersionStatus=1 and ContentSize IS NOT NULL ");
          deleteuery.append(configData.get(DELETEWHERECLAUSE).trim());
        }

        try {
          if (session != null) {
            SearchWrapper search = session.getSearch();
            search.fetchObjects(deleteuery.toString(),
                1, SearchWrapper.noFilter, SearchWrapper.FIRST_ROWS);
          }
        } catch (EngineRuntimeException e) {
          if (e.toString().contains(ACCESS_DENIED_EXCEPTION)) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = OBJECT_STORE;
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(
                    resource.getString("object_store_access_error"),
                    form);
          } else if (e.toString().contains(RETRIEVE_SQL_SYNTAX_ERROR)) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = DELETEWHERECLAUSE;
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(
                    resource.getString("delete_additional_where_clause_invalid"),
                    form);
          } else {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            this.validation = "FileNet exception";
            form = makeConfigForm(configData, this.validation, resource);

            return new ConfigureResponse(e.getLocalizedMessage(),
                    form);
          }
        }

        if (!configData.get(WHERECLAUSE).trim().equalsIgnoreCase("")
                || !configData.get(DELETEWHERECLAUSE).trim().equalsIgnoreCase("")) {
          if ((configData.get(WHERECLAUSE).trim()).equalsIgnoreCase(configData.get(DELETEWHERECLAUSE).trim())) {
            this.validation = DELETEWHERECLAUSE;
            form = makeConfigForm(configData, this.validation, resource);
            return new ConfigureResponse(
                    resource.getString("same_additional_where_clause_and_additional_delete_clause"),
                    form);
          }
        }
      } catch (EngineRuntimeException e) {
        String errorKey = e.getExceptionCode().getKey();
        String bundleMessage;
        try {
          if (errorKey.equalsIgnoreCase("E_NULL_OR_INVALID_PARAM_VALUE")) {
            bundleMessage = resource.getString("content_engine_url_invalid");
            LOGGER.log(Level.SEVERE, bundleMessage, e);
          } else {
            bundleMessage = e.getLocalizedMessage();
            LOGGER.log(Level.SEVERE, bundleMessage);
          }
        } catch (MissingResourceException mre) {
          bundleMessage = e.getLocalizedMessage();
          LOGGER.log(Level.SEVERE, bundleMessage, mre);
        }
        form = makeConfigForm(configData, validation, resource);
        return new ConfigureResponse(bundleMessage, form);
      } catch (RepositoryException e) {
        String bundleMessage;
        try {
          if (e.getCause() instanceof EngineRuntimeException) {
            EngineRuntimeException ere = (EngineRuntimeException) e.getCause();
            String errorKey = ere.getExceptionCode().getKey();
            if (errorKey.equalsIgnoreCase("E_OBJECT_NOT_FOUND")) {
              bundleMessage = resource.getString("object_store_invalid");
            } else if (errorKey.equalsIgnoreCase("E_NOT_AUTHENTICATED")) {
              bundleMessage = resource.getString("invalid_credentials_error");
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
              } else if (ere.getCause() instanceof ExceptionInInitializerError) {
                bundleMessage = resource.getString("jaxrpc_jar_error");
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
          LOGGER.log(Level.SEVERE, bundleMessage, e);
        } catch (MissingResourceException mre) {
          bundleMessage = resource.getString("required_field_error")
                  + " " + mre.getLocalizedMessage();
          LOGGER.log(Level.SEVERE, bundleMessage, mre);
        } catch (NullPointerException npe) {
          bundleMessage = npe.getLocalizedMessage();
          LOGGER.log(Level.SEVERE, "Unable to connect to FileNet server. Got exception: ", npe);
        } catch (Throwable th) {
          bundleMessage = th.getLocalizedMessage();
          LOGGER.log(Level.SEVERE, "Unable to connect to FileNet server. Got exception: ", th);
        }

        LOGGER.info("request to make configuration form..");
        form = makeConfigForm(configData, validation, resource);
        return new ConfigureResponse(bundleMessage, form);
      }
      return null;
    }
    form = makeConfigForm(configData, validation, resource);
    return new ConfigureResponse(resource.getString(validation + "_error"),
            form);
  }

  /**
   * This method validates WorkPlace URL used to configure connector.
   */
  private void testWorkplaceUrl(String workplaceServerUrl,
      ResourceBundle resource) throws RepositoryException {
    try {
      new UrlValidator().validate(workplaceServerUrl);
      LOGGER.log(Level.INFO, "Connection to Workplace URL is Successful");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error validating Workplace URL", e);
      throw new RepositoryException(
              resource.getString("workplace_url_error"));
    }
  }

  /**
   * Make a config form snippet using the keys (in the supplied order) and, if
   * passed a non-null config map, pre-filling values in from that map
   *
   * @return config form snippet
   */
  private String makeConfigForm(Map<String, String> configMap,
      String validate, ResourceBundle resource) {
    StringBuffer buf = new StringBuffer(2048);
    String value = " ";
    for (Iterator<String> i = keys.iterator(); i.hasNext();) {
      String key = i.next();
      if (configMap != null) {
        value = configMap.get(key);
      }

      if (key.equals(CHECKMARKING)) {
        appendMarkingCheckBox(buf, key, resource.getString(key), value);
        appendStartHiddenRow(buf);
        buf.append(OPEN_ELEMENT);
        buf.append(INPUT);
        appendAttribute(buf, TYPE, HIDDEN);
        appendAttribute(buf, VALUE, "off");
        appendAttribute(buf, NAME, key);
        buf.append(CLOSE_ELEMENT);
        appendEndRow(buf);
        value = "";
      } else {
        if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
            && !key.equals(FILEPATH)) {
          if (validate.equals(key)) {
            appendStartRow(buf, key, validate, resource);
          } else {
            appendStartRow(buf, key, "", resource);
          }
        } else {
          appendStartHiddenRow(buf);
        }
        buf.append(OPEN_ELEMENT);

        if (key.equals(WHERECLAUSE) || key.equals(DELETEWHERECLAUSE)) {
          buf.append(TEXTAREA);
          appendAttribute(buf, TYPE, TEXTAREA);
          appendAttribute(buf, NAME, key);
          buf.append(CLOSE_TAG);
          if (value == null) {
            value = "";
          }

          try {
            XmlUtils.xmlAppendAttrValue(value, buf);
          } catch (IOException e) {
            LOGGER.severe("SEVERE" + e.getStackTrace());
          }

          buf.append(OPEN_ELEMENT);
          buf.append("/" + TEXTAREA);
          buf.append(CLOSE_TAG);
        } else {
          buf.append(INPUT);
          if (key.equalsIgnoreCase(PASSWORD_KEY)) {
            appendAttribute(buf, TYPE, PASSWORD);
          } else if (key.equals(FNCLASS)
                  || key.equals(AUTHENTICATIONTYPE)) {
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
    }
    if (configMap != null) {
      appendStartHiddenRow(buf);
      Iterator<String> i = new TreeSet<String>(configMap.keySet()).iterator();
      while (i.hasNext()) {
        String key = i.next();
        if (!keys.contains(key)) {
          // add another hidden field to preserve this data
          String val = configMap.get(key);
          buf.append("<input type=\"hidden\" value=\"");
          buf.append(val);
          buf.append("\" name=\"");
          buf.append(key);
          buf.append("\"/>\r\n");
        }
      }
      appendEndRow(buf);
    }
    return buf.toString();
  }

  /**
   * To append table row start (TR_START) and table column start (TD_START)
   * tags to the configuration form for the hidden form elements.
   */
  private void appendStartHiddenRow(StringBuffer buf) {
    buf.append(TR_START_HIDDEN);
    buf.append(TD_START);
  }

  /**
   * To creates a new table row in the configuration form.
   */
  private void appendStartRow(StringBuffer buf, String key, String validate,
      ResourceBundle resource) {
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
      buf.append(resource.getString(key));
      buf.append(TD_END);
    }
    buf.append(TD_START);
  }

  /**
   * To append table column end (TD_END), and
   * table row end (TR_END) tags to the current table row.
   */
  private void appendEndRow(StringBuffer buf) {
    buf.append(TD_END);
    buf.append(TR_END);
  }

  /**
   * To append an attribute to the connector configuration form.
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

  /**
   * To add a 'Check Marking Set' check box to the form
   */
  private void appendMarkingCheckBox(StringBuffer buf, String key,
          String label, String value) {
    buf.append(TR_START);
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

  /**
   * To check all the required field are entered or not.
   */
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
