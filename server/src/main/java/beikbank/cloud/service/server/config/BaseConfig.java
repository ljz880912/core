package beikbank.cloud.service.server.config;

import beikbank.cloud.service.server.BaseProps;
import beikbank.cloud.service.server.ServiceInfo;
import beikbank.cloud.service.server.filter.BkAuthFilter;
import beikbank.cloud.service.server.filter.CacheControlFilter;
import beikbank.cloud.service.server.filter.ratelimit.RateLimitFilter;
import beikbank.cloud.service.server.service.BaseEnvironment;
import beikbank.cloud.service.server.service.BaseErrorContentService;
import beikbank.cloud.service.server.service.RedisService;
import com.beikbank.authentication.center.client.BeikBankAucClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 基本配置
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:34 2018/4/10
 */
//@Configuration
public abstract class BaseConfig extends SpringBootServletInitializer{

    private static final Logger log = LoggerFactory.getLogger(BaseConfig.class);

//    @Autowired
//    private BaseEnvironment baseEnvironment;
//
//    @Autowired(required = false)
//    private RedisService redisService;
//
//    @Autowired
//    private BaseErrorContentService errorContentService;



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BaseConfig.class);
    }

//    @Bean
//    public BeikBankAucClient cifClient() {
//        return BeikBankAucClient.instance(BaseProps.clientOptions(), BaseProps.aucTag(), BaseProps.serviceToken());
//    }

//    /**
//     * 请求速率限制
//     * @return
//     */
//    @Bean
//    @ConditionalOnProperty(
//            name = {"ratelimit.enabled"},
//            havingValue = "true"
//    )
//    public FilterRegistrationBean rateLimitFilter() {
//        RateLimitFilter rateLimitFilter = new RateLimitFilter(BaseProps.serviceName(), this.redisService, this.baseEnvironment, this.errorContentService);
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(rateLimitFilter);
//        registrationBean.setOrder(-2147483645);
//        return registrationBean;
//    }


//    @Bean
//    @ConditionalOnProperty(
//            name = {"error.mapping.enabled", "common-service.tag", "org.code"}
//    )
//    public FilterRegistrationBean errorMappingFilterRegistrationBean() {
//        String tag = this.env.getProperty("common-service.tag");
//        CommonServiceClient commonServiceClient = CommonServiceClientFactory.instance(BaseProps.clientOptions(), tag, BaseProps.serviceToken());
//        ErrorMappingFilter errorMappingFilter = new ErrorMappingFilter(commonServiceClient, this.env.getProperty("org.code"), this.env.getProperty("moxie.cloud.service.name"));
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(errorMappingFilter);
//        registrationBean.setOrder(-2147483646);
//        return registrationBean;
//    }


    /**
     * 缓存机制
     * @return
     */
    @Bean
    public FilterRegistrationBean cacheControlFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        CacheControlFilter cacheControlFilter = new CacheControlFilter();
        registrationBean.setFilter(cacheControlFilter);
        registrationBean.setOrder(-2147483648);
        return registrationBean;
    }

    @Bean
    @ExportMetricWriter
    @ConditionalOnProperty({"metrics.statsd.host"})
    MetricWriter metricWriter() {
        return new StatsdMetricWriter(BaseProps.serviceName(), BaseProps.statsdHost(), BaseProps.statsdPort().intValue());
    }

    public abstract ServiceInfo getServiceInfo();
}
