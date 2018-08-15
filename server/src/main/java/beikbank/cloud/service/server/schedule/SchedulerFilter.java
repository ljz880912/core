package beikbank.cloud.service.server.schedule;

import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class SchedulerFilter extends OncePerRequestFilter implements Ordered {

    private int order = 2147413132;
    private final SchedulerManager schedulerManager;

    public SchedulerFilter(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if(this.schedulerManager.include(requestPath)) {
            this.schedulerManager.schedule(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
