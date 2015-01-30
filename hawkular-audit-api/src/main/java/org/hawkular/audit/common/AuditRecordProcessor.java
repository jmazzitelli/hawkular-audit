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
package org.hawkular.audit.common;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.Endpoint.Type;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.bus.common.producer.ProducerConnectionContext;

/**
 * Consumes and produces audit records.
 */
public class AuditRecordProcessor extends ConnectionContextFactory {

    private final MessageProcessor msgProcessor;

    public AuditRecordProcessor(String brokerURL) throws JMSException {
        super(brokerURL);
        msgProcessor = new MessageProcessor();
    }

    public AuditRecordProcessor(String brokerURL, String username, String password) throws JMSException {
        super(brokerURL, username, password);
        msgProcessor = new MessageProcessor();
    }

    public AuditRecordProcessor(ConnectionFactory connectionFactory) throws JMSException {
        super(connectionFactory);
        msgProcessor = new MessageProcessor();
    }

    /**
     * Send the given audit record.
     *
     * @param auditRecord the record to send
     * @return the message ID
     * @throws JMSException
     */
    public MessageId sendAuditRecord(AuditRecord auditRecord) throws JMSException {
        ProducerConnectionContext context = createProducerConnectionContext(getEndpoint());
        return msgProcessor.send(context, auditRecord);
    }

    /**
     * Listens for audit records.
     *
     * @param listener the listener that processes the incoming audit records
     * @throws JMSException
     */
    public void listen(BasicMessageListener<AuditRecord> listener) throws JMSException {
        msgProcessor.listen(createConsumerConnectionContext(getEndpoint()), listener);
    }

    /**
     * @return the endpoint used for all audit messages
     */
    protected Endpoint getEndpoint() {
        return new Endpoint(Type.QUEUE, "AuditQueue");
    }
}
