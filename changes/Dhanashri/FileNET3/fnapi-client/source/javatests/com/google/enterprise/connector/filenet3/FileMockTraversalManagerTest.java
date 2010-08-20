// Copyright (C) 2007-2010 Google Inc.
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
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import junit.framework.TestCase;

public class FileMockTraversalManagerTest extends TestCase {

    Connector connector = null;

    Session sess = null;

    FileTraversalManager qtm = null;

    protected void setUp() throws Exception {
        connector = new FileConnector();
        ((FileConnector) connector).setUsername(FnMockConnection.userName);
        ((FileConnector) connector).setPassword(FnMockConnection.password);
        ((FileConnector) connector).setObject_store(FnMockConnection.objectStoreName);
        // ((FileConnector)
        // connector).setCredential_tag(FnMockConnection.credTag);
        ((FileConnector) connector).setWorkplace_display_url(FnMockConnection.displayUrl);
        ((FileConnector) connector).setObject_factory(FnMockConnection.objectFactory);
        ((FileConnector) connector).setPath_to_WcmApiConfig(FnMockConnection.pathToWcmApiConfig);
        ((FileConnector) connector).setAdditional_where_clause(FnMockConnection.additionalWhereClause);
        sess = (FileSession) connector.login();
        qtm = (FileTraversalManager) sess.getTraversalManager();
    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
     */
    public void testStartTraversal() throws RepositoryException {
        DocumentList resultSet = qtm.startTraversal();
        assertTrue(resultSet instanceof DocumentList);
        int counter = 0;
        while (resultSet.nextDocument() != null) {
            // resultSet.nextDocument();
            counter++;
        }
        assertEquals(27, counter);

    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
     */
    public void testResumeTraversal() throws RepositoryException {
        DocumentList resultSet = null;
        String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1969-01-01 01:00:00.010\"}";
        resultSet = (DocumentList) qtm.resumeTraversal(checkPoint);
        assertNotNull(resultSet);
        int counter = 0;
        while (resultSet.nextDocument() != null) {
            counter++;
        }
        assertEquals(27, counter);
    }

    public void testResumeTraversalWithSimilarDate() throws RepositoryException {
        DocumentList resultSet = null;

        // String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01
        // 01:00:00.010\"}";

        String checkPoint = "{\"uuid\":\"users\",\"lastModified\":\"1994-11-15 12:45:26.010\"}";

        qtm.setBatchHint(1);
        resultSet = qtm.resumeTraversal(checkPoint);
        // FileDocumentIterator iter = (FileDocumentIterator) resultSet
        // .iterator();
        FileDocument map;
        String docId;
        String modifyDate;
        String[] tabDocIds = { "users" };
        String[] tabTimeStamp = { "1970-01-01T00:00:00.000Z" };
        int i = 0;
        if ((map = (FileDocument) resultSet.nextDocument()) != null) {
            docId = map.findProperty(SpiConstants.PROPNAME_DOCID).nextValue().toString();
            assertEquals(tabDocIds[i], docId);
            modifyDate = map.findProperty(SpiConstants.PROPNAME_LASTMODIFIED).nextValue().toString();
            assertEquals(tabTimeStamp[i], modifyDate);
            i++;
        }
    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.fetchAndVerifyValueForCheckpoint(PropertyMap,
     * String)'
     */
    public void testFetchAndVerifyValueForCheckpoint()
            throws IllegalArgumentException, RepositoryException {
        FileDocument propertyMap = new FileDocument("doc2",
                ((FileSession) sess).getObjectStore(), false, "",
                FnMockConnection.included_meta, FnMockConnection.excluded_meta,
                SpiConstants.ActionType.ADD);
        Calendar calDate = null;

        FileDocumentList fdl = (FileDocumentList) qtm.startTraversal();
        DateValue value = (DateValue) fdl.fetchAndVerifyValueForCheckpoint(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED).nextValue();
        // Value value = qtm.fetchAndVerifyValueForCheckpoint(propertyMap,
        // SpiConstants.PROPNAME_LASTMODIFIED);
        calDate = Calendar.getInstance();
        calDate.set(1970, 0, 1, 1, 0, 0);

        assertTrue(value instanceof ValueImpl);
        // assertEquals(calDate.getTime().toString(),value.getDate().getTime().toString());
        assertEquals("1970-01-01T00:00:00.020Z", value.toIso8601());

        StringValue value2 = (StringValue) fdl.fetchAndVerifyValueForCheckpoint(propertyMap, SpiConstants.PROPNAME_DOCID).nextValue();
        assertTrue(value2 instanceof StringValue);
        assertEquals("doc2", value2.toString());

    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractDocidFromCheckpoint(JSONObject,
     * String)'
     */
    public void testExtractDocidFromCheckpoint() {
        String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01 01:00:00.020\"}";
        String uuid = null;
        JSONObject jo = null;

        try {
            jo = new JSONObject(checkPoint);
        } catch (JSONException e) {
            throw new IllegalArgumentException(
                    "checkPoint string does not parse as JSON: " + checkPoint);
        }

        uuid = qtm.extractDocidFromCheckpoint(jo, checkPoint, FnConnection.PARAM_UUID);
        assertNotNull(uuid);
        assertEquals(uuid, "doc2");
    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.extractNativeDateFromCheckpoint(JSONObject,
     * String)'
     */
    public void testExtractNativeDateFromCheckpoint() {
        String checkPoint = "{\"uuid\":\"doc2\",\"lastModified\":\"1970-01-01 01:00:00.020\"}";
        JSONObject jo = null;
        String modifDate = null;

        try {
            jo = new JSONObject(checkPoint);
        } catch (JSONException e) {
            throw new IllegalArgumentException(
                    "checkPoint string does not parse as JSON: " + checkPoint);
        }

        modifDate = qtm.extractNativeDateFromCheckpoint(jo, checkPoint, FnConnection.PARAM_DATE_LASTMODIFIED);
        assertNotNull(modifDate);
        assertEquals(modifDate, "1970-01-01 01:00:00.020");
    }

    /*
     * Test method for
     * 'com.google.enterprise.connector.file.FileQueryTraversalManager.makeCheckpointQueryString(String,
     * String)'
     */
    public void testMakeCheckpointQueryString() {
        String uuid = "doc2";
        String statement = "";

        try {
            statement = qtm.makeCheckpointQueryString(uuid, "1970-01-01 01:00:00.020", FnConnection.PARAM_UUID);
        } catch (RepositoryException re) {
            re.printStackTrace();
        }

        assertNotNull(statement);
        assertEquals(FnMockConnection.FN_CHECKPOINT_QUERY_STRING, statement);
    }

}
