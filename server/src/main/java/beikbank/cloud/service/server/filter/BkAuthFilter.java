package beikbank.cloud.service.server.filter;

import beikbank.cloud.service.client.exception.ClientException;
import beikbank.cloud.service.server.ServerException;
import beikbank.cloud.service.server.model.AuthRole;
import beikbank.cloud.service.server.model.AuthScope;
import com.beikbank.authentication.center.client.BeikBankAucClient;
import com.beikbank.authentication.center.dto.req.TokenVerifyReq;
import com.beikbank.authentication.center.dto.rsp.TokenVerifyRsp;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/4/13/013.
 */
@Component
public class BkAuthFilter extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(BkAuthFilter.class);
    @Autowired
    private BeikBankAucClient beikBankAucClient;
    private LoadingCache<String, TokenVerifyRsp> tokenCache;

    public BkAuthFilter() {
        this.tokenCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).maximumSize(1000L).build(new CacheLoader<String, TokenVerifyRsp>() {
            @Override
            public TokenVerifyRsp load(String token) throws Exception {
                return BkAuthFilter.this.beikBankAucClient.verifyToken(new TokenVerifyReq("Bearer " + token));
            }
        });
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if(handler instanceof HandlerMethod && !HttpMethod.OPTIONS.name().equalsIgnoreCase(req.getMethod())) {
            Method method = ((HandlerMethod)handler).getMethod();

            try {
                this.checkAuthScopes(method, req);
            } finally {
                resp.setHeader("Access-Control-Allow-Origin", "*");
            }
        }

        return true;
    }

    private void checkAuthScopes(Method method, HttpServletRequest req) {
        AuthScope authScope = method.getAnnotation(AuthScope.class);
        if(authScope != null) {
            String jwtToken = this.getToken(req, "Bearer ");

            try {
                TokenVerifyRsp verifyRsp = this.tokenCache.get(jwtToken);
                if(verifyRsp != null && "0000000".equals(verifyRsp.getCode())) {
                    AuthRole role =  Arrays.stream(authScope.value()).filter((authRole) -> {
                        return StringUtils.equalsIgnoreCase(authRole.name(), verifyRsp.getTokenType());
                    }).findAny().orElse(null);
                    if(role != null) {
                        if(StringUtils.equalsIgnoreCase(verifyRsp.getTokenType(), "service")) {
                            MDC.put("serviceFrom", verifyRsp.getRequester());
                        } else if(StringUtils.equalsIgnoreCase(verifyRsp.getTokenType(), "tenant")) {
                            MDC.put("orgCode", verifyRsp.getOrgCode());
                        } else if(StringUtils.equalsIgnoreCase(verifyRsp.getTokenType(), "user")) {
                            MDC.put("orgCode", verifyRsp.getOrgCode());
                            MDC.put("userId", verifyRsp.getRequester());
                            MDC.put("account", verifyRsp.getAccount());
                        }
                    }

                    return;
                }

                if(verifyRsp != null) {
                    log.error("token校验失败:" + verifyRsp.getMsg());
                }
            } catch (Throwable var7) {
                if(var7 instanceof ClientException && ((ClientException)var7).getStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
                    log.error("无service token权限调用auc validate接口");
                    throw ServerException.fromKey("internal.server.error", new String[0]);
                }

                log.error("调用auc校验token异常", var7);
            }

            throw new ServerException("unauthorized", "调用auc校验token异常");
        }
    }

    private String getToken(HttpServletRequest request, String prefix) {
        String authHeader = request.getHeader("Authorization");
        if(StringUtils.isBlank(authHeader)) {
            log.error("请求缺少头部:Authorization");
            throw new ServerException("unauthorized", "请求缺少头部:Authorization");
        } else {
            int index = authHeader.indexOf(" ");
            if(index == -1) {
                log.error("token[{}]缺少token类型", authHeader);
                throw new ServerException("unauthorized","缺少token类型");
            } else {
                String tokenType = authHeader.substring(0, index);
                if(!tokenType.equalsIgnoreCase(prefix.trim())) {
                    log.error("token类型不匹配 期望[{}], 实际[{}]", prefix, tokenType);
                    throw new ServerException("unauthorized", "token类型不匹配");
                } else {
                    return authHeader.substring(index).trim();
                }
            }
        }
    }
}
