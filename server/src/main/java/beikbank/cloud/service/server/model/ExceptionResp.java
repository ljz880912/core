package beikbank.cloud.service.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 16:05 2018/4/10
 */
@Data
public class ExceptionResp {

    @JsonProperty("correlation_id")
    private String correlationId;

    private String code;

    private String msg;

    public ExceptionResp(){

    }

    public ExceptionResp(String code,String msg,String correlationId){
        this.code=code;
        this.msg=msg;
        this.correlationId=correlationId;
    }
}
