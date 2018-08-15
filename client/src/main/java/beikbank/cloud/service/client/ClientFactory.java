package beikbank.cloud.service.client;

import beikbank.cloud.service.client.auth.AuthorizationProvider;
import beikbank.cloud.service.client.discovery.DiscoveryService;
import com.beikbank.common.BaseJsonUtils;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/26/026.
 */
public abstract class ClientFactory implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(ClientFactory.class);
    protected final URI baseUrl;
    protected final CloseableHttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected final AuthorizationProvider authorizationProvider;
    protected final DiscoveryService discoveryService;
    protected final HttpRequestRetryHandler retryHandler;
    private final HttpClientConnectionManager connectionManager;
    private final boolean isSelfManagedConnectionManager;
    private final HttpUtil.DeferredAssignmentHttpRequestExecutor deferredAssignmentRequestExecutor;
    private final String clientFactoryName;
    protected MetricRegistry metricRegistry;

    protected ClientFactory(ClientFactory.Builder<?> builder) {
        this.discoveryService = builder.discoveryService;
        this.baseUrl = builder.baseUrl;
        HttpClientBuilder clientBuilder = this.newHttpClientBuilder();
        if (builder.connectionManager != null) {
            this.connectionManager = builder.connectionManager;
            this.isSelfManagedConnectionManager = false;
        } else {
            this.connectionManager = this.newPoolingClientConnectionManager(builder);
            this.isSelfManagedConnectionManager = true;
        }

        clientBuilder.setConnectionManager(this.connectionManager);
        this.retryHandler = builder.retryHandler != null ? builder.retryHandler : HttpUtil.defaultRetryHandler();
        clientBuilder.setRetryHandler(this.retryHandler);
        this.clientFactoryName = builder.clientFactoryName == null ? this.getClass().getSimpleName().replace("ClientFactory", "").toLowerCase() : builder.clientFactoryName;
        if (builder.metricRegistry != null) {
            this.deferredAssignmentRequestExecutor = null;
            clientBuilder.setRequestExecutor(HttpUtil.newInstrumentedHttpRequestExecutor(builder.metricRegistry, this.getClientFactoryName()));
        } else {
            this.deferredAssignmentRequestExecutor = HttpUtil.newDeferredAssignmentInstrumentedHttpRequestExecutor(this.getClientFactoryName());
            clientBuilder.setRequestExecutor(this.deferredAssignmentRequestExecutor);
        }

        clientBuilder.disableCookieManagement();
        if (builder.enableRequestCookies) {
            clientBuilder.addInterceptorFirst(new RequestAddCookies());
        }

        if (builder.requestInterceptor != null) {
            clientBuilder.addInterceptorFirst(builder.requestInterceptor);
        }

        if (builder.userAgent != null) {
            clientBuilder.setUserAgent(builder.userAgent);
        }

        org.apache.http.client.config.RequestConfig.Builder requestConfig = RequestConfig.custom();
        if (builder.connectTimeout > 0L) {
            requestConfig.setConnectTimeout((int) builder.connectTimeout);
            requestConfig.setConnectionRequestTimeout((int) builder.connectTimeout);
        }

        if (builder.readTimeout > 0L) {
            requestConfig.setSocketTimeout((int) builder.readTimeout);
        }

        if (builder.cookieSpec != null) {
            requestConfig.setCookieSpec(builder.cookieSpec);
        }

        if (builder.disableRedirects) {
            requestConfig.setRedirectsEnabled(false);
        }

        clientBuilder.setDefaultRequestConfig(requestConfig.build());
        if (builder.proxyHost != null && builder.proxyPort > 0) {
            clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(builder.proxyHost, builder.proxyPort, "http")));
        } else {
            clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        }

        this.authorizationProvider = builder.authorizationProvider;
        CredentialsProvider credentialsProvider = null;
        if (builder.authScope != null && builder.credentials != null) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(builder.authScope, builder.credentials);
        }

        if (builder.proxyAuthentication.size() > 0) {
            if (credentialsProvider == null) {
                credentialsProvider = new BasicCredentialsProvider();
            }

            Iterator var5 = builder.proxyAuthentication.iterator();

            while (var5.hasNext()) {
                ClientFactory.ProxyAuthentication proxyAuth = (ClientFactory.ProxyAuthentication) var5.next();
                credentialsProvider.setCredentials(proxyAuth.authScope, proxyAuth.credentials);
            }
        }

        if (credentialsProvider != null) {
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        this.objectMapper = builder.objectMapper == null ? BaseJsonUtils.defaultMapper() : builder.objectMapper;
        this.httpClient = clientBuilder.build();
    }

    @PostConstruct
    public void init() {
        if (this.metricRegistry != null && this.deferredAssignmentRequestExecutor != null) {
            this.deferredAssignmentRequestExecutor.setMetricRegistry(this.metricRegistry);
            if (this.isSelfManagedConnectionManager) {
                this.registerHttpPoolForMetrics(this.metricRegistry, (PoolingHttpClientConnectionManager)this.connectionManager);
            }
        }

    }

    protected HttpClientBuilder newHttpClientBuilder() {
        return HttpClientBuilder.create();
    }

    private PoolingHttpClientConnectionManager newPoolingClientConnectionManager(ClientFactory.Builder builder) {
        PoolingHttpClientConnectionManager cm = HttpUtil.newPoolingClientConnectionManager(builder.disableSslChecks, (int)builder.maxConnections, (int)builder.maxConnectionsPerRoute);
        if (builder.metricRegistry != null) {
            this.registerHttpPoolForMetrics(builder.metricRegistry, cm);
        }

        return cm;
    }

    private void registerHttpPoolForMetrics(MetricRegistry reg, PoolingHttpClientConnectionManager cm) {
        HttpUtil.registerMetrics(reg, cm, this.getClientFactoryName());
    }

    public URI getBaseUrl() {
        return this.baseUrl;
    }

    protected String getClientFactoryName() {
        return this.clientFactoryName;
    }

    @Override
    public void close() {
        if (this.isSelfManagedConnectionManager) {
            log.info("Closing connection manager");
            this.connectionManager.shutdown();
        }

    }

    public HttpRequestRetryHandler getRetryHandler() {
        return this.retryHandler;
    }

    private static class ProxyAuthentication {
        public final AuthScope authScope;
        public final Credentials credentials;

        public ProxyAuthentication(AuthScope authScope, Credentials credentials) {
            this.authScope = authScope;
            this.credentials = credentials;
        }
    }

    public abstract static class Builder<T extends ClientFactory.Builder<?>> {
        protected final List<ClientFactory.ProxyAuthentication> proxyAuthentication = new LinkedList();
        protected URI baseUrl;
        protected String userAgent;
        protected long readTimeout;
        protected long connectTimeout;
        protected long maxConnections;
        protected long maxConnectionsPerRoute;
        protected boolean disableSslChecks;
        protected boolean disableRedirects;
        protected boolean enableRequestCookies;
        protected String cookieSpec;
        protected ObjectMapper objectMapper;
        protected AuthorizationProvider authorizationProvider;
        protected AuthScope authScope;
        protected Credentials credentials;
        protected String proxyHost;
        protected int proxyPort;
        protected HttpClientConnectionManager connectionManager;
        protected HttpRequestInterceptor requestInterceptor;
        protected MetricRegistry metricRegistry;
        protected HttpRequestRetryHandler retryHandler;
        protected String clientFactoryName;
        protected DiscoveryService discoveryService;

        protected Builder(ClientFactory.Options props) {
            this.configure(props);
        }

        public T configure(ClientFactory.Options props) {
            this.connectTimeout(props.connectTimeout());
            this.readTimeout(props.readTimeout());
            this.userAgent(props.userAgent());
            this.maxConnections(props.maxConnections());
            this.maxConnectionsPerRoute(props.maxConnectionsPerRoute());
            this.disableSSLChecks(true);
            return this.builder();
        }

        public T baseUrl(String url) {
            return this.baseUrl(URI.create(url));
        }

        public T baseUrl(URL url) {
            return this.baseUrl(url.toString());
        }

        public T baseUrl(URI url) {
            this.baseUrl = (URI) Preconditions.checkNotNull(url, "url");
            return this.builder();
        }

        public T userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this.builder();
        }

        public T readTimeout(long millis) {
            this.readTimeout = millis;
            return this.builder();
        }

        public T connectTimeout(long millis) {
            this.connectTimeout = millis;
            return this.builder();
        }

        public T maxConnections(long maxConnections) {
            this.maxConnections = maxConnections;
            return this.builder();
        }

        public T maxConnectionsPerRoute(long maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this.builder();
        }

        public T connectionManager(HttpClientConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this.builder();
        }

        public T objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = (ObjectMapper)Preconditions.checkNotNull(objectMapper, "objectMapper");
            return this.builder();
        }

        public T disableSSLChecks(boolean disableSslChecks) {
            this.disableSslChecks = disableSslChecks;
            return this.builder();
        }

        public T disableRedirects(boolean disableRedirects) {
            this.disableRedirects = disableRedirects;
            return this.builder();
        }

        public T enableRequestCookies(boolean enableRequestCookies) {
            this.enableRequestCookies = enableRequestCookies;
            return this.builder();
        }

        public T cookieSpec(String cookieSpec) {
            this.cookieSpec = cookieSpec;
            return this.builder();
        }

        public T authorizationProvider(AuthorizationProvider authorizationProvider) {
            this.authorizationProvider = authorizationProvider;
            return this.builder();
        }

        public T authScope(AuthScope authScope) {
            this.authScope = (AuthScope)Preconditions.checkNotNull(authScope, "authScope");
            return this.builder();
        }

        public T credentials(Credentials credentials) {
            this.credentials = (Credentials)Preconditions.checkNotNull(credentials, "credentials");
            return this.builder();
        }

        public T proxyAuthentication(AuthScope proxyAuthScope, Credentials proxyCredentials) {
            AuthScope authScope = (AuthScope)Preconditions.checkNotNull(proxyAuthScope, "proxyAuthScope");
            Credentials credentials = (Credentials)Preconditions.checkNotNull(proxyCredentials, "proxyCredentials");
            this.proxyAuthentication.add(new ClientFactory.ProxyAuthentication(authScope, credentials));
            return this.builder();
        }

        public T proxyAuthentication(String proxyHost, int proxyPort, Credentials proxyCredentials) {
            String host = (String)Preconditions.checkNotNull(proxyHost, "proxyHost");
            if (proxyPort <= 0) {
                throw new IllegalArgumentException("Proxy port must be greater than zero");
            } else {
                return this.proxyAuthentication(new AuthScope(host, proxyPort), proxyCredentials);
            }
        }

        public T proxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
            return this.builder();
        }

        public T proxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
            return this.builder();
        }

        public T requestInterceptor(HttpRequestInterceptor requestInterceptor) {
            this.requestInterceptor = requestInterceptor;
            return this.builder();
        }

        public T metricRegistry(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            return this.builder();
        }

        public T clientFactoryName(String clientFactoryName) {
            this.clientFactoryName = clientFactoryName;
            return this.builder();
        }

        private T builder() {
            return (T)this;
        }

        public T retryHandler(HttpRequestRetryHandler retryHandler) {
            this.retryHandler = retryHandler;
            return this.builder();
        }

        public T discoveryService(DiscoveryService discoveryService) {
            this.discoveryService = discoveryService;
            return this.builder();
        }

    }

    public interface Options {
        String userAgent();

        long connectTimeout();

        long readTimeout();

        long maxConnections();

        long maxConnectionsPerRoute();
    }

}
