package beikbank.cloud.service.server.model;

import lombok.Data;
import org.slf4j.MDC;

/**
 * 错误内容
 * @author : liujianzhao
 * @version :
 * @date: Create in 15:42 2018/4/10
 */
@Data
public class ErrorContent {

    private String code;

    private String msg;

    private Integer httpCode;

    public ErrorContent(String code, String msg, Integer httpCode) {
        this.code = code;
        this.msg = msg;
        this.httpCode = httpCode;
    }

    public ExceptionResp exceptionResp() {
        return new ExceptionResp(this.getCode(), this.getMsg(), MDC.get("CORRELATION_ID"));
    }
}
