package beikbank.cloud.service.server.model;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:38 2018/4/11
 */
public class AuthUtil {

    private static final String BASIC_MECHANISM = "Basic ";
    public static final String BEARER_MECHANISM = "Bearer ";
    public static final String INTERNAL_MECHANISM = "Service ";
    private static final String AUTH_INFO_ATTRIBUTE = "AuthInfo";

    public static boolean isBasicMechanism(String authorization) {
        return authorization != null && authorization.startsWith(BASIC_MECHANISM);
    }

    public static boolean isBearerMechanism(String authorization) {
        return authorization != null && authorization.startsWith(BEARER_MECHANISM);
    }

    public static void setAuthInfo(HttpServletRequest request, AuthInfo authInfo) {
        request.setAttribute(AUTH_INFO_ATTRIBUTE, authInfo);
    }

    public static AuthInfo getAuthInfo(HttpServletRequest request) {
        return (AuthInfo)request.getAttribute(AUTH_INFO_ATTRIBUTE);
    }

    public static String getAccessToken(String authorization) {
        int i = authorization.indexOf(32);
        return i > 0 ? authorization.substring(i + 1).trim() : null;
    }
}
