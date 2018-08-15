package beikbank.cloud.service.common.metrics;

import com.timgroup.statsd.StatsDClient;

/**
 * 限流监控
 * @author : liujianzhao
 * @version :
 * @date: Create in 19:44 2018/4/9
 */
public class RateLimitMetrics extends MetricsBase{

    protected RateLimitMetrics(StatsDClient statsDClient, String prefix) {
        super(statsDClient, prefix);
    }

    public void countStatus(int httpStatus) {
        this.statsDClient.increment(this.start("rate_limit_filter.response", new Object[]{httpStatus}));
    }

    public void sampleDuration(long ms, int httpStatus) {
        this.statsDClient.recordExecutionTime(this.start("rate_limit_filter.http.status", new Object[]{httpStatus, "duration"}), ms, 0.1D);
    }

    public void timeStatus200Duration(boolean isUserAuth, long ms) {
        if (isUserAuth) {
            this.statsDClient.time(this.start("rate_limit_filter.user.auth.duration", new Object[0]), ms);
        } else {
            this.statsDClient.time(this.start("rate_limit_filter.service.auth.duration", new Object[0]), ms);
        }

    }
}
