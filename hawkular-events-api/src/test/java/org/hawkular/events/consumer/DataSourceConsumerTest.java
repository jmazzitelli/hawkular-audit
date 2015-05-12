/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.events.consumer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.Subsystem;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DataSourceConsumerTest extends ProducerConsumerSetup {

    @Test
    public void testDataSourceConsumer() throws Exception {
        final long timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24); // yesterday
        final HashMap<String, String> details = new HashMap<String, String>();
        details.put("onekey", "onevalue");
        details.put("twokey", "twovalue");
        final CountDownLatch latch = new CountDownLatch(4); // counts down to count our four test event records

        DataSource mockDs = Mockito.mock(DataSource.class);
        Connection mockConn = Mockito.mock(Connection.class);
        Statement mockStmt = Mockito.mock(Statement.class);
        Mockito.when(mockDs.getConnection()).thenReturn(mockConn);
        Mockito.when(mockConn.createStatement()).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeUpdate(Mockito.anyString())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                String sql = (String) invocation.getArguments()[0];
                if (sql.contains("INSERT")) {
                    // if we get here, we know the DataSourceConsumer is trying to INSERT the data to the database
                    latch.countDown();
                    return 1; // simulate that we inserted one row in the database
                } else {
                    throw new SQLException("invalid sql: " + sql);
                }
            }
        });
        // mock out the consumer's table existence check - pretend the schema always exists
        DatabaseMetaData mockMetadata = Mockito.mock(DatabaseMetaData.class);
        ResultSet mockMetadataResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockConn.getMetaData()).thenReturn(mockMetadata);
        Mockito.when(
                mockMetadata.getTables(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(String[].class))).thenReturn(mockMetadataResults);
        Mockito.when(mockMetadataResults.next()).thenReturn(true);

        DataSourceConsumer listener = new DataSourceConsumer();
        listener.initialize(mockDs, null);
        consumer.listen(listener);

        producer.sendEventRecord(new EventRecord("msg: no details, no timestamp", Subsystem.MISCELLANEOUS));
        producer.sendEventRecord(new EventRecord("msg: no details", new Subsystem("SUBSYSTEM.FOO"), timestamp));
        producer.sendEventRecord(new EventRecord("msg: no timestamp", new Subsystem("ANOTHER.SUBSYS"), details));
        producer.sendEventRecord(new EventRecord("full msg", new Subsystem("WOT.GORILLA"), details, timestamp));

        Assert.assertTrue("Looks like we missed some events", latch.await(10, TimeUnit.SECONDS));
    }
}
