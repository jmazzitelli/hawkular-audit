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
package org.hawkular.events.common.log;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author John Mazzitelli
 */
@MessageLogger(projectCode = "HAWKEVENT")
@ValidIdRange(min = 1, max = 999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 1, value = "Event schema has been created")
    void infoEventSchemaCreated();

    @LogMessage(level = Level.INFO)
    @Message(id = 2, value = "Event schema already exists")
    void infoEventSchemaExists();

    @LogMessage(level = Level.ERROR)
    @Message(id = 3, value = "Failed to create event schema - event subsystem is most likely in a bad state")
    void errorEventSchemaFailedCreation(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 4, value = "Failed to close connection")
    void errorFailedToCloseConnection(@Cause Throwable t);
}
