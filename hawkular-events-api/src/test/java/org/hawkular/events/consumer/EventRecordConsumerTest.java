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

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.test.StoreAndLatchBasicMessageListener;
import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.Subsystem;
import org.junit.Assert;
import org.junit.Test;

public class EventRecordConsumerTest extends ProducerConsumerSetup {

    @Test
    public void testMessageSend() throws Exception {
        final int numberOfTestRecords = 5;
        final CountDownLatch latch = new CountDownLatch(numberOfTestRecords);
        final ArrayList<EventRecord> records = new ArrayList<EventRecord>();
        final StoreAndLatchBasicMessageListener<EventRecord> listener =
                new StoreAndLatchBasicMessageListener<EventRecord>(latch, records, null, EventRecord.class);

        consumer.listen(listener);

        // send some event records
        for (int i = 0; i < numberOfTestRecords; i++) {
            EventRecord eventRecord = new EventRecord("test event message#" + i, Subsystem.MISCELLANEOUS);
            producer.sendEventRecord(eventRecord);
        }
        latch.await(5, TimeUnit.SECONDS);

        // make sure the event records flowed properly
        Assert.assertEquals(records.size(), numberOfTestRecords);
        for (int i = 0; i < numberOfTestRecords; i++) {
            Assert.assertEquals(Subsystem.MISCELLANEOUS, records.get(i).getSubsystem());
            Assert.assertEquals("test event message#" + i, records.get(i).getMessage());
            Assert.assertNull(records.get(i).getDetails());
            Assert.assertTrue(records.get(i).getTimestamp() > 0L);
            Assert.assertNotNull(records.get(i).getMessageId());
            Assert.assertNull(records.get(i).getCorrelationId());
        }
    }

    @Test
    public void testCorrelatedMessages() throws Exception {
        final int numberOfTestRecords = 5;
        final CountDownLatch latch = new CountDownLatch(numberOfTestRecords);
        final ArrayList<EventRecord> records = new ArrayList<EventRecord>();
        final StoreAndLatchBasicMessageListener<EventRecord> listener =
                new StoreAndLatchBasicMessageListener<EventRecord>(latch, records, null, EventRecord.class);

        consumer.listen(listener);

        // send some event records, correlate the everything to the first one
        MessageId firstMessageId = null;
        for (int i = 0; i < numberOfTestRecords; i++) {
            EventRecord eventRecord = new EventRecord("test event message#" + i, Subsystem.MISCELLANEOUS);
            if (firstMessageId != null) {
                eventRecord.setCorrelationId(firstMessageId);
                producer.sendEventRecord(eventRecord);
            } else {
                firstMessageId = producer.sendEventRecord(eventRecord);
            }
        }
        latch.await(5, TimeUnit.SECONDS);

        // make sure the event records flowed properly
        firstMessageId = null;
        Assert.assertEquals(numberOfTestRecords, records.size());
        for (int i = 0; i < numberOfTestRecords; i++) {
            Assert.assertEquals(Subsystem.MISCELLANEOUS, records.get(i).getSubsystem());
            Assert.assertEquals("test event message#" + i, records.get(i).getMessage());
            Assert.assertNull(records.get(i).getDetails());
            Assert.assertTrue(records.get(i).getTimestamp() > 0L);
            Assert.assertNotNull(records.get(i).getMessageId());
            if (i == 0) {
                Assert.assertNull(records.get(i).getCorrelationId());
                firstMessageId = records.get(i).getMessageId();
            } else {
                Assert.assertEquals(firstMessageId, records.get(i).getCorrelationId());
            }
        }
    }
}
