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
package org.hawkular.audit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.ConnectionFactory;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.AuditRecordProcessor;
import org.hawkular.audit.common.Subsystem;
import org.hawkular.audit.log.MsgLogger;
import org.jboss.logging.Logger;

@Startup
@Singleton
public class AuditStartupBean {
    private final MsgLogger msglog = Logger.getMessageLogger(MsgLogger.class, this.getClass().getPackage().getName());

    @Resource(mappedName = "java:/HawkularBusConnectionFactory")
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {
        msglog.infoAuditSubsystemStartingUpNot();

        try {
            String nodeName = System.getProperty("jboss.node.name", "-unknown-");
            String msg = "Audit subsystem starting up on [" + nodeName + "]";
            AuditRecordProcessor processor = new AuditRecordProcessor(this.connectionFactory);
            AuditRecord record = new AuditRecord(msg, new Subsystem("STARTUP"));
            processor.sendAuditRecord(record);
        } catch (Exception e) {
            msglog.errorCannotSendInitialStartupMessage(e.toString());
            throw new RuntimeException(e);
        }
    }
}
