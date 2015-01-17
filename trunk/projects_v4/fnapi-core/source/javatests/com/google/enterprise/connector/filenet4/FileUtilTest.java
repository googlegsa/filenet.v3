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

import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
import com.google.enterprise.connector.spi.Value;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class FileUtilTest extends TestCase {
  private TimeZone defaultTimeZone = TimeZone.getDefault();

  @Override
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

  public void testGetPrincipals() {
    Set<String> nameSet = new HashSet<String>();
    nameSet.add("user1");
    nameSet.add("user2");
    nameSet.add("group1");
    nameSet.add("group2");
    List<Principal> list = FileUtil.getPrincipals(PrincipalType.UNKNOWN,
        "Default", nameSet, CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    assertEquals(list.size(), 4);

    Principal entry = list.get(0);
    assertEquals(PrincipalType.UNKNOWN, entry.getPrincipalType());
    assertEquals("Default", entry.getNamespace());
    assertTrue(nameSet.contains(entry.getName()));
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        entry.getCaseSensitivityType());
  }

  public void testAddPrincipals() {
    Set<String> nameSet = new HashSet<String>();
    nameSet.add("user1");
    nameSet.add("user2");
    nameSet.add("group1");
    nameSet.add("group2");

    List<Value> list = new LinkedList<Value>();
    FileUtil.addPrincipals(list, PrincipalType.UNKNOWN, "Default", nameSet,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    assertEquals(list.size(), 4);
    String listValueString = list.toString();
    for (String name : nameSet) {
      assertTrue(name + " is not found", listValueString.contains(name));
    }
  }

  public void testConvertDn_email() {
    assertEquals("jsmith@example.com",
        FileUtil.convertDn("jsmith@example.com"));
  }

  public void testConvertDn_netbios() {
    assertEquals("example.com\\jsmith",
        FileUtil.convertDn("example.com\\jsmith"));
  }

  public void testConvertDn_slash() {
    assertEquals("example.com/jsmith",
        FileUtil.convertDn("example.com/jsmith"));
  }

  public void testConvertDn_dn() {
    assertEquals("Jane Smith@example.com",
        FileUtil.convertDn("cn=Jane Smith,ou=users,dc=example,dc=com"));
  }
}