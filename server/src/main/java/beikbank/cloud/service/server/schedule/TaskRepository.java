package beikbank.cloud.service.server.schedule;

import com.beikbank.common.BaseDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class TaskRepository {

    private static final Logger log = LoggerFactory.getLogger(TaskRepository.class);
    private int capacity = 100;
    private final List<TaskStatics> taskStatics = new LinkedList();
    public static final String SUCCESS_FLAG = "success";
    public static final String FAIL_FLAG = "fail";
    private boolean reverse = true;

    public void setCapacity(int capacity) {
        List var2 = this.taskStatics;
        synchronized(this.taskStatics) {
            this.capacity = capacity;
        }
    }

    public List<TaskStatics> findAll() {
        List var1 = this.taskStatics;
        synchronized(this.taskStatics) {
            if(!CollectionUtils.isEmpty(this.taskStatics)) {
                this.taskStatics.forEach((taskStatic) -> {
                    if(this.isTimeout(taskStatic.getStartTime(), taskStatic.getEndTime(), taskStatic.getTimeout())) {
                        taskStatic.setStatus(FAIL_FLAG);
                        taskStatic.setFailReason("task execute timeout");
                        if(taskStatic.getStatus().equalsIgnoreCase(SUCCESS_FLAG) || taskStatic.getStatus().equalsIgnoreCase(FAIL_FLAG)) {
                            this.taskStatics.remove(taskStatic);
                        }
                    }

                });
            }

            return Collections.unmodifiableList(new ArrayList(this.taskStatics));
        }
    }

    public void add(TaskStatics taskStatics) {
        log.debug("add taskStatic:{}", taskStatics);
        List var2 = this.taskStatics;
        synchronized(this.taskStatics) {
            if(!CollectionUtils.isEmpty(this.taskStatics)) {
                this.taskStatics.forEach((taskStatic) -> {
                    if(this.isTimeout(taskStatic.getStartTime(), taskStatic.getEndTime(), taskStatic.getTimeout())) {
                        this.taskStatics.remove(taskStatic);
                    }

                });
            }

            while(this.taskStatics.size() >= this.capacity) {
                this.taskStatics.remove(this.reverse?this.capacity - 1:0);
            }

            if(this.reverse) {
                this.taskStatics.add(0, taskStatics);
            } else {
                this.taskStatics.add(taskStatics);
            }

        }
    }

    private boolean isTimeout(Date startDate, Date endDate, long timeout) {
        endDate = endDate == null? BaseDateUtils.getCurrentDate():endDate;
        long timeMilli = endDate.getTime() - startDate.getTime();
        return timeMilli > 0L && timeMilli >= timeout;
    }

    public void update(TaskStatics taskStatic) {
        if(this.taskStatics.contains(taskStatic)) {
            log.debug("update taskStatic:{}", this.taskStatics);
            int index = this.taskStatics.indexOf(taskStatic);
            taskStatic.setEndTime(new Date());
            this.taskStatics.add(index, taskStatic);
        }

    }
}
