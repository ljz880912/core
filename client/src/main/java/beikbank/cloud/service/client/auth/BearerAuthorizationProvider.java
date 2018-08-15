package beikbank.cloud.service.client.auth;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 11:13 2018/4/12
 */
public class BearerAuthorizationProvider implements AuthorizationProvider{
    @Override
    public String getAuthorization() {
        return null;
    }

    @Override
    public boolean refreshToken() {
        return false;
    }
}
