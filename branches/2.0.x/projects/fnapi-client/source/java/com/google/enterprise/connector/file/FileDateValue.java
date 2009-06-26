package com.google.enterprise.connector.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.google.enterprise.connector.spi.Value;
//import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

public class FileDateValue extends ValueImpl {

	Calendar calendarValue;

	
	private static Logger logger = Logger.getLogger(FileDocumentList.class
			.getName());

	
	public FileDateValue(Calendar calendarValue) {
	
		this.calendarValue = calendarValue;
	}
	
	public String toFeedXml() {
		return toString();
	}

	public String toRfc822() {
		return Value.calendarToRfc822(calendarValue);
	}

	public String toIso8601() {
		logger.info("toIso8601 calendarValue : "+calendarValue);
		return FileDateValue.calendarToIso8601(calendarValue);
	}

	public String toString() {
		return toIso8601();
	}

	public boolean toBoolean() {
		return (calendarValue == null);
	}

	public Calendar getCalendarValue() {
		return this.calendarValue;
	}

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private static synchronized Date iso8601ToDate(String s)
			throws ParseException {
		Date d = null;
		try {
			//d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
			d = ISO8601_DATE_FORMAT_SECS.parse(s);
			return d;
		} catch (ParseException e) {
			// this is just here so we can try another format
		}
		//d = ISO8601_DATE_FORMAT_SECS.parse(s);
		d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
		logger.info("WARNING : Date with milliseconds");
		return d;
	}

	public static synchronized Calendar iso8601ToCalendar(String s)
			throws ParseException {
		Date d = iso8601ToDate(s);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}

	public static synchronized String calendarToIso8601(Calendar c) {
		Date d = c.getTime();
		String isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
		return isoString;
	}

	public String FiletoIso8601() {
		return FileDateValue.calendarToIso8601(calendarValue);
	}

}
