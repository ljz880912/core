package beikbank.cloud.service.server.schedule;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@ConditionalOnProperty(
        prefix = "common.schedule",
        value = {"enabled"},
        havingValue = "true"
)
@ConfigurationProperties(
        prefix = "common.schedule"
)
@Configuration
public class SchedulerAutoConfiguration {

    public static final String DEFAULT_PATH = "/schedule";
    private String path;
    private BeanFactory beanFactory;

    public SchedulerAutoConfiguration(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @ConditionalOnMissingBean({TaskRepository.class})
    @Bean
    public TaskRepository taskRepository() {
        return new TaskRepository();
    }

    @ConditionalOnMissingBean({SchedulerManager.class})
    @Bean
    public SchedulerManager schedulerManager() {
        this.path = this.path == null?"/schedule":this.path;
        SchedulerManager schedulerManager = new SchedulerManager(this.beanFactory, this.taskRepository(), this.path);
        return schedulerManager;
    }

    @ConditionalOnMissingBean({SchedulerFilter.class})
    @Bean
    public SchedulerFilter schedulerFilter() {
        SchedulerFilter schedulerFilter = new SchedulerFilter(this.schedulerManager());
        return schedulerFilter;
    }

    @ConditionalOnMissingBean({TaskEndPoint.class})
    @Bean
    public TaskEndPoint taskEndpoint() {
        return new TaskEndPoint(this.taskRepository());
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
