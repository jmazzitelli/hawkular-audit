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
