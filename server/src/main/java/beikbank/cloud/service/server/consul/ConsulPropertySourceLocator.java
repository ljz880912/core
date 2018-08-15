package beikbank.cloud.service.server.consul;

import com.ecwid.consul.v1.ConsulClient;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@Data
public class ConsulPropertySourceLocator implements PropertySourceLocator{

    private static final Logger log = LoggerFactory.getLogger(ConsulPropertySourceLocator.class);

    @Override
    public PropertySource<?> locate(Environment environment) {
        if(environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment)environment;
            String serviceName = env.getProperty("beikbank.cloud.service.name");
            String serviceTag = env.getProperty("service.tag");
            if("local".equalsIgnoreCase(serviceTag)) {
                return null;
            } else {
                env.getPropertySources().remove("applicationConfigurationProperties");
                String consulKey = String.format("service/%s/%s/config", new Object[]{serviceName, serviceTag});

                try {
                    ConsulClient consul = new ConsulClient("http://localhost", 8500);
                    return new ConsulPropertySource(consulKey, env.getProperty("consul.acl_token"), consul);
                } catch (Throwable var7) {
                    throw new RuntimeException("初始化Consul PropertySource异常", var7);
                }
            }
        } else {
            return null;
        }
    }
}
