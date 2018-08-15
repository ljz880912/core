package beikbank.cloud.service.common.metrics;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.timgroup.statsd.StatsDClient;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 19:29 2018/4/9
 */
public abstract class MetricsBase {

    protected final StatsDClient statsDClient;
    protected final String prefix;
    private static final Joiner joiner = Joiner.on('.');

    protected MetricsBase(StatsDClient statsDClient, String prefix) {
        this.statsDClient = Preconditions.checkNotNull(statsDClient);
        this.prefix = Preconditions.checkNotNull(prefix);
    }

    protected String start(Object part, Object... parts) {
        return joiner.join(this.prefix, part, parts);
    }
}
