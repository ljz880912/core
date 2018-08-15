package beikbank.cloud.service.server.model;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:37 2018/4/11
 */
public enum AuthRole {

    USER,
    //第三方接入者
    TENANT,
    SERVICE;

    private AuthRole() {
    }
}
