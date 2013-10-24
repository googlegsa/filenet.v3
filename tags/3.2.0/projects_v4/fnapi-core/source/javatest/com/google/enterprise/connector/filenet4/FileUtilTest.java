// Copyright 2013 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet4;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {
  private TimeZone defaultTimeZone = TimeZone.getDefault();

  protected void tearDown() throws Exception {
    TimeZone.setDefault(defaultTimeZone);
  }

  private void testCheckpoint(String tzStr, String dateUnderTest,
      String checkpoint) throws ParseException, RepositoryException {
    TimeZone tz = TimeZone.getTimeZone(tzStr);
    TimeZone.setDefault(tz);
    Value.setFeedTimeZone(null);

    // Construct an input Calendar object in the given time zone.
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    Date d = sdf.parse(dateUnderTest);
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    assertEquals(tz, cal.getTimeZone());

    // Test the checkpoint string.
    Value val = Value.getDateValue(cal);
    SimpleProperty prop = new SimpleProperty(val);
    assertEquals(checkpoint, FileUtil.getQueryTimeString(prop));
  }

  private void testQuery(String tzStr, String checkpoint, String query) {
    TimeZone.setDefault(TimeZone.getTimeZone(tzStr));

    // Test the query string.
    assertEquals(query, FileUtil.getQueryTimeString(checkpoint));
  }

  public void testWestCheckpoint() throws Exception {
    testCheckpoint("GMT-0800",
        "2013-04-30T12:00:00.392+0200",
        "2013-04-30T02:00:00.392-0800");
  }

  public void testWestQuery() {
    testQuery("GMT-0800",
        "2013-04-30T02:00:00.392-0800",
        "2013-04-30T02:00:00.392-08:00");
  }

  public void testUtcCheckpoint() throws Exception {
    testCheckpoint("GMT",
        "2013-04-30T12:00:00.392+0200",
        "2013-04-30T10:00:00.392Z");
  }

  public void testUtcQuery() {
    testQuery("GMT",
        "2013-04-30T10:00:00.392Z",
        "2013-04-30T10:00:00.392+00:00");
  }

  public void testEastCheckpoint() throws Exception {
    testCheckpoint("GMT+0400",
        "2013-04-30T12:00:00.392+0200",
        "2013-04-30T14:00:00.392+0400");
  }

  public void testEastQuery() {
    testQuery("GMT+0400",
        "2013-04-30T14:00:00.392+0400",
        "2013-04-30T14:00:00.392+04:00");
  }

  public void testLegacyCheckpoint() {
    testQuery("GMT+0400",
        "2013-02-28T23:16:04.597",
        "2013-02-28T23:16:04.597+00:00");
  }
}