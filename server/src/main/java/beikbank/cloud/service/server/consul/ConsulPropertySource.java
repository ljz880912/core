package beikbank.cloud.service.server.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class ConsulPropertySource extends MapPropertySource{

    private static final Logger log = LoggerFactory.getLogger(ConsulPropertySource.class);
    private DynamicConfiguration props;

    public ConsulPropertySource(String configKey, String token, ConsulClient consul) {
        super(configKey, Maps.newHashMap());
        this.props = this.getDynamicConfiguration(configKey, token, consul);
        Iterator it = this.props.getKeys();

        while(it.hasNext()) {
            String kk = (String)it.next();
            (this.source).put(kk, this.props.getProperty(kk));
        }
    }

    private DynamicConfiguration getDynamicConfiguration(String configKey, String token, ConsulClient consul) {
        PolledConfigurationSource polledSource = new PolledConsulConfigurationSource(configKey, token, consul);
        return new DynamicConfiguration(polledSource, new FixedDelayPollingScheduler());
    }

    @Override
    public Object getProperty(String name) {
        return this.props.getProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
        return Iterators.toArray(this.props.getKeys(), String.class);
    }
}
