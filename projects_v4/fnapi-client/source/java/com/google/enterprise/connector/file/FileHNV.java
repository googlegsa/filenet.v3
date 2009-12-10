package com.google.enterprise.connector.file;

import javax.net.ssl.SSLSession;


public class FileHNV implements javax.net.ssl.HostnameVerifier {
	
	public FileHNV() {
		
	}

	public boolean verify(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean verify(String arg0, SSLSession arg1) {
		// TODO Auto-generated method stub
		return true;
	}

}
