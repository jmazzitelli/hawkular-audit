package org.hawkular.audit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.ConnectionFactory;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.AuditRecordProcessor;
import org.hawkular.audit.common.Subsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class AuditStartupBean {
    private final Logger log = LoggerFactory.getLogger(AuditStartupBean.class);

    @Resource(mappedName = "java:/HawkularBusConnectionFactory")
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() throws Exception {
        log.info("Audit subsystem starting up");
        AuditRecordProcessor processor = new AuditRecordProcessor(this.connectionFactory);
        AuditRecord record = new AuditRecord("Audit subsystem starting up", new Subsystem("STARTUP"));
        processor.sendAuditRecord(record);
    }
}
