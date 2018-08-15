package beikbank.cloud.service.server.consul;

import beikbank.cloud.service.client.discovery.ConsulDiscoveryService;
import com.ecwid.consul.v1.ConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@PropertySource({"classpath:bootstrap.properties"})
@Configuration
public class ConsulConfig {

    private static final Logger log = LoggerFactory.getLogger(ConsulConfig.class);
    @Autowired
    private Environment env;

    @Bean
    public ConsulClient consulClient() {
        return this.isLocal()?null:new ConsulClient("http://localhost", 8500);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulConfigProperties consulConfigProperties() {
        return new ConsulConfigProperties();
    }

    @Bean
    public ConsulPropertySourceLocator consulPropertySource() {
        return new ConsulPropertySourceLocator();
    }

    @Bean
    public ConsulDiscoveryService consulDiscovery() {
        return this.isLocal()?null:new ConsulDiscoveryService();
    }

    private boolean isLocal() {
        return "local".equalsIgnoreCase(this.env.getProperty("service.tag"));
    }
}
