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

import java.util.Map;

import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.Subsystem;
import org.jboss.logging.Logger;

/**
 * A very simple event record consumer that merely logs the event record via a logger.
 *
 * @author John Mazzitelli
 */
public class LoggerConsumer extends BasicMessageListener<EventRecord> {
    @Override
    protected void onBasicMessage(EventRecord basicMessage) {
        final MessageId id = basicMessage.getMessageId();
        final MessageId corId = basicMessage.getCorrelationId();
        final String msg = basicMessage.getMessage();
        final Map<String, String> details = basicMessage.getDetails();
        final Subsystem subsystem = basicMessage.getSubsystem();
        final long timestamp = basicMessage.getTimestamp();

        Logger logger = Logger.getLogger("EVENT." + subsystem);
        logger.infof("id=[%s], correlation=[%s], timestamp=[%d], subsystem=[%s], msg=[%s], details=[%s],", id, corId,
                timestamp, subsystem, msg, details);
    }
}
