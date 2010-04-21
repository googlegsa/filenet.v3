package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDateValue extends ValueImpl {

    private static Logger logger = null;
    {
        logger = Logger.getLogger(FileDateValue.class.getName());
    }

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
    private static final Calendar GMT_CALENDAR = Calendar.getInstance(TIME_ZONE_GMT);

    private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ");

    // Set the date formatter to GMT+0 calendar, so that dates used for
    // checkpoint are always in GMT+0 i.e. UTC
    static {
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
            logger.log(Level.WARNING, "Unable to parse date in milli-second format yyyy-MM-dd'T'HH:mm:ss.SSSZ. Trying to parse in second format yyyy-MM-dd'T'HH:mm:ssZ");
        }
        d = ISO8601_DATE_FORMAT_SECS.parse(s);
        return d;
    }

    /**
     * Converts the date in Calendar to string date in UTC timezone and in the
     * format yyyy-MM-dd'T'HH:mm:ss.SSS
     *
     * @param calendar Calendar whose date need to be converted
     * @return String date in UTC timezone in format yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static synchronized String calendarToIso8601(Calendar calendar) {
        return toISO8601DateFormat(calendar.getTime());
    }

    /**
     * Parses and formats the string in UTC timezone and in the format
     * yyyy-MM-dd'T'HH:mm:ss.SSS
     *
     * @param s String date to be formatted
     * @return and in the format yyyy-MM-dd'T'HH:mm:ss.SSS
     * @throws ParseException
     */
    public static synchronized String calendarToIso8601(String s)
            throws ParseException {
        return toISO8601DateFormat(iso8601ToDate(s));
    }

    /**
     * Converts the date into string in UTC timezone and returns the string date
     * in the format yyyy-MM-dd'T'HH:mm:ss.SSS
     *
     * @param date Date to be formatted
     * @return String date in UTC timezone in format yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    private static String toISO8601DateFormat(Date date) {
        String isoString = ISO8601_DATE_FORMAT_MILLIS.format(date);
        // Since the date string returned in in the form
        // yyyy-MM-dd'T'HH:mm:ss.SSS+0000
        // This may lead to error when actual TimeZone will be concatenated with
        // this date.
        // Thus removing the last substring "+0000"
        isoString = isoString.replaceFirst("\\+0000", "");
        return isoString;
    }

    public String FiletoIso8601() {
        return FileDateValue.calendarToIso8601(calendarValue);
    }

}
