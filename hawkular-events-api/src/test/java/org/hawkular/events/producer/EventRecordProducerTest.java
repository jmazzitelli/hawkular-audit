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
package org.hawkular.events.producer;

import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.test.AbstractEmbeddedBrokerWrapper;
import org.hawkular.bus.common.test.VMEmbeddedBrokerWrapper;
import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.EventRecordProcessor;
import org.hawkular.events.common.Subsystem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventRecordProducerTest {

    private EventRecordProcessor producer;
    private AbstractEmbeddedBrokerWrapper broker;

    @Before
    public void setupProducer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new EventRecordProcessor(brokerURL);
    }

    @After
    public void teardownProducer() throws Exception {
        if (producer != null) {
            producer.close();
            producer = null;
        }
        if (broker != null) {
            broker.stop();
            broker = null;
        }
    }

    @Test
    public void testMessageSend() throws Exception {
        EventRecord eventRecord;
        eventRecord = new EventRecord("test event record", Subsystem.MISCELLANEOUS);
        Assert.assertNull(eventRecord.getMessageId());
        MessageId messageId = producer.sendEventRecord(eventRecord);
        Assert.assertNotNull(messageId);
        Assert.assertEquals(messageId, eventRecord.getMessageId());
    }
}
