package beikbank.cloud.service.server.schedule;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@Data
public class TaskStatics {

    private String key;
    private String status;
    private String failReason;
    private String condition;
    private Date startTime;
    private Date endTime;
    private long timeout;
    private Map extra;

    public TaskStatics(String key) {
        this.key = key;
    }

    public TaskStatics() {
    }
}
