package com.google.enterprise.connector.filenet3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class FileDateValueTest extends TestCase {
	public void testCalendarToIso8601() throws ParseException {
		String date = "2010-04-08T18:36:03.327+0530";
		String utcDate = FileDateValue.calendarToIso8601(date);

		assertEquals("2010-04-08T13:06:03.327", utcDate);
	}

	public void ftestFiletoIso8601() throws ParseException {
		String date = "2010-04-08T18:36:03.327+0530";
		TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
		Calendar GMT_CALENDAR = Calendar.getInstance(TIME_ZONE_GMT);
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		sdf.setCalendar(GMT_CALENDAR);
		Date d = sdf.parse(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		FileDateValue fdv = new FileDateValue(cal);
		String utcDate = fdv.FiletoIso8601();

		assertEquals("2010-04-08T13:06:03.327+0000", utcDate);
	}
}
