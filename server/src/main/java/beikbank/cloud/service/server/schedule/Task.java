package beikbank.cloud.service.server.schedule;

import java.util.Map;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public interface Task {

    default TaskStatics notifyTask() {
        return null;
    }

    void run(Map var1) throws Exception;
}
