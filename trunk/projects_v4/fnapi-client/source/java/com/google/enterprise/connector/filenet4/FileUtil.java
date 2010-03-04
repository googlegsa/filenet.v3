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

import java.util.StringTokenizer;
/**
 * Utility class, which will have independent utility methods, that can be used by other classes.
 * @author pankaj_chouhan
 *
 */
public class FileUtil {

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
}
