package beikbank.cloud.service.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@Component
@Order(-2147483647)
public class CorrelationIdFilter extends AbstractFilter{

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        String correlationId = httpServletRequest.getHeader("X-Correlation-Id");
        if(!this.isAsyncDispatcher(httpServletRequest)) {
            if(correlationId == null) {
                correlationId = UUID.randomUUID().toString();
                log.debug("No correlationId found in header. Generated : " + correlationId);
            } else {
                log.debug("Found correlationId in header : " + correlationId);
            }
        }

        MDC.put("CORRELATION_ID", correlationId);
        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    private boolean isAsyncDispatcher(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getDispatcherType().equals(DispatcherType.ASYNC);
    }
}
