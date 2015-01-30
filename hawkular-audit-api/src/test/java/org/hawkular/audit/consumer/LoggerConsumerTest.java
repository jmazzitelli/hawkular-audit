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
package org.hawkular.audit.consumer;

import java.util.HashMap;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.Subsystem;
import org.junit.Test;

public class LoggerConsumerTest extends ProducerConsumerSetup {

    @Test
    public void testLoggerConsumer() throws Exception {
        final long timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24); // yesterday
        final HashMap<String, String> details = new HashMap<String, String>();
        details.put("onekey", "onevalue");
        details.put("twokey", "twovalue");

        LoggerConsumer listener = new LoggerConsumer();
        consumer.listen(listener);

        producer.sendAuditRecord(new AuditRecord("msg: no details, no timestamp", Subsystem.MISCELLANEOUS));
        producer.sendAuditRecord(new AuditRecord("msg: no details", new Subsystem("SUBSYSTEM.FOO"), timestamp));
        producer.sendAuditRecord(new AuditRecord("msg: no timestamp", new Subsystem("ANOTHER.SUBSYS"), details));
        producer.sendAuditRecord(new AuditRecord("full msg", new Subsystem("WOT.GORILLA"), details, timestamp));
    }
}
