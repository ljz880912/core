package beikbank.cloud.service.client;

import beikbank.cloud.service.common.util.SslUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by 琉璃 on 2017/12/15.
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    public static final String CLIENT_METRIC_PREFIX = "http-client";
    public static final String FACTORY_METRIC_PREFIX = "http-client-factory";
    private static final Registry<ConnectionSocketFactory> permissiveSocketFactoryRegistry
            = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", new SSLConnectionSocketFactory(SslUtil.permissiveContext(), (hostname, session) -> {
                return true;
            })).build();

    private static final HttpRequestRetryHandler defaultRetryHandler = new StandardHttpRequestRetryHandler() {
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (super.retryRequest(exception, executionCount, context)) {
                HttpUtil.log.debug("###Retrying http request... (executionCount: {})", Integer.valueOf(executionCount), exception);
                return true;
            } else {
                HttpUtil.log.info("###Cannot retry http request...", exception);
                return false;
            }
        }
    };

    public HttpUtil() {
    }


    public static String getHeader(HttpUriRequest request, String name) {
        return getHeader(request, name, null);
    }

    public static String getHeader(HttpUriRequest request, String name, String defaultValue) {
        Preconditions.checkNotNull(request, "request is null");
        Preconditions.checkNotNull(name, "name is null");
        Header header = request.getFirstHeader(name);
        return header != null ? header.getValue() : defaultValue;
    }

    public static Registry<ConnectionSocketFactory> permissiveSocketFactoryRegistry() {
        return permissiveSocketFactoryRegistry;
    }

    public static HttpRequestRetryHandler defaultRetryHandler() {
        return defaultRetryHandler;
    }

    /**
     * http请求使用请求池
     *
     * @param disableSslChecks
     * @param maxConnections
     * @param maxConnectionsPerRoute
     * @return
     */
    public static PoolingHttpClientConnectionManager newPoolingClientConnectionManager(boolean disableSslChecks, int maxConnections, int maxConnectionsPerRoute) {
        PoolingHttpClientConnectionManager cm = disableSslChecks ? new PoolingHttpClientConnectionManager(permissiveSocketFactoryRegistry()) : new PoolingHttpClientConnectionManager();
        if (maxConnections > 0) {
            cm.setMaxTotal(maxConnections);
        }

        if (maxConnectionsPerRoute > 0) {
            cm.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        }

        return cm;
    }


    /**
     * metric监测程序
     */
    public static MetricRegistry registerMetrics(MetricRegistry registry, ConnPoolControl poolControl, String context) {
        Preconditions.checkNotNull(registry, "MetricRegistry is null");
        Preconditions.checkNotNull(poolControl, "poolControl is null");

        registry.register(MetricRegistry.name(FACTORY_METRIC_PREFIX, new String[]{context, "available-connections"}), (Gauge<Integer>)() -> {
            return Integer.valueOf(poolControl.getTotalStats().getAvailable());
        });
        registry.register(MetricRegistry.name(FACTORY_METRIC_PREFIX, new String[]{context, "leased-connections"}), (Gauge<Integer>)() -> {
            return Integer.valueOf(poolControl.getTotalStats().getLeased());
        });
        registry.register(MetricRegistry.name(FACTORY_METRIC_PREFIX, new String[]{context, "max-connections"}), (Gauge<Integer>)() -> {
            return Integer.valueOf(poolControl.getTotalStats().getMax());
        });
        registry.register(MetricRegistry.name(FACTORY_METRIC_PREFIX, new String[]{context, "pending-connections"}), (Gauge<Integer>)() -> {
            return Integer.valueOf(poolControl.getTotalStats().getPending());
        });

        return registry;
    }

    public static HttpRequestExecutor newInstrumentedHttpRequestExecutor(MetricRegistry registry, String context) {
        return new InstrumentedHttpRequestExecutor(registry, (name, request) -> {
            if (request instanceof HttpRequestWrapper) {
                request = ((HttpRequestWrapper)request).getOriginal();
            }

            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod().toLowerCase();
            return MetricRegistry.name("http-client", new String[]{context, method + "-requests"});
        });
    }

    public static HttpUtil.DeferredAssignmentHttpRequestExecutor newDeferredAssignmentInstrumentedHttpRequestExecutor(String context) {
        return new HttpUtil.DeferredAssignmentHttpRequestExecutor(context);
    }

    public static boolean isInformational(int httpStatus) {
        return httpStatus >= 100 && httpStatus < 200;
    }

    public static boolean isSuccessful(int httpStatus) {
        return httpStatus >= 200 && httpStatus < 300;
    }

    public static boolean isRedirection(int httpStatus) {
        return httpStatus >= 300 && httpStatus < 400;
    }

    public static boolean isClientError(int httpStatus) {
        return httpStatus >= 400 && httpStatus < 500;
    }

    public static boolean isServerError(int httpStatus) {
        return httpStatus >= 500 && httpStatus < 600;
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("Error encoding URL with UTF-8", var2);
        }
    }

    public static class DeferredAssignmentHttpRequestExecutor extends HttpRequestExecutor {
        private HttpRequestExecutor delegate;
        private final String name;

        public DeferredAssignmentHttpRequestExecutor(String name) {
            this.name = name;
        }

        public void setMetricRegistry(MetricRegistry registry) {
            this.delegate = HttpUtil.newInstrumentedHttpRequestExecutor(registry, this.name);
        }

        @Override
        public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
            return this.delegate != null ? this.delegate.execute(request, conn, context) : super.execute(request, conn, context);
        }

        @Override
        public void preProcess(HttpRequest request, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
            if (this.delegate != null) {
                this.delegate.preProcess(request, processor, context);
            } else {
                super.preProcess(request, processor, context);
            }

        }

        @Override
        public void postProcess(HttpResponse response, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
            if (this.delegate != null) {
                this.delegate.postProcess(response, processor, context);
            } else {
                super.postProcess(response, processor, context);
            }

        }
    }

}
