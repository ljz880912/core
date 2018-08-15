package beikbank.cloud.service.server.service;

import beikbank.cloud.service.server.model.ErrorContent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基本错误内容
 * @author : liujianzhao
 * @version :
 * @date: Create in 15:36 2018/4/10
 */
@Service
public class BaseErrorContentService {

    private static final Logger log = LoggerFactory.getLogger(BaseErrorContentService.class);
    private final String DEFAULT_ERROR = "系统异常,请稍候重试!";

    @Autowired
    private BaseEnvironment env;

    public ErrorContent errorContentByKey(String key,String... args){
//        String errorContent=this.env.getError(key);
//        if(StringUtils.isBlank(errorContent)){
//            log.error("未找到error key[{}]对应的配置", key);
//            return new ErrorContent("9999",DEFAULT_ERROR,500);
//        }else {
//            return new ErrorContent(key,
//                    this.getErrorMsg(StringUtils.substringBetween(errorContent,";",";")),
//                    Integer.valueOf(StringUtils.substringAfter(errorContent,";")));
//        }
        return new ErrorContent(key,args[0],500);
    }

    private String getErrorMsg(String msg, String... args) {
        String[] var3 = args;
        int var4 = args.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String arg = var3[var5];
            if (arg != null) {
                msg = msg.replaceFirst("\\{\\}", arg);
            }
        }

        return msg;
    }
}
