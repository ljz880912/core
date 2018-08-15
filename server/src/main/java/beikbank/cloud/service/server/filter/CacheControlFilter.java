package beikbank.cloud.service.server.filter;

import com.google.common.collect.ImmutableSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class CacheControlFilter extends AbstractFilter{

    private ImmutableSet cacheControlMethods = ImmutableSet.of("GET", "HEAD");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.addHeader(request, response);
        chain.doFilter(request, response);
    }

    private void addHeader(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        if(!response.isCommitted() && !response.containsHeader("Cache-Control")) {
            if(this.cacheControlMethods.contains(request.getMethod())) {
                response.setHeader("Cache-Control", "no-cache");
            }

        }
    }
}
