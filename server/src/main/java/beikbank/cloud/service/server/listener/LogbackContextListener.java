package beikbank.cloud.service.server.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.StringReader;
import java.util.Properties;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:23 2018/4/11
 */
public class LogbackContextListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(LogbackContextListener.class);
    private boolean started = false;

    @Override
    public void start() {
        if (!this.started) {
            String consulKey = "common/" + System.getProperty("service.tag") + "/config";
            Properties props = new Properties();
            if (!System.getProperty("service.tag").equals("local")) {
                ConsulClient consulClient = new ConsulClient();
                GetValue value;
                if (StringUtils.isBlank(System.getProperty("consul.token"))) {
                    value = consulClient.getKVValue(consulKey).getValue();
                } else {
                    value = consulClient.getKVValue(consulKey, System.getProperty("consul.token")).getValue();
                }

                if (value != null) {
                    try {
                        props.load(new StringReader(new String(Base64Utils.decodeFromString(value.getValue()), "UTF-8")));
                    } catch (Throwable var7) {
                        log.error("加载consul key:common/log/config配置异常", var7);
                    }
                }
            } else {
                try {
                    props.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
                } catch (Throwable var6) {
                    log.error("加载本地application.properties异常", var6);
                }
            }

            if (props != null) {
                props.entrySet().forEach((prop) -> {
                    this.getContext().putProperty((String)prop.getKey(), (String)prop.getValue());
                    System.setProperty((String)prop.getKey(), (String)prop.getValue());
                });
                props.clear();
            }

            this.started = true;
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {
    }
}
