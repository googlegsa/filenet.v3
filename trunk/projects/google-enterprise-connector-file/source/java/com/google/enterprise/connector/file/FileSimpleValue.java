// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.enterprise.connector.file.filewrap.IDocument;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

/**
 * Simple convenience implementation of the spi.SimpleValue interface. This
 * class is not part of the spi - it is provided for developers to assist in
 * implementations of the spi.
 */
public class FileSimpleValue extends SimpleValue implements Value {

	private final IDocument document;

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	
	private final String stringValue;

	public FileSimpleValue(ValueType t, String v) {
		super(t, v);
		stringValue = v;
		document = null;
	}

	public FileSimpleValue(ValueType t, byte[] v) {
		super(t, v);
		stringValue = "";
		document = null;
	}

	public FileSimpleValue(ValueType t, IDocument v) {

		super(t, "");
		stringValue = "";
		document = v;
	}

	public InputStream getStream() throws IllegalArgumentException,
			IllegalStateException, RepositoryException {
		if (document == null) {
			return super.getStream();
		} else {
			InputStream str = null;
			if(document.getContentSize() != 0){
				str = document.getContent();
			}else{
				str = new ByteArrayInputStream(new byte[1]);
			}
			return str;
		}

	}

	public String getString() throws IllegalArgumentException,
			RepositoryException {
		return stringValue;
	}

	public long getLong() throws IllegalArgumentException, RepositoryException {
		return super.getLong();
	}

	public double getDouble() throws IllegalArgumentException,
			RepositoryException {
		return super.getDouble();
	}

	public boolean getBoolean() throws IllegalArgumentException,
			RepositoryException {
		return super.getBoolean();
	}

	public ValueType getType() throws RepositoryException {
		return super.getType();
	}

	
	public static String calendarToIso8601(Calendar c) {

		Date d = c.getTime();
		String isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
		return isoString;

	}

	private static Date iso8601ToDate(String s) throws ParseException {
		Date d = null;
		try {
			d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
			return d;
		} catch (ParseException e) {
			// this is just here so we can try another format
		}
		d = ISO8601_DATE_FORMAT_SECS.parse(s);
		return d;
	}

	public static Calendar iso8601ToCalendar(String s) throws ParseException {
		Date d = iso8601ToDate(s);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}

	public Calendar getDate() throws IllegalArgumentException,
			RepositoryException {

		Calendar c;
		try {
			c = iso8601ToCalendar(stringValue);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Can't parse stringValue as date: " + e.getMessage());
		}
		return c;

	}

	

}
