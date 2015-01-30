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
package org.hawkular.audit.common.log;

import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author John Mazzitelli
 */
@MessageLogger(projectCode = "HAWKAUDIT")
@ValidIdRange(min = 1000, max = 1999)
public interface MsgLogger {
    @LogMessage(level = Level.INFO)
    @Message(id = 1000, value = "Audit schema has been created")
    void infoAuditSchemaCreated();

    @LogMessage(level = Level.INFO)
    @Message(id = 1001, value = "Audit schema already exists")
    void infoAuditSchemaExists();

    @LogMessage(level = Level.ERROR)
    @Message(id = 1002, value = "Failed to create audit schema - audit subsystem is most likely in a bad state")
    void errorAuditSchemaFailedCreation(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 1003, value = "Failed to close connection")
    void errorFailedToCloseConnection(@Cause Throwable t);
}
