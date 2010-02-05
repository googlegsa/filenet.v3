package com.google.enterprise.connector.file;

import java.util.StringTokenizer;

public class FileUtil {

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
