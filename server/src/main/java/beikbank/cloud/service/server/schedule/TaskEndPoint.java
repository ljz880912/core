package beikbank.cloud.service.server.schedule;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@ConfigurationProperties(
        prefix = "common.schedule"
)
public class TaskEndPoint  extends AbstractEndpoint<List<TaskStatics>> {

    private final TaskRepository taskRepository;

    public TaskEndPoint(TaskRepository taskRepository) {
        super("task", false, true);
        Assert.notNull(taskRepository, "Repository must not be null");
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskStatics> invoke() {
        return this.taskRepository.findAll();
    }
}
