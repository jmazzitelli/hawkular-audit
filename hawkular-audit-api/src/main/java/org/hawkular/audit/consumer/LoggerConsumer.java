package org.hawkular.audit.consumer;

import java.util.Map;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.Subsystem;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.jboss.logging.Logger;

/**
 * A very simple audit record consumer that merely logs the audit record via a logger.
 * 
 * @author John Mazzitelli
 */
public class LoggerConsumer extends BasicMessageListener<AuditRecord> {
    @Override
    protected void onBasicMessage(AuditRecord basicMessage) {
        final MessageId id = basicMessage.getMessageId();
        final MessageId corId = basicMessage.getCorrelationId();
        final String msg = basicMessage.getMessage();
        final Map<String, String> details = basicMessage.getDetails();
        final Subsystem subsystem = basicMessage.getSubsystem();
        final long timestamp = basicMessage.getTimestamp();

        Logger logger = Logger.getLogger("AUDIT." + subsystem);
        logger.infof("id=[%s], correlation=[%s], timestamp=[%d], subsystem=[%s], msg=[%s], details=[%s],", id, corId,
                timestamp, subsystem, msg, details);
    }
}
