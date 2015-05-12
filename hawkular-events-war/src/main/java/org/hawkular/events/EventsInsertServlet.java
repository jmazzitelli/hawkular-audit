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
package org.hawkular.events;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.Endpoint.Type;
import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.EventRecordProcessor;
import org.hawkular.events.common.Subsystem;

public class EventsInsertServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Resource(mappedName = "java:/HawkularBusConnectionFactory")
    private ConnectionFactory connectionFactory;

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String message = null;
        String subsystemName = null;
        HashMap<String, String> details = null;

        boolean logOnly = false;
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramName.equals("message")) {
                message = paramValue;
            } else if (paramName.equals("subsystem")) {
                subsystemName = paramValue;
            } else if (paramName.equals("logonly")) {
                logOnly = Boolean.parseBoolean(paramValue);
            } else {
                if (details == null) {
                    details = new HashMap<String, String>();
                }
                details.put(paramName, paramValue);
            }
        }

        if (message == null) {
            throw new ServletException("'message' parameter must not be null");
        }

        if (subsystemName == null) {
            throw new ServletException("'subsystem' parameter must not be null");
        }

        try {
            // send the event
            EventRecord record;

            if (!logOnly) {
                record = sendEvent(message, subsystemName, details);
            } else {
                record = sendLogOnlyEvent(message, subsystemName, details);
            }

            // let the user know what we did
            PrintWriter writer = response.getWriter();
            writer.println("<p><b>SENT EVENT:</b></p><p>" + record.toJSON() + "</p>");
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    private EventRecord sendEvent(String message, String subsystemName, HashMap<String, String> details)
            throws JMSException {
        // put the event on the bus
        EventRecordProcessor processor = new EventRecordProcessor(this.connectionFactory);
        EventRecord record = new EventRecord(message, new Subsystem(subsystemName), details);
        processor.sendEventRecord(record);
        return record;
    }

    private EventRecord sendLogOnlyEvent(String message, String subsystemName, HashMap<String, String> details)
            throws JMSException {

        // this shows how we can extend the processor to send to different endpoints (in this case a different queue)
        // I could have alternatively designed this log-only stuff with filters, but this is just an illustration.
        EventRecordProcessor processor = new EventRecordProcessor(this.connectionFactory) {
            @Override
            protected Endpoint getEndpoint() {
                return new Endpoint(Type.QUEUE, "LogOnlyEventsQueue");
            }
        };

        EventRecord record = new EventRecord(message, new Subsystem(subsystemName), details);
        processor.sendEventRecord(record);
        return record;
    }
}
