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
package org.hawkular.audit.producer;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.AuditRecordProcessor;
import org.hawkular.audit.common.Subsystem;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.test.AbstractEmbeddedBrokerWrapper;
import org.hawkular.bus.common.test.VMEmbeddedBrokerWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuditRecordProducerTest {

    private AuditRecordProcessor producer;
    private AbstractEmbeddedBrokerWrapper broker;

    @Before
    public void setupProducer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProcessor(brokerURL);
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
        AuditRecord auditRecord;
        auditRecord = new AuditRecord("test audit record", Subsystem.MISCELLANEOUS);
        Assert.assertNull(auditRecord.getMessageId());
        MessageId messageId = producer.sendAuditRecord(auditRecord);
        Assert.assertNotNull(messageId);
        Assert.assertEquals(messageId, auditRecord.getMessageId());
    }
}
