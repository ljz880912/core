package beikbank.cloud.service.server.service;

import beikbank.cloud.service.server.consul.PolledConsulConfigurationSource;
import com.ecwid.consul.v1.ConsulClient;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:36 2018/4/10
 */
@Component
public class BaseEnvironment {

    private static final Logger log = LoggerFactory.getLogger(BaseEnvironment.class);
    @Value("${beikbank.cloud.service.name}")
    private String serviceName;
    @Value("${service.tag:local}")
    private String serviceTag;
    @Value("${dynamic.properties.files:}")
    private String dynamicFiles;
    @Value("${dynamic.properties.refresh.interval:60}")
    private Integer refreshInterval;
    @Autowired
    private Environment env;

    @Autowired
    private ConsulClient consul;

    /**
     * 定时执行器，从配置源中获取配置信息，然后设置到配置对象中
     */
    private FixedDelayPollingScheduler scheduler;

    /**
     * 动态配置
     */
    private DynamicConfiguration dynamicErrorProps;
    private Map<String, DynamicConfiguration> dynamicFileProps = new ConcurrentHashMap();
    private Properties errorProps;
    private Map<String, Properties> fileProps = new ConcurrentHashMap();

    @PostConstruct
    public void init() {
        this.scheduler = new FixedDelayPollingScheduler(10000, this.refreshInterval * 1000, false);
        if (!StringUtils.equalsIgnoreCase(this.serviceTag, "local")) {
            this.dynamicErrorProps = this.getDynamicConfiguration("error");
            Arrays.stream(this.dynamicFiles.split(",")).forEach((fileName) -> {
                DynamicConfiguration config = this.getDynamicConfiguration(fileName);
                this.dynamicFileProps.put(fileName, config);
            });
        }

        this.errorProps = this.loadLocalProperties("error.properties");
        if (StringUtils.isNotBlank(this.dynamicFiles)) {
            Arrays.stream(this.dynamicFiles.split(",")).forEach((fileName) -> {
                Properties prop = this.loadLocalProperties(fileName);
                this.fileProps.put(fileName, prop);
            });
        }

    }

    private DynamicConfiguration getDynamicConfiguration(String fileName) {
        String configKey = "service/" + this.serviceName + "/" + this.serviceTag + "/" + fileName;
        PolledConfigurationSource polledSource = new PolledConsulConfigurationSource(configKey, this.env.getProperty("consul.acl_token"), this.consul);
        return new DynamicConfiguration(polledSource, this.scheduler);
    }

    private Properties loadLocalProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        } else {
            if (!fileName.contains(".properties")) {
                fileName = fileName + ".properties";
            }

            Properties props = null;

            try {
                URL url = this.getClass().getClassLoader().getResource(fileName);
                if (url != null) {
                    props = PropertiesLoaderUtils.loadProperties(new EncodedResource(new UrlResource(url), "utf-8"));
                }
            } catch (Throwable var4) {
                log.error("加载本地配置文件[{}]异常: {}", fileName, ExceptionUtils.getStackTrace(var4));
            }

            if (props == null) {
                log.warn("缺少配置文件:" + fileName);
            }

            return props;
        }
    }

    public String getProperty(String key) {
        return this.env.getProperty(key);
    }

    public String getError(String errorKey) {
        if (this.dynamicErrorProps != null && this.dynamicErrorProps.containsKey(errorKey)) {
            return (String)this.dynamicErrorProps.getProperty(errorKey);
        } else if (this.errorProps != null) {
            return this.errorProps.getProperty(errorKey);
        } else {
            log.warn("无法从错误信息配置文件中找到key[{}]的值", errorKey);
            return null;
        }
    }

    public String getProperty(String fileName, String key) {
        if (this.dynamicFileProps.containsKey(fileName) && (this.dynamicFileProps.get(fileName)).getProperties() != null && (this.dynamicFileProps.get(fileName)).getProperties().size() != 0) {
            return (String)(this.dynamicFileProps.get(fileName)).getProperty(key);
        } else if (this.fileProps.containsKey(fileName)) {
            return (this.fileProps.get(fileName)).getProperty(key);
        } else {
            log.warn("无法从配置文件[{}]中找到key[{}]的值", fileName, key);
            return null;
        }
    }

    public Properties getProperties(String fileName) {
        if (this.dynamicFileProps.containsKey(fileName) && (this.dynamicFileProps.get(fileName)).getProperties() != null && (this.dynamicFileProps.get(fileName)).getProperties().size() != 0) {
            return (this.dynamicFileProps.get(fileName)).getProperties();
        } else if (this.fileProps.containsKey(fileName)) {
            return this.fileProps.get(fileName);
        } else {
            log.warn("无法找到配置文件[{}]", fileName);
            return null;
        }
    }

    public Environment getInnerEnvironment() {
        return this.env;
    }
}
