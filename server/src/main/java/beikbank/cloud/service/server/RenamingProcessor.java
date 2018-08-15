package beikbank.cloud.service.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.ServletRequest;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 9:50 2018/4/11
 */
public class RenamingProcessor extends ServletModelAttributeMethodProcessor {

    private static final Logger log = LoggerFactory.getLogger(RenamingProcessor.class);
    private Map<Class<?>, Map<String, String>> nameMapping = new ConcurrentHashMap();
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;


    public RenamingProcessor(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        super(false);
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    private static Map<String, String> analyzeClass(Class<?> targetClass) {
        Field[] fields = targetClass.getDeclaredFields();
        Map<String, String> renameMap = new HashMap();
        Field[] var3 = fields;
        int var4 = fields.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Field field = var3[var5];
            JsonProperty paramNameAnnotation = (JsonProperty)field.getAnnotation(JsonProperty.class);
            if (paramNameAnnotation != null && !paramNameAnnotation.value().isEmpty()) {
                renameMap.put(paramNameAnnotation.value(), field.getName());
            }
        }

        if (renameMap.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return renameMap;
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(ModelAttribute.class) != null;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest nativeWebRequest) {
        Object target = binder.getTarget();
        Class<?> targetClass = target.getClass();
        Map mapping;
        if (!this.nameMapping.containsKey(targetClass)) {
            mapping = analyzeClass(targetClass);
            this.nameMapping.put(targetClass, mapping);
        }

        mapping = this.nameMapping.get(targetClass);
        RenamingProcessor.ParamNameDataBinder paramNameDataBinder = new RenamingProcessor.ParamNameDataBinder(target, binder.getObjectName(), mapping);
        this.requestMappingHandlerAdapter.getWebBindingInitializer().initBinder(paramNameDataBinder, nativeWebRequest);
        super.bindRequestParameters(paramNameDataBinder, nativeWebRequest);
    }

    public static class ParamNameDataBinder extends ExtendedServletRequestDataBinder {
        private final Map<String, String> renameMapping;

        public ParamNameDataBinder(Object target, String objectName, Map<String, String> renameMapping) {
            super(target, objectName);
            this.renameMapping = renameMapping;
        }

        @Override
        protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
            super.addBindValues(mpvs, request);
            Iterator var3 = this.renameMapping.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var3.next();
                String from = entry.getKey();
                String to = entry.getValue();
                if (mpvs.contains(from)) {
                    mpvs.add(to, mpvs.getPropertyValue(from).getValue());
                }
            }

        }
    }
}
