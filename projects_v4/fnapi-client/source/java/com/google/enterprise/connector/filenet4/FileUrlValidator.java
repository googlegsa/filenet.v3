// Copyright (C) 2007-2009 Google Inc.
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


import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Validates URLs by making an HTTP request. */
class FileUrlValidator {
    /** The logger for this class. */
    private static final Logger LOGGER =
        Logger.getLogger(FileUrlValidator.class.getName());

    /** An all-trusting TrustManager for SSL URL validation. */
    private static final TrustManager[] trustAllCerts =
        new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkServerTrusted(
                    X509Certificate[] certs, String authType)
                    throws CertificateException {
                    return;
                }
                public void checkClientTrusted(
                    X509Certificate[] certs,
                    String authType)
                    throws CertificateException {
                    return;
                }
            }
        };

  /** An all-trusting HostnameVerifier for SSL URL validation. */
  private static final HostnameVerifier trustAllHosts =
      new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

  /** The connect timeout; only used in Java SE 5.0 or later. */
  private volatile int connectTimeout = 60 * 1000;

  /** The read timeout; only used in Java SE 5.0 or later. */
  private volatile int readTimeout = 60 * 1000;

  /** The HTTP request method. */
  private volatile String requestMethod = "HEAD";

  /** Whether redirects should be followed or returned as the response. */
  private volatile boolean followRedirects = false;

  /** Constructs an instance using the default parameter values. */
  public FileUrlValidator() {
  }

  /**
   * Sets the HTTP request method. The default value is "HEAD".
   *
   * @param requestMethod should be either "GET" or "HEAD"
   * @see HttpURLConnection#setRequestMethod
   */
  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  /**
   * Sets whether to follow HTTP redirects, or return them as the
   * response. The default is <code>false</code>, which returns the
   * redirect as the HTTP response.
   *
   * @param followRedirects <code>true</code> to follow HTTP
   *     redirects, or <code>false</code> to return them as the HTTP
   *     response
   * @see HttpURLConnection#setInstanceFollowRedirects
   */
  public void setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
  }

  /**
   * Sets the connect timeout. The default value is 60000 milliseconds.
   *
   * @param connectTimeout the connect timeout in milliseconds
   * @see URLConnection#setConnectTimeout
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Sets the read timeout. The default value is 60000 milliseconds.
   *
   * @param readTimeout the read timeout in milliseconds
   * @see URLConnection#setReadTimeout
   */
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  /**
   * Attempts to validate the given URL by making an HTTP request. In
   * this case, we're mostly trying to catch typos, so "valid" means
   *
   * <ol>
   * <li>The URL syntax is valid.
   * <li>If the URL uses HTTP or HTTPS:
   *   <ol>
   *   <li>A connection can be made and the response read.
   *   <li>The response code was not 404,
   *   or any of the following related but less common errors:
   *   400, 405, 410, or 414.
   *   </ol>
   * </ol>
   *
   * The 405 (Method Not Allowed) is related because the Sun Java
   * System Web Server, and possibly Apache, return this code rather
   * than a 404 if you attempt to access a CGI program in an unknown
   * directory.
   *
   * When testing an HTTPS URL, we override server certificate
   * validation to skip trying to verify the server's certificate,
   * and we accept hostname mismatches. In this case, all we care
   * about is that the configured URL can be reached; it's up to the
   * connector administrator to enter the right URL.
   *
   * @param urlString the URL to test
   * @throws GeneralSecurityException if there is an error configuring
   *     the HTTPS connection
   * @throws IOException if the URL is malformed, or if there is an
   *     error connecting or reading the response
   * @throws FileUrlValidatorException if the HTTP status code was invalid
   */
  /*
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4912484
   * The above Sun bug report documents that openConnection
   * doesn't try to connect.
   *
   * This method returns the HTTP response code so that it can be
   * unit tested. A return value of 0 is arbitrary and unused by the
   * tests.
   */
  public final int validate(String urlString)
      throws GeneralSecurityException, IOException, FileUrlValidatorException {
    if (urlString == null || urlString.trim().length() == 0)
      return 0;

    URL url = new URL(urlString);
    URLConnection conn = url.openConnection();

    if (!(conn instanceof HttpURLConnection)) {
      // If the URL is not an HTTP or HTTPS URL, which is
      // incredibly unlikely, we don't check anything beyond
      // the URL syntax.
      return 0;
    }

    HttpURLConnection httpConn = (HttpURLConnection) conn;
    if (httpConn instanceof HttpsURLConnection) {
      setTrustingHttpsOptions((HttpsURLConnection) httpConn);
    }
    setTimeouts(conn);
    httpConn.setRequestMethod(requestMethod);
    httpConn.setInstanceFollowRedirects(followRedirects);

    httpConn.connect();
    try {
      int responseCode = httpConn.getResponseCode();
      String responseMessage = httpConn.getResponseMessage();


      switch (responseCode) {
        case HttpURLConnection.HTTP_BAD_REQUEST:
        case HttpURLConnection.HTTP_NOT_FOUND:
        case HttpURLConnection.HTTP_BAD_METHOD:
        case HttpURLConnection.HTTP_GONE:
        case HttpURLConnection.HTTP_REQ_TOO_LONG:
          if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe("Validate URL HTTP response: "
                + responseCode + " " + responseMessage);
          }
          throw new FileUrlValidatorException(responseCode, responseMessage);

        default:
          if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config("Validate URL HTTP response: "
                + responseCode + " " + responseMessage);
          }
          break;
      }
      return responseCode;
    } finally {
      httpConn.disconnect();
    }
  }

  /**
   * Replaces the default <code>TrustManager</code> for this
   * connection with one that trusts all certificates, and the default
   * <code>HostnameVerifier</code> with one that accepts all
   * hostnames.
   *
   * @param conn the HTTPS URL connection
   * @throws GeneralSecurityException if an error occurs setting the properties
   */
  private void setTrustingHttpsOptions(HttpsURLConnection conn)
      throws GeneralSecurityException {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, null);
    SSLSocketFactory factory = sc.getSocketFactory();
    conn.setSSLSocketFactory(factory);

    conn.setHostnameVerifier(trustAllHosts);
  }

  /**
   * Sets the connect and read timeouts of the given
   * <code>URLConnection</code>. This is only possible with Java SE
   * 5.0 or later. With earlier versions, this method does nothing.
   *
   * @param conn the URL connection
   */
  /*
   * Java 1.4 doesn't support setting a timeout on the
   * URLConnection. Java 1.5 does support timeouts, so we're
   * using reflection to set timeouts if available.
   */
   private void setTimeouts(URLConnection conn) {
    try {
      Class c = URLConnection.class;
      Method setConnectTimeout = c.getMethod("setConnectTimeout",
          new Class[] { int.class });
      Method setReadTimeout = c.getMethod("setReadTimeout",
          new Class[] { int.class });

      final Integer[] connectTimeoutArg = { new Integer(connectTimeout) };
      setConnectTimeout.invoke(conn, (Object[]) connectTimeoutArg);

      final Integer[] readTimeoutArg = { new Integer(readTimeout) };
      setReadTimeout.invoke(conn, (Object[]) readTimeoutArg);
    } catch (NoSuchMethodException m) {
      // Ignore; we're probably on Java 1.4.
      LOGGER.log(Level.FINEST,
          "No timeout methods; we're probably on Java 1.4.");
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING, "Error setting connection timeout",
          t);
    }
  }
}

