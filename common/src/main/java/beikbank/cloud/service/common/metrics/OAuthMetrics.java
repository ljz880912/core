package beikbank.cloud.service.common.metrics;

import com.timgroup.statsd.StatsDClient;

/**
 * oauth鉴权监控
 * @author : liujianzhao
 * @version :
 * @date: Create in 19:40 2018/4/9
 */
public class OAuthMetrics extends MetricsBase{

    protected OAuthMetrics(StatsDClient statsDClient, String prefix) {
        super(statsDClient, prefix);
    }

    /**
     * oauth验证通过返回
     * @param status
     */
    public void validationStatus(int status) {
        this.statsDClient.increment(this.start("oauth.validation.response", new Object[]{status}));
    }

    /**
     * 保持oauth验证通过的时间
     * @param timeInMilliSecond
     */
    public void validationDuration(long timeInMilliSecond) {
        this.statsDClient.time(this.start("oauth.validation.duration", new Object[0]), timeInMilliSecond);
    }

    /**
     * 账户终止提醒
     */
    public void accountExpiryWarning() {
        this.statsDClient.increment("oauth.account.expiry");
    }
}
