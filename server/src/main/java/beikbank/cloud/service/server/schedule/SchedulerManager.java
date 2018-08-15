package beikbank.cloud.service.server.schedule;

import beikbank.cloud.service.server.model.ExceptionResp;
import com.beikbank.common.BaseJsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class SchedulerManager {

    private final BeanFactory beanFactory;
    private final TaskRepository taskRepository;
    private static final long DEFAULT_TIMEOUT = 3600000L;
    private String path;

    public SchedulerManager(BeanFactory beanFactory, TaskRepository taskRepository, String path) {
        this.beanFactory = beanFactory;
        this.taskRepository = taskRepository;
        this.path = path;
    }

    public boolean include(String requestPath) {
        return this.path.equalsIgnoreCase(requestPath);
    }

    public void schedule(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TaskConfig taskConfig = null;
        byte[] bytes = IOUtils.toByteArray(request.getInputStream());
        if(bytes != null && bytes.length > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            taskConfig = objectMapper.readValue(bytes, TaskConfig.class);
        }

        if(taskConfig != null && taskConfig.getBeanName() != null) {
            Task bean;
            try {
                bean = this.beanFactory.getBean(taskConfig.getBeanName(), Task.class);
            } catch (BeansException var10) {
                this.sendError(response, 404, new ExceptionResp("9999", "bean not found", (String) MDC.get("CORRELATION_ID")));
                return;
            }

            TaskStatics taskStatics = new TaskStatics();
            taskStatics.setKey(taskConfig.getJobKey());
            taskStatics.setStartTime(new Date());
            long timeout = taskConfig.getTimeout() == 0?3600000L:(long)taskConfig.getTimeout();
            taskStatics.setTimeout(timeout);
            taskStatics.setStatus(TaskExecStatus.INIT.name());
            this.taskRepository.add(taskStatics);
            Map data = taskConfig.getData();
            (new Thread(() -> {
                try {
                    bean.run(data);
                    taskStatics.setStatus(TaskExecStatus.SUCCESS.name());
                    this.taskRepository.update(taskStatics);
                } catch (Exception var6) {
                    String msg = var6.getMessage() == null?"task execute exception":var6.getMessage();
                    taskStatics.setFailReason(msg);
                    taskStatics.setStatus(TaskExecStatus.FAIL.name());
                    this.taskRepository.update(taskStatics);
                }

            })).start();
        }

    }

    private void sendError(HttpServletResponse response, int httpStatus, ExceptionResp exceptionResp) throws IOException {
        response.reset();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8.toString());
        response.setStatus(httpStatus);
        String errorStr = BaseJsonUtils.defaultMapper().writeValueAsString(exceptionResp);
        response.getOutputStream().write(errorStr.getBytes());
    }
}
