package beikbank.cloud.service.server.filter.ratelimit;

import beikbank.cloud.service.server.filter.AbstractFilter;
import beikbank.cloud.service.server.model.ConcurrentHashSet;
import beikbank.cloud.service.server.model.ErrorContent;
import beikbank.cloud.service.server.service.BaseEnvironment;
import beikbank.cloud.service.server.service.BaseErrorContentService;
import beikbank.cloud.service.server.service.RedisService;
import com.beikbank.common.BaseIpUtils;
import com.beikbank.common.BaseJwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/4/10/010.
 */
public class RateLimitFilter extends AbstractFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private String serviceName;
    private RedisService redis;
    private BaseEnvironment env;
    private BaseErrorContentService errorContentService;
    private Set<RateLimiter> rateLimiters = new ConcurrentHashSet();
    private ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitFilter(String serviceName, RedisService redis, BaseEnvironment env, BaseErrorContentService errorContentService) {
        this.serviceName = serviceName;
        this.redis = redis;
        this.env = env;
        this.errorContentService = errorContentService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        if(((HttpServletRequest)servletRequest).getRequestURI().contains("/" + this.serviceName)) {
            Set<RateLimiter> crtRateLimiter = this.getRateLimiters(this.env.getProperties("ratelimit"));
            Iterator iterator = this.rateLimiters.iterator();

            while(iterator.hasNext()) {
                if(!crtRateLimiter.contains(iterator.next())) {
                    iterator.remove();
                }
            }

            this.rateLimiters.addAll(crtRateLimiter);
            List<Boolean> checkResults = this.rateLimiters.stream().map((rateLimiter) -> {
                if(rateLimiter.getType() == RateLimiter.Type.IP) {
                    return Boolean.valueOf(rateLimiter.incr(BaseIpUtils.getIp(httpServletRequest)));
                } else {
                    if(rateLimiter.getType() == RateLimiter.Type.TOKEN) {
                        if(httpServletRequest.getHeader("Authorization") != null) {
                            return Boolean.valueOf(rateLimiter.incr(httpServletRequest.getHeader("Authorization")));
                        }
                    } else if((rateLimiter.getType() == RateLimiter.Type.ORG || rateLimiter.getType() == RateLimiter.Type.USER)
                            && httpServletRequest.getHeader("Authorization") != null) {
                        Map<String, Object> tokenInfo = BaseJwtUtils.getTokenInfo(httpServletRequest.getHeader("Authorization"));
                        if(tokenInfo != null) {
                            String tokenType = (String)tokenInfo.get("type");
                            if(rateLimiter.getType() == RateLimiter.Type.ORG) {
                                if(StringUtils.equalsIgnoreCase(tokenType, "user") || StringUtils.equalsIgnoreCase(tokenType, "tenant")) {
                                    return Boolean.valueOf(rateLimiter.incr((String)tokenInfo.get("aud")));
                                }
                            } else if(rateLimiter.getType() == RateLimiter.Type.USER && StringUtils.equalsIgnoreCase(tokenType, "user")) {
                                return Boolean.valueOf(rateLimiter.incr(tokenInfo.get("aud") + ":" + tokenInfo.get("userId")));
                            }
                        }
                    }

                    return Boolean.valueOf(true);
                }
            }).collect(Collectors.toList());
            if(checkResults.contains(new Boolean("false"))) {
                this.writeErrorResponse((HttpServletResponse)servletResponse, "rate.limit.exceed");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Set<RateLimiter> getRateLimiters(Properties prop) {
        if(prop == null) {
            return Collections.emptySet();
        } else {
            Set<String> keys = prop.stringPropertyNames();
            return keys.stream().map((key) -> {
                try {
                    String[] rateLimitInfo = key.split("\\.");
                    RateLimiter.Type type = RateLimiter.Type.valueOf(rateLimitInfo[0].toUpperCase());
                    RateLimiter.Strategy strategy = RateLimiter.Strategy.valueOf(rateLimitInfo[1].toUpperCase());
                    Integer duration = Integer.valueOf(rateLimitInfo[2]);
                    RateLimiter rateLimiter = new RateLimiter(this.redis);
                    rateLimiter.setType(type);
                    rateLimiter.setStrategy(strategy);
                    rateLimiter.setDuration(duration);
                    rateLimiter.setLimit(Integer.valueOf((String)prop.get(key)));
                    rateLimiter.setServiceName(this.serviceName);
                    return rateLimiter;
                } catch (Throwable var8) {
                    log.error("从配置文件中获取RateLimiter信息异常", var8);
                    return null;
                }
            }).filter((limiter) -> {
                return limiter != null;
            }).collect(Collectors.toSet());
        }
    }


    private void writeErrorResponse(HttpServletResponse res, String errorKey) {
        try {
            ErrorContent errorContent = this.errorContentService.errorContentByKey("rate.limit.exceed", new String[0]);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json");
            res.setStatus(errorContent.getHttpCode().intValue());
            this.objectMapper.writeValue(res.getOutputStream(), errorContent.exceptionResp());
        } catch (IOException var4) {
            log.error("Can not write to the response output stream: {}", var4);
        }

    }
}
