package beikbank.cloud.service.server;


import beikbank.cloud.service.client.ClientFactory;
import beikbank.cloud.service.common.constants.BasePropKeys;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * Created by Administrator on 2018/4/10/010.
 */
@Data
public class BaseProps {

    private static Environment env;

    public static void setEnvironment(Environment enviroment) {
        env = enviroment;
    }

    public static Environment getEnvironment() {
        return env;
    }

    public static String serviceName() {
        return env.getProperty(BasePropKeys.SERVICE_NAME);
    }

    public static String serviceTag() {
        return System.getProperty(BasePropKeys.SERVICE_TAG);
    }

    public static Integer serverPort() {
        return Integer.valueOf(System.getProperty(BasePropKeys.SERVER_PORT));
    }

    public static String aucTag() {
        return StringUtils.defaultString(env.getProperty(BasePropKeys.AUC_TAG), serviceTag());
    }

    public static String serviceToken() {
        return env.getProperty(BasePropKeys.SERVICE_TOKEN);
    }

    public static boolean isLocal() {
        return "local".equalsIgnoreCase(serviceTag());
    }

    public static String consulCheckUrl() {
        return env.getProperty(BasePropKeys.CONSUL_CHECK_URL);
    }

    public static Integer consulCheckInterval() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.CONSUL_CHECK_INTERVAL), "10"));
    }

    public static Integer consulCheckTtl() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.CONSUL_CHECK_TTL), "30"));
    }

    public static String consulToken() {
        return env.getProperty(BasePropKeys.CONSUL_TOKEN);
    }

    public static String statsdHost() {
        return env.getProperty(BasePropKeys.METRICS_STATSD_HOST);
    }

    public static Integer statsdPort() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.METRICS_STATSD_PORT), "8125"));
    }

    public static Integer clientConnTimeout() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.CLIENT_CONN_TIMEOUT), "2000"));
    }

    public static Integer clientReadTimeout() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.CLIENT_READ_TIMEOUT), "5000"));
    }

    public static Integer clientMaxConn() {
        return Integer.valueOf(StringUtils.defaultString(env.getProperty(BasePropKeys.CLIENT_MAX_CONN), "50"));
    }

    public static ClientFactory.Options clientOptions() {
        return new ClientFactory.Options() {
            @Override
            public String userAgent() {
                return BaseProps.serviceName();
            }
            @Override
            public long connectTimeout() {
                return (long)BaseProps.clientConnTimeout().intValue();
            }
            @Override
            public long readTimeout() {
                return (long)BaseProps.clientReadTimeout().intValue();
            }
            @Override
            public long maxConnections() {
                return (long)BaseProps.clientMaxConn().intValue();
            }
            @Override
            public long maxConnectionsPerRoute() {
                return 100L;
            }
        };
    }

}
