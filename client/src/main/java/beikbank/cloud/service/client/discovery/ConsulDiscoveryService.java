package beikbank.cloud.service.client.discovery;

import beikbank.cloud.service.common.exception.CoreRuntimeException;
import com.google.common.base.Splitter;
import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class ConsulDiscoveryService implements DiscoveryService{

    private static final Logger log = LoggerFactory.getLogger(ConsulDiscoveryService.class);
    private static final String DEFAULT_CONSUL_IP = "127.0.0.1";
    private static final int DEFAULT_CONSUL_DNS_PORT = 8600;
    private static int CACHED_ITEMS_MAX_SIZE = 1000;
    private static int CACHED_ITEMS_REFRESH_INTERVAL = 5;
    private ConsulDnsResolver consulDnsResolver;
    private String consulHost;
    private int consulPort;
    private String tag;
    private LoadingCache<String, List<ServiceAddress>> serverCache;

    @Override
    public URI getOptimizedURI(URI var1) {
        return null;
    }

    public ConsulDiscoveryService(String host, int port) {
        this.serverCache = CacheBuilder.newBuilder().maximumSize((long)CACHED_ITEMS_MAX_SIZE).refreshAfterWrite((long)CACHED_ITEMS_REFRESH_INTERVAL, TimeUnit.SECONDS).removalListener(new ConsulDiscoveryService.CacheRemoveListener()).build(new CacheLoader<String, List<ServiceAddress>>() {
            public List<ServiceAddress> load(String fqdn) throws Exception {
                List<ServiceAddress> serverList = ConsulDiscoveryService.this.queryDNS(fqdn);
                if (serverList.isEmpty()) {
                    ConsulDiscoveryService.log.error("No node found, FQDN = {}", fqdn);
                    throw new CoreRuntimeException("No node found for " + fqdn);
                } else {
                    return serverList;
                }
            }
        });
        this.consulHost = host;
        this.consulPort = port;
        this.consulDnsResolver = new ConsulDnsResolver(this.consulHost, this.consulPort);
    }

    public ConsulDiscoveryService() {
        this(DEFAULT_CONSUL_IP, DEFAULT_CONSUL_DNS_PORT);
    }

    public List<ServiceAddress> queryDNS(String fqdn) throws IOException {
        if (this.consulDnsResolver != null) {
            List<String> nameParts = Splitter.on(".").limit(3).splitToList(fqdn);
            int size = nameParts.size();
            List result;
            if (size == 1) {
                result = this.consulDnsResolver.resolve(fqdn, "");
            } else if (size == 2) {
                result = this.consulDnsResolver.resolve(nameParts.get(0), nameParts.get(1));
            } else {
                result = this.consulDnsResolver.resolve(nameParts.get(0), nameParts.get(1), nameParts.get(2));
            }

            return result;
        } else {
            return null;
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private class CacheRemoveListener implements RemovalListener<String, List<ServiceAddress>> {
        private CacheRemoveListener() {
        }

        @Override
        public void onRemoval(RemovalNotification<String, List<ServiceAddress>> removalNotification) {
            ConsulDiscoveryService.log.info("Remove expired cache. serviceName: {}", removalNotification.getKey());
        }
    }
}
