// Copyright (C) 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class, which will have independent utility methods, that can be used by other classes.
 * @author pankaj_chouhan
 *
 */
public class FileUtil {

	private static Logger logger = null;
	static {
		logger = Logger.getLogger(FileUtil.class.getName());
	}

	private FileUtil() {
	}
	/**
	 * getShortName takes a string as parameter and parses it to get the shortname. It supports User Principle Name
	 * (UPN) and Full Distinguished Name (DN) format.
	 * @param longName Username in the form of UPN or Full DN format.
	 * @return ShortName of the Username (Which may be in one of the form i.e. UPN or Full DN format.)
	 */
	public static String getShortName(String longName){
		StringTokenizer strtok = new StringTokenizer(longName,",");
		String shortUserName = null;
		if(strtok.countTokens() > 1){
			while ((null!=strtok) && (strtok.hasMoreTokens())){

				String mytok1 = strtok.nextToken();
				if(null!=mytok1){
					//filter for the shortened name
					StringTokenizer innerToken = new StringTokenizer(mytok1,"=");
					if((null!=innerToken)&&(innerToken.countTokens()==2)){
						String key = innerToken.nextToken();
						if(null!=key){
							if((key.equalsIgnoreCase("cn"))||(key.equalsIgnoreCase("uid"))){
								shortUserName = innerToken.nextToken();
								break;
							}
						}
					}
				}//end:if(null!=mytok1){
			}//end: while
		} else if(longName.contains("@")){
			shortUserName = longName.substring(0, longName.indexOf("@"));
		}
		return shortUserName;
	}

	/**
     * Validates the String to check whether it represents an IP address or not
     * and returns the boolean status.
     *
     * @param ip IP adress to be validated in the form of string.
     * @return If ip address matches the regular expression then true else false
     *         is returned.
     */
    public static boolean isIPAddress(String ip) {
        if (ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"))
            return true;
        else
            return false;
    }

    /**
     * Validates the server name to check whether it is in FQDN format or not
     * and returns the boolean status.
     *
     * @param serverName Server Name to be validated in the form of string.
     * @return Returns true if Server Name is in FQDN format else returns false.
     */
    public static boolean isFQHN(String serverName){
        if (serverName.indexOf(".") == -1
                || serverName.lastIndexOf(".") == serverName.length() - 1) {
            return false;
        }
        return true;
    }

    /**
     * To fetch the host name or host ip address from a given URL.
     * @param strURL Target URL, whose host name is to be fetched.
     * @return Return the Host Name or Host IP. Returns null, if URL is in malformed state.
     */
    public static String getHost(String strURL){
		try {
			URL url = new URL(strURL);
			return url.getHost();
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Malformed URL has occurred. Either no legal protocol could be found in a URL string or the URL string could not be parsed. URL string is: ["+strURL+"]", e);
			return null;
		}
	}

    /**
     * To fetch the Fully Qualified Host Name.
     * @param host ShortName of the host.
     * @return Returns Fully Qualified Host Name
     */
    public static String getFQHN(String host){
		try {
			return InetAddress.getByName(host).getCanonicalHostName();
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, "Unable to reach the Host: ["+host+"]", e);
			return null;
		}
	}

    public static String getTimeZone(String time){
    	return time.replaceFirst("GMT", "");
    }

    public static String getTimeZone(Calendar cal){
		DateFormat dateStandard = new SimpleDateFormat("Z");
		StringBuffer strDate = new StringBuffer(dateStandard.format(cal.getTime()));
		return (strDate.insert(strDate.length()-2, ':')).toString();
	}
}
