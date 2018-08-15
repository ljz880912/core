package beikbank.cloud.service.server.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 10:38 2018/4/11
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthScope {

    AuthRole[] value() default {AuthRole.USER};
}
