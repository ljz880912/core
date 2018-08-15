package beikbank.cloud.service.server.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public abstract class AbstractFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    protected HttpServletRequest cast(ServletRequest request) {
        return (HttpServletRequest)request;
    }

    protected HttpServletResponse cast(ServletResponse response) {
        return (HttpServletResponse)response;
    }
}
