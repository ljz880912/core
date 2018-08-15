package beikbank.cloud.service.server.listener;

import beikbank.cloud.service.server.BaseProps;
import beikbank.cloud.service.server.RenamingProcessor;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 9:46 2018/4/11
 */
public class ApplicationEventListener implements ApplicationListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            BaseProps.setEnvironment(((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment());
        } else if (applicationEvent instanceof ApplicationReadyEvent && !((ApplicationReadyEvent)applicationEvent).getApplicationContext().getId().contains("bootstrap")) {
            this.handleApplicationReadyEvent((ApplicationReadyEvent)applicationEvent);
        }
    }

    private void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        RequestMappingHandlerAdapter handlerAdapter = (RequestMappingHandlerAdapter)context.getBean(RequestMappingHandlerAdapter.class);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList();
        resolvers.add(new RenamingProcessor(handlerAdapter));
        resolvers.addAll(handlerAdapter.getArgumentResolvers());
        handlerAdapter.setArgumentResolvers(resolvers);
        String serviceTag = BaseProps.serviceTag();
        if (serviceTag.equals("local")) {
            log.info("服务以本地模式启动...");
        } else {
            log.info("服务以tag[{}]启动, 将注册到consul...", BaseProps.serviceTag());
            ConsulClient consul = (ConsulClient)context.getBean(ConsulClient.class);

            try {
                NewService newService = new NewService();
                String serviceId = BaseProps.serviceName() + "-" + BaseProps.serverPort();
                newService.setId(serviceId);
                newService.setName(BaseProps.serviceName());
                newService.setPort(BaseProps.serverPort());
                newService.setTags(Lists.newArrayList(new String[]{BaseProps.serviceTag()}));
                NewService.Check check = new NewService.Check();
                check.setHttp(BaseProps.consulCheckUrl());
                check.setInterval(BaseProps.consulCheckInterval() + "s");
                check.setDeregisterCriticalServiceAfter("12h");
                newService.setCheck(check);
                consul.agentServiceRegister(newService);
                log.info("服务[{}]成功注册到consul...", BaseProps.serviceName());
            } catch (Throwable var10) {
                log.error("服务注册consul过程异常", var10);
            }

        }
    }
}
