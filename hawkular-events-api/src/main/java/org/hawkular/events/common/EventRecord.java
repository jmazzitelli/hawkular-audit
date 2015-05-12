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
package org.hawkular.events.common;

import java.util.Map;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.SimpleBasicMessage;

import com.google.gson.annotations.Expose;

/**
 * Information about an event.
 *
 * A {@link #getSubsystem() subsystem} identifies in which subsystem the event occurred. If not specified,
 * {@link Subsystem#MISCELLANEOUS} is used.
 *
 * The {@link #getTimestamp() timestamp} identifies when the event occurred. If not specified, the current time is used.
 * Note this is not representative of when this EventRecord was sent to the backend store. This is supposed to identify
 * when the actual event happened.
 */
public class EventRecord extends SimpleBasicMessage {
    @Expose
    private Subsystem subsystem;
    @Expose
    private long timestamp;

    public static EventRecord fromJSON(String json) {
        return BasicMessage.fromJSON(json, EventRecord.class);
    }

    protected EventRecord() {
        // leave everything empty, but we still want to at least set the timestamp to now.
        timestamp = System.currentTimeMillis();
    }

    public EventRecord(String message, Subsystem subsystem) {
        this(message, subsystem, null, 0);
    }

    public EventRecord(String message, Subsystem subsystem, Map<String, String> details) {
        this(message, subsystem, details, 0);
    }

    public EventRecord(String message, Subsystem subsystem, long timestamp) {
        this(message, subsystem, null, timestamp);
    }

    public EventRecord(String message, Subsystem subsystem, Map<String, String> details, long timestamp) {
        super(message, details);

        if (subsystem == null) {
            subsystem = Subsystem.MISCELLANEOUS;
        }
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }

        this.subsystem = subsystem;
        this.timestamp = timestamp;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
