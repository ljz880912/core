package beikbank.cloud.service.server.aspect;

import com.beikbank.common.BaseAspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/4/7/007.
 */
@Component
@Aspect
public class RedisAspect {

    private static final Logger log = LoggerFactory.getLogger(RedisAspect.class);

    @Around("execution(public * beikbank.cloud.service.server.service.RedisService.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        return BaseAspectUtils.logAround(joinPoint, Long.valueOf(200L));
    }
}
