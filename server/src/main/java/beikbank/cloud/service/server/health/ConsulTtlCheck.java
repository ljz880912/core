package beikbank.cloud.service.server.health;

import beikbank.cloud.service.server.BaseProps;
import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@ConditionalOnProperty({"beikbank.cloud.consul.checks.ttl"})
public class ConsulTtlCheck {

    private static final Logger log = LoggerFactory.getLogger(ConsulTtlCheck.class);
    public static final int INTERVAL = 10;
    @Autowired
    private ConsulClient consul;

    @Scheduled(
            fixedRate = 10000L
    )
    public void reportStatus() {
        String serviceId = BaseProps.serviceName() + "-" + BaseProps.serverPort();

        try {
            log.debug("heartbeat to consul with serviceId[{}].", serviceId);
            this.consul.agentCheckPass(serviceId, "OK");
        } catch (ConsulException var3) {
            log.error("agentCheckPass to consul failed", var3);
        }

    }
}
