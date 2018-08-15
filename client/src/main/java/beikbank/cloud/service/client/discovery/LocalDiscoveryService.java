package beikbank.cloud.service.client.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 14:11 2018/4/12
 */
public class LocalDiscoveryService implements DiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(LocalDiscoveryService.class);
    private Map<URI, URI> localDiscoveryURIMap = null;

    public LocalDiscoveryService(Map<URI, URI> map) {
        if (map != null) {
            this.localDiscoveryURIMap = Collections.unmodifiableMap(map);
        }

    }

    @Override
    public URI getOptimizedURI(URI url) {
        try {
            if (url != null && this.localDiscoveryURIMap != null) {
                URI optimizedUrl = (URI)this.localDiscoveryURIMap.get(url);
                return optimizedUrl != null ? optimizedUrl : this.optimizeParentPart(url);
            } else {
                return url;
            }
        } catch (Exception var3) {
            log.warn("Exception while trying to get optimized url: ", var3);
            return url;
        }
    }

    private URI optimizeParentPart(URI fullUri) throws MalformedURLException {
        Optional<Map.Entry<URI, URI>> matchingUrl = this.localDiscoveryURIMap.entrySet().stream().filter((url) -> {
            return fullUri.toString().contains(((URI)url.getKey()).toString());
        }).findFirst();
        return matchingUrl.isPresent() ? URI.create(fullUri.toString().replace(((URI)((Map.Entry)matchingUrl.get()).getKey()).toString(), ((URI)((Map.Entry)matchingUrl.get()).getValue()).toString())) : fullUri;
    }
}
