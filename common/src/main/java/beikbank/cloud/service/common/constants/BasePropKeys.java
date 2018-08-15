package beikbank.cloud.service.common.constants;

/**
 * @author : liujianzhao
 * @version : 1.0
 * @date: Create in 16:14 2018/4/9
 */
public interface BasePropKeys {

    String SERVICE_NAME = "beikbank.cloud.service.name";
    String SERVICE_TAG = "service.tag";
    String SERVER_PORT = "server.port";
    String SWAGGER_ENABLED = "swagger.enabled";
    String CONSUL_CHECK_URL = "beikbank.cloud.consul.checks.http.url";
    String CONSUL_CHECK_INTERVAL = "beikbank.cloud.consul.checks.http.interval";
    String CONSUL_CHECK_TTL = "beikbank.cloud.consul.checks.ttl";
    String CONSUL_TOKEN = "consul.acl_token";
    String METRICS_STATSD_HOST = "metrics.statsd.host";
    String METRICS_STATSD_PORT = "metrics.statsd.port";
    String CLIENT_CONN_TIMEOUT = "client.default.conn.timeout";
    String CLIENT_READ_TIMEOUT = "client.default.read.timeout";
    String CLIENT_MAX_CONN = "client.default.max.conn";
    String SERVICE_TOKEN = "service.token";
    String AUC_TAG = "auc.tag";
    String COMMON_SERVICE_TAG = "common-service.tag";
    String ORG_CODE = "org.code";
    String RATE_LIMIT_ENABLED = "ratelimit.enabled";
}
