package beikbank.cloud.service.server.consul;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@Data
public class ConsulConfigProperties {

    @Value("${server.port:8080}")
    private int servicePort;
    @Value("${beikbank.cloud.consul.checks.ttl:30}")
    private int checkTTL;
    /**
     * 查看http间隔
     */
    @Value("${beikbank.cloud.consul.checks.http.interval:60}")
    private int httpInterval;
    @Value("${beikbank.cloud.consul.checks.http.url}")
    private String httpUrl;
    @Value("${external.port:${server.port}}")
    private int externalPort;
    @Value("${service.tag:local}")
    private String serviceTag;
    @Value("${consul.agent.ip:localhost}")
    private String hostIp;
    @Value("${consul.acl_token:anonymous}")
    private String token;
}
