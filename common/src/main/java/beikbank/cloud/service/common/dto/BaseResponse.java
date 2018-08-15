package beikbank.cloud.service.common.dto;

import lombok.Data;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 16:18 2018/4/9
 */
@Data
public class BaseResponse {

    private String code="0000000";

    private String resMsg;

    /**
     * http状态码
     */
    private int httpCode=200;
}
