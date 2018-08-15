package beikbank.cloud.service.server.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.boot.actuate.metrics.CounterService;

/**
 * log日志监控附加器
 *
 * @author : liujianzhao
 * @version :
 * @date: Create in 21:18 2018/4/9
 */
public class LoggingMetricsAppender extends AppenderBase<ILoggingEvent> {

    private CounterService counterService;

    public LoggingMetricsAppender(CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {

    }
}
