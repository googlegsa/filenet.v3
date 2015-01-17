// Copyright 2010 Google Inc. All Rights Reserved.
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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.Value;

import com.filenet.api.constants.PropertyNames;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
  private static final Logger logger =
      Logger.getLogger(FileUtil.class.getName());

  private static final Pattern ZONE_PATTERN =
      Pattern.compile("(?:(Z)|([+-][0-9]{2})(:)?([0-9]{2})?)$");

  private static final String ZULU_WITH_COLON = "+00:00";

  private FileUtil() {
  }

  /**
   * getShortName takes a string as parameter and parses it to get the shortname. It supports User Principle Name
   * (UPN) and Full Distinguished Name (DN) format.
   * @param longName Username in the form of UPN or Full DN format.
   * @return ShortName of the Username (Which may be in one of the form i.e. UPN or Full DN format.)
   */
  public static String getShortName(String longName) {
    StringTokenizer strtok = new StringTokenizer(longName,",");
    String shortUserName = null;
    if (strtok.countTokens() > 1) {
      while ((null!=strtok) && (strtok.hasMoreTokens())) {

        String mytok1 = strtok.nextToken();
        if (null!=mytok1) {
          //filter for the shortened name
          StringTokenizer innerToken = new StringTokenizer(mytok1,"=");
          if ((null!=innerToken)&&(innerToken.countTokens()==2)) {
            String key = innerToken.nextToken();
            if (null!=key) {
              if ((key.equalsIgnoreCase("cn"))||(key.equalsIgnoreCase("uid"))) {
                shortUserName = innerToken.nextToken();
                break;
              }
            }
          }
        } //end:if (null!=mytok1) {
      } //end: while
    } else if (longName.contains("@")) {
      shortUserName = longName.substring(0, longName.indexOf("@"));
    }
    return shortUserName;
  }

  /** Creates a default property filter for document. */
  public static PropertyFilter getDocumentPropertyFilter(
      Set<String> includedMetaNames) {
    Set<String> filterSet = new HashSet<String>();
    if (includedMetaNames != null) {
      filterSet.addAll(includedMetaNames);
    }
    filterSet.add(PropertyNames.ID);
    filterSet.add(PropertyNames.CLASS_DESCRIPTION);
    filterSet.add(PropertyNames.CONTENT_ELEMENTS);
    filterSet.add(PropertyNames.DATE_LAST_MODIFIED);
    filterSet.add(PropertyNames.MIME_TYPE);
    filterSet.add(PropertyNames.VERSION_SERIES);
    filterSet.add(PropertyNames.VERSION_SERIES_ID);
    filterSet.add(PropertyNames.RELEASED_VERSION);
    filterSet.add(PropertyNames.OWNER);
    filterSet.add(PropertyNames.PERMISSIONS);
    filterSet.add(PropertyNames.PERMISSION_TYPE);
    filterSet.add(PropertyNames.PERMISSION_SOURCE);

    StringBuilder buf = new StringBuilder();
    for (String filterName : filterSet) {
      buf.append(filterName).append(" ");
    }
    buf.deleteCharAt(buf.length() - 1);

    PropertyFilter filter = new PropertyFilter();
    filter.addIncludeProperty(
        new FilterElement(null, null, null, buf.toString(), null));
    return filter;
  }

  /**
   * Retrieve date time value from property and convert it to a proper format.
   * @param Property prop - Document LastModified time
   * @return String - date time in ISO8601 format including zone
   * @throws RepositoryException
   */
  public static String getQueryTimeString(Property prop)
      throws RepositoryException {
    Value val = prop.nextValue();
    if (prop.nextValue() != null) {
      logger.log(Level.WARNING, "Property contains multivalue datetime");
    }
    return val.toString();
  }

  /**
   * Validate the time string by:
   * (a) appending zone portion (+/-hh:mm) or
   * (b) inserting the colon into zone portion
   * if it does not already have zone or colon.
   * 
   * @param String checkpoint - in ISO8601 format
   * @return String - date time in ISO8601 format including zone
   * @throws RepositoryException
   */
  public static String getQueryTimeString(String checkpoint) {
    Matcher matcher = ZONE_PATTERN.matcher(checkpoint);
    if (matcher.find()) {
      String timeZone = matcher.group();
      if (timeZone.length() == 5) {
        return checkpoint.substring(0, matcher.start()) + 
            timeZone.substring(0,3) + ":" + timeZone.substring(3);
      } else if (timeZone.length() == 3) {
        return checkpoint + ":00";
      } else {
        return checkpoint.replaceFirst("Z$", FileUtil.ZULU_WITH_COLON);
      }
    } else {
      return checkpoint + ZULU_WITH_COLON;
    }
  }

  /**
   * Helper method to create a list of principals from names.
   */
  public static List<Principal> getPrincipals(PrincipalType principalType,
      String namespace, Set<String> names,
      CaseSensitivityType caseSensitivityType) {
    List<Principal> principalList = new ArrayList<Principal>(names.size());
    for (String name : names) {
      Principal principal = new Principal(principalType, namespace,
          convertDn(name), caseSensitivityType);
      principalList.add(principal);
    }
    return principalList;
  }

  /**
   * Helper method to add names to principal list.
   */
  public static void addPrincipals(List<Value> list,
      PrincipalType principalType, String namespace, Set<String> names,
      CaseSensitivityType caseSensitivityType) {
    for (String name : names) {
      Principal principal = new Principal(principalType, namespace,
          convertDn(name), caseSensitivityType);
      list.add(Value.getPrincipalValue(principal));
    }
  }

  /**
   * Converts Distinguished Name to shortname@domain format.  If the input name
   * is in domain\shortname, domain/shortname or shortname@domain, it will not
   * do the conversion.
   * 
   * @param name string in Distinguished Name or other naming formats.
   * @return shortname@domain.com
   */
  public static String convertDn(String name) {
    if (name.toLowerCase().startsWith("cn=")) {
      String domainName = getCNFromDN(name) + "@" + getDomain(name);
      logger.log(Level.FINEST, "Convert DN {0} to {1}",
          new Object[] {name, domainName});
      return domainName;
    }
    return name;
  }

  /**
   * Extracts CN attribute from a given DN.
   * This method is copied from
   * com/google/enterprise/connector/dctm/IdentityUtil
   */
  public static String getCNFromDN(String dn) {
    if (Strings.isNullOrEmpty(dn)) {
      return null;
    }
    int pre = dn.toLowerCase().indexOf("cn=");
    int post = dn.indexOf(",", pre);
    if (pre == -1) {
      return null;
    }
    String cn;
    if (post != -1) {
      // Here 3 is length of 'cn='. We just want to add the
      // group name.
      cn = dn.substring(pre + 3, post);
    } else {
      cn = dn.substring(pre + 3);
    }
    return cn;
  }

  /**
   * Given a dn, it returns the domain.
   * E.g., DN: uid=xyz,ou=engineer,dc=corp.google,dc=com
   * it will return corp.google.com
   * 
   * This method is copied from com/google/enterprise/secmgr/ldap/LDAPClient
   * and modified to exclude NETBIOS naming check.
   * 
   * @param dn the distinguished name
   * @return domain in the form abc.com, or null if the input was invalid or did
   * not contain the domain attribute
   */
  public static String getDomain(String dn) {
    if (Strings.isNullOrEmpty(dn)) {
      return null;
    }
    Iterable<String> str =
        Splitter.on(',').trimResults().omitEmptyStrings().split(dn);
    StringBuilder strBuilder = new StringBuilder();
    for (String substr : str) {
      if (substr.startsWith("dc") || substr.startsWith("DC")) {
        strBuilder.append(substr.substring(3)).append(".");
      }
    }
    String strDomain = strBuilder.toString();
    if (Strings.isNullOrEmpty(strDomain)) {
      return null;
    }
    return strDomain.substring(0, strDomain.length() - 1);
  }
}
