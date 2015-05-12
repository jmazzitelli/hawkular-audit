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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.events.common.EventRecord;
import org.hawkular.events.common.Subsystem;
import org.hawkular.events.common.log.MsgLogger;

/**
 * Stores event records in the backend data store via the given DataSource.
 *
 * @author John Mazzitelli
 */
public class DataSourceConsumer extends BasicMessageListener<EventRecord> {
    private DataSource dataSource;
    private SqlGenerator sqlGenerator;

    /**
     * You can implement your own custom SQL generator and pass it to the constructor of DataSourceConsumer if you want
     * to insert the event record in your own custom database.
     */
    public interface SqlGenerator {
        /**
         * Given an event record, return the SQL that should be used to insert the record into the database.
         *
         * @param eventRecord the event record to be stored
         * @return the INSERT SQL statement that can be used to store the event record
         */
        String generateSql(EventRecord eventRecord);

        /**
         * Asks the SQL generator to initialize its schema if required.
         */
        void createSchema(DataSource ds);
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }

    protected void setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("datasource is null");
        }
        this.dataSource = dataSource;
    }

    protected SqlGenerator getSqlGenerator() {
        return this.sqlGenerator;
    }

    protected void setSqlGenerator(SqlGenerator sqlGenerator) {
        // if no SQL generator given, use a default one that assumes a simple schema with one table
        if (sqlGenerator == null) {
            sqlGenerator = new SqlGenerator() {
                @Override
                public String generateSql(EventRecord eventRecord) {
                    final MessageId id = eventRecord.getMessageId();
                    final MessageId corId = eventRecord.getCorrelationId();
                    final String msg = eventRecord.getMessage();
                    final Map<String, String> details = eventRecord.getDetails();
                    final Subsystem subsystem = eventRecord.getSubsystem();
                    final long timestamp = eventRecord.getTimestamp();

                    final String sql = String.format("INSERT INTO HAWKULAR_EVENTS " + //
                            "(ID, CORRELATION_ID, SUBSYSTEM, EVENT_TIME, MESSAGE, DETAILS) " + //
                            "VALUES ('%s', '%s', '%s', %d, '%s', '%s')", //
                            id, corId, subsystem, timestamp, msg, details);
                    return sql;
                }

                @Override
                public void createSchema(DataSource ds) {
                    String createString = "CREATE TABLE HAWKULAR_EVENTS (" + //
                            "ID VARCHAR(512) NULL," + //
                            "CORRELATION_ID VARCHAR(512) NULL," + //
                            "SUBSYSTEM VARCHAR(512) NULL," + //
                            "EVENT_TIME LONG NULL," + //
                            "MESSAGE VARCHAR(4096) NULL," + //
                            "DETAILS VARCHAR(4096) NULL)";

                    Connection conn = null;
                    try {
                        conn = ds.getConnection();
                        DatabaseMetaData metadata = conn.getMetaData();
                        ResultSet tables = metadata.getTables(null, null, "HAWKULAR_EVENTS", new String[] { "TABLE" });
                        if (!tables.next()) {
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(createString);
                            MsgLogger.LOGGER.infoEventSchemaCreated();
                        } else {
                            MsgLogger.LOGGER.infoEventSchemaExists();
                        }
                    } catch (SQLException sqle) {
                        MsgLogger.LOGGER.errorEventSchemaFailedCreation(sqle);
                    } finally {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException sqle) {
                                MsgLogger.LOGGER.errorFailedToCloseConnection(sqle);
                            }
                        }
                    }
                }
            };
        }

        this.sqlGenerator = sqlGenerator;
    }

    /**
     * Call this to initialize the consumer which verifies it is ready and will attempt to create a schema.
     *
     * @param ds
     *            the datasource
     * @param sg
     *            the sql generator to use - if <code>null</code>, one is created that uses the default schema
     */
    protected void initialize(DataSource ds, SqlGenerator sg) {
        setDataSource(ds);
        setSqlGenerator(sg);
        getSqlGenerator().createSchema(getDataSource());
    }

    @Override
    protected void onBasicMessage(EventRecord eventRecord) {

        final String sql = this.sqlGenerator.generateSql(eventRecord);

        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement stmt = conn.createStatement();
                int rowsAffected = stmt.executeUpdate(sql);
                if (rowsAffected != 1) {
                    throw new SQLException(String.format("%d rows were inserted!", rowsAffected));
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            getLog().error("Event record did not properly get inserted into datasource using sql [%s]", sql, e);
        }
    }
}
