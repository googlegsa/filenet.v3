package com.google.enterprise.connector.filenet4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.enterprise.connector.spi.Value;
//import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

public class FileDateValue extends ValueImpl {

	Calendar calendarValue;

	public FileDateValue() {

	}
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

	private static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
	private static final Calendar CALENDAR = Calendar.getInstance();
	private static final Calendar GMT_CALENDAR =Calendar.getInstance(TIME_ZONE_GMT);

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	//Set the date formatter to GMT+0 calendar, so that dates used for checkpoint are always in GMT+0 i.e. UTC
	static{
		ISO8601_DATE_FORMAT_MILLIS.setCalendar(GMT_CALENDAR);
		ISO8601_DATE_FORMAT_SECS.setCalendar(GMT_CALENDAR);
	}
	private static synchronized Date iso8601ToDate(String s)
			throws ParseException {
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

	public static synchronized String calendarToIso8601(Calendar c) {
		Date d = c.getTime();
		String isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
		return isoString;
	}

	public static synchronized String calendarToIso8601(String s)
			throws ParseException  {
		Date d = iso8601ToDate(s);
		String isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
		return isoString;
	}

	public String FiletoIso8601() {
		return FileDateValue.calendarToIso8601(calendarValue);
	}

}
