package beikbank.cloud.service.client;

import beikbank.cloud.service.client.auth.AuthorizationProvider;
import beikbank.cloud.service.client.discovery.DiscoveryService;
import beikbank.cloud.service.client.exception.ClientException;
import beikbank.cloud.service.client.exception.RetryAfterException;
import beikbank.cloud.service.common.exception.BaseTimeoutException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Administrator on 2017/11/26/026.
 */
public abstract class Client {

    private static final Logger log= LoggerFactory.getLogger(Client.class);
    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    protected final HttpContext httpContext;
    protected final ObjectMapper objectMapper;
    protected final ClientFactory factory;
    protected URI baseUrl;
    protected boolean optimizeURIRoute;
    protected AuthorizationProvider authorizationProvider;
    protected Function<HttpUriRequest, HttpUriRequest> requestModifier;
    protected HttpClient httpClient;
    private String apiToken;
    private String proxyUserId;
    private Map<String, String> defaultHeaders;
    private DiscoveryService discoveryService;

    protected Client(ClientFactory factory) {
        this(factory, factory.baseUrl, false);
    }

    protected Client(ClientFactory factory, URI baseUrl) {
        this(factory, baseUrl, true);
    }

    protected Client(ClientFactory factory, URI baseUrl, boolean optimizeURIRoute) {
        this.defaultHeaders = Maps.newHashMap();
        this.factory = factory;
        this.httpClient = factory.httpClient;
        this.objectMapper = factory.objectMapper;
        this.discoveryService = factory.discoveryService;
        this.authorizationProvider = factory.authorizationProvider;
        this.baseUrl = baseUrl;
        this.optimizeURIRoute = optimizeURIRoute;
        this.httpContext = new BasicHttpContext();
        this.httpContext.setAttribute("http.cookie-store", new BasicCookieStore());
    }



    private static String toLogSafeString(URI uri) {
        try {
            return (new URI(uri.getScheme(), (String)null, uri.getHost(), uri.getPort(), uri.getPath(), (String)null, (String)null)).toString();
        } catch (URISyntaxException var2) {
            throw new AssertionError();
        }
    }

    protected static boolean isApplicationJson(HttpEntity entity) {
        ContentType type = ContentType.get(entity);
        return type != null && ContentType.APPLICATION_JSON.getMimeType().equals(type.getMimeType());
    }

    protected static boolean isInformational(HttpResponse response) {
        return HttpUtil.isInformational(response.getStatusLine().getStatusCode());
    }

    protected static boolean isSuccessful(HttpResponse response) {
        return HttpUtil.isSuccessful(response.getStatusLine().getStatusCode());
    }

    protected static boolean isRedirection(HttpResponse response) {
        return HttpUtil.isRedirection(response.getStatusLine().getStatusCode());
    }

    protected static boolean isClientError(HttpResponse response) {
        return HttpUtil.isClientError(response.getStatusLine().getStatusCode());
    }

    protected static boolean isServerError(HttpResponse response) {
        return HttpUtil.isServerError(response.getStatusLine().getStatusCode());
    }

    protected void setBaseUrl(URI baseUrl) {
        this.baseUrl = (URI)Preconditions.checkNotNull(baseUrl);
    }

    public URI getBaseUrl() {
        return this.baseUrl;
    }

    public ClientFactory getFactory() {
        return this.factory;
    }

    public String getApiToken() {
        return this.apiToken;
    }

    public void setApiToken(String sharedToken) {
        this.apiToken = sharedToken;
    }

    public void setAuthorization(final String authorization) {
        Preconditions.checkNotNull(authorization, "authorization");
        this.setAuthorizationProvider(new AuthorizationProvider() {
            @Override
            public String getAuthorization() {
                return authorization;
            }
            @Override
            public boolean refreshToken() {
                return false;
            }
        });
    }

    public String getAuthorization() {
        return this.authorizationProvider != null ? this.authorizationProvider.getAuthorization() : null;
    }

    public void setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
    }

    public String getProxyUserId() {
        return this.proxyUserId;
    }

    public void setProxyUserId(String proxiedUserId) {
        this.proxyUserId = proxiedUserId;
    }

    public Client setDefaultHeader(String name, String value) {
        this.defaultHeaders.put(name, value);
        return this;
    }

    public Request<HttpGet> get(Object... paths) {
        return this.newRequest(new HttpGet(), paths);
    }

    public Request<HttpGet> get(URI uri, Object... paths) {
        return this.newRequest(new HttpGet(), uri, paths);
    }

    public Request<HttpHead> head(Object... paths) {
        return this.newRequest(new HttpHead(), paths);
    }

    public Request<HttpHead> head(URI uri, Object... paths) {
        return this.newRequest(new HttpHead(), uri, paths);
    }

    public Request<HttpPost> post(Object... paths) {
        return this.newRequest(new HttpPost(), paths);
    }

    public Request<HttpPost> post(URI uri, Object... paths) {
        return this.newRequest(new HttpPost(), uri, paths);
    }

    public Request<HttpPut> put(Object... paths) {
        return this.newRequest(new HttpPut(), paths);
    }

    public Request<HttpPut> put(URI uri, Object... paths) {
        return this.newRequest(new HttpPut(), uri, paths);
    }

    public Request<HttpPatch> patch(Object... paths) {
        return this.newRequest(new HttpPatch(), paths);
    }

    public Request<HttpPatch> patch(URI uri, Object... paths) {
        return this.newRequest(new HttpPatch(), uri, paths);
    }

    public Request<HttpDelete> delete(Object... paths) {
        return this.newRequest(new HttpDelete(), paths);
    }

    public Request<HttpDelete> delete(URI uri, Object... paths) {
        return this.newRequest(new HttpDelete(), uri, paths);
    }

    public Request<HttpOptions> options(Object... paths) {
        return this.newRequest(new HttpOptions(), paths);
    }

    protected <T extends HttpRequestBase> Request<T> newRequest(T requestBase, Object... paths) {
        Preconditions.checkState(this.baseUrl != null, "baseUrl is not set");
        return this.newRequest(requestBase, this.baseUrl, this.optimizeURIRoute, paths);
    }

    protected <T extends HttpRequestBase> Request<T> newRequest(T requestBase, URI baseUrl, boolean optimizeBaseUrlRoute, Object... paths) {
        Preconditions.checkNotNull(requestBase, "requestBase");
        Preconditions.checkNotNull(baseUrl, "baseUrl");
        baseUrl = optimizeBaseUrlRoute ? this.getOptimizedURIRoute(baseUrl, this.discoveryService) : baseUrl;
        return (new Request(requestBase, this, baseUrl)).path(paths);
    }

    protected HttpEntity jsonEntity(Object value) {
        Preconditions.checkNotNull(value, "value");

        try {
            return new ByteArrayEntity(this.objectMapper.writeValueAsBytes(value), ContentType.APPLICATION_JSON);
        } catch (IOException var3) {
            throw new ClientException("Error serializing JSON payload", var3);
        }
    }

    protected HttpEntity urlEncodedFormEntity(Iterable<? extends NameValuePair> params) {
        Preconditions.checkNotNull(params, "params");
        return new UrlEncodedFormEntity(params, Charsets.UTF_8);
    }

    protected HttpResponse execute(HttpUriRequest request) {
        Preconditions.checkNotNull(request, "request");
        return (HttpResponse)this.execute(request, (response) -> {
            this.checkResponse(request, response);
            return response;
        });
    }

    protected long execute(HttpUriRequest request, OutputStream out) {
        Preconditions.checkNotNull(out, "out");
        return (Long)this.execute(request, (response) -> {
            this.checkResponse(request, response);
            return ByteStreams.copy(response.getEntity().getContent(), out);
        });
    }

    protected long execute(HttpUriRequest request, ByteSink out) {
        Preconditions.checkNotNull(out, "out");
        return (Long)this.execute(request, (response) -> {
            this.checkResponse(request, response);
            return out.writeFrom(response.getEntity().getContent());
        });
    }

    protected <T> T execute(HttpUriRequest req, TypeReference<T> typeReference) {
        return this.execute(req, this.typeFactory().constructType(typeReference));
    }

    protected <T> T execute(HttpUriRequest req, Class<T> type) {
        return this.execute(req, this.typeFactory().constructType(type));
    }

    protected <T> T execute(final HttpUriRequest request, final JavaType type) {
        Preconditions.checkNotNull(type, "type");
        return this.execute(request, new ResponseHandler<T>() {
            @Override
            public T handleResponse(HttpResponse response) throws IOException {
                Client.this.checkResponse(request, response);
                if (response.getEntity() == null) {
                    throw new ClientException("Unexpected empty response body: response = " + response.getStatusLine());
                } else {
                    InputStream is = new BufferedInputStream(response.getEntity().getContent());
                    return Client.this.objectMapper.readValue(is, type);
                }
            }
        });
    }

    protected <T> T readJsonEntity(HttpResponse response, Class<T> type) throws IOException {
        HttpEntity entity = response.getEntity();
        return entity != null && isApplicationJson(entity) ? this.objectMapper.readValue(entity.getContent(), type) : null;
    }

    protected <T> T readJsonEntity(HttpResponse response, TypeReference<T> typeRef) throws IOException {
        HttpEntity entity = response.getEntity();
        return entity != null && isApplicationJson(entity) ? this.objectMapper.readValue(entity.getContent(), typeRef) : null;
    }

    protected <T> T execute(HttpUriRequest request, ResponseHandler<T> handler) {
        Preconditions.checkNotNull(request, "request");
        Preconditions.checkNotNull(handler, "handler");
        this.prepareRequest(request);

        HttpResponse response;
        try {
            HttpContext context = HttpClientContext.create();
            context.removeAttribute("http.protocol.redirect-locations");
            response = this.httpClient.execute(request, context);
        } catch (IOException var12) {
            if (!(var12 instanceof SocketTimeoutException) && !(var12 instanceof ConnectTimeoutException)) {
                throw new ClientException("Error executing request (url = " + request.getURI() + ")", var12);
            }

            throw new BaseTimeoutException();
        }

        Object var16;
        try {
            if (response.getStatusLine().getStatusCode() == 401 && this.authorizationProvider != null && this.authorizationProvider.refreshToken()) {
                try {
                    response = this.httpClient.execute(request, this.httpContext);
                } catch (IOException var11) {
                    throw new ClientException("Error executing request (url = " + request.getURI() + ")", var11);
                }
            }

            var16 = handler.handleResponse(response);
        } catch (ClientException var13) {
            throw var13;
        } catch (Exception var14) {
            throw new ClientException("Error in response handler (url = " + request.getURI() + ")", var14);
        } finally {
            this.postHandleResponse(response);
            EntityUtils.consumeQuietly(response.getEntity());
        }

        return (T)var16;
    }

    protected void postHandleResponse(HttpResponse response) {
    }

    protected void prepareRequest(HttpUriRequest request) {
        if (!request.containsHeader("Authorization")) {
            String authorization = this.getAuthorization();
            if (authorization != null) {
                request.setHeader("Authorization", authorization);
            }
        }

        if (!request.containsHeader("Accept")) {
            request.setHeader("Accept", ContentType.APPLICATION_JSON.toString());
        }

        if (this.apiToken != null) {
            request.setHeader("X-Api-Token", this.apiToken);
        }

        if (!request.containsHeader("X-Correlation-Id")) {
            request.setHeader("X-Correlation-Id", MDC.get("CORRELATION_ID"));
        }

        if (this.proxyUserId != null) {
            request.setHeader("X-Proxy-User", this.proxyUserId);
        }

        if (this.requestModifier != null) {
            this.requestModifier.apply(request);
        }

        this.defaultHeaders.entrySet().stream().filter((entry) -> {
            return !request.containsHeader((String)entry.getKey());
        }).forEach((entry) -> {
            request.setHeader((String)entry.getKey(), (String)entry.getValue());
        });
    }

    protected void checkResponse(HttpUriRequest request, HttpResponse response) {
        if (!isSuccessful(response)) {
            throw this.clientException(request, response);
        } else {
            Header header = response.getFirstHeader("Authorization");
            if (header != null) {
                this.setAuthorization(header.getValue());
            }

        }
    }

    private ClientException clientException(HttpUriRequest request, HttpResponse response) {
        StatusLine status = response.getStatusLine();
        Formatter fmt = (new Formatter()).format("%s failed: %s (url = %s", request.getMethod(), status, toLogSafeString(request.getURI()));
        String correlationId = HttpUtil.getHeader(request, "X-Correlation-Id");
        if (correlationId != null) {
            fmt.format(", correlationId = %s", correlationId);
        }

        ClientException clientException = this.getResponseErrors(response);
        if (clientException != null && !Strings.isNullOrEmpty(clientException.getMessage())) {
            fmt.format(", errorCode = '%s', errorDesc = '%s'", clientException.getErrorCode(), clientException.getMessage());
        }

        fmt.format(")");
        if (status.getStatusCode() == HTTP_STATUS_TOO_MANY_REQUESTS) {
            Integer retryAfterSecs = null;
            String retryAfter = HttpUtil.getHeader(request, "Retry-After");
            if (retryAfter != null) {
                try {
                    retryAfterSecs = Integer.parseInt(retryAfter);
                } catch (NumberFormatException var10) {
                    log.warn("Unable to parse '%s' http header value: %s", "Retry-After", retryAfter);
                }
            }

            return new RetryAfterException(status.getStatusCode(), fmt.toString(), retryAfterSecs);
        } else {
            if (clientException == null) {
                clientException = new ClientException(status.getStatusCode(), fmt.toString());
            }

            return clientException;
        }
    }

    private ClientException getResponseErrors(HttpResponse response) {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null && entity.getContentLength() != 0L && isApplicationJson(entity)) {
                String message = EntityUtils.toString(entity, Charsets.UTF_8);
                JsonNode node = this.objectMapper.readTree(message);
                if (node.has("code") && node.has("msg") && node.has("correlation_id")) {
                    return new ClientException(response.getStatusLine().getStatusCode(), node.get("code").toString(), node.get("msg").toString());
                }
            }
        } catch (IOException var5) {
            log.error("从response中获取ClientException对象异常", var5);
        }

        return null;
    }

    protected TypeFactory typeFactory() {
        return this.objectMapper.getTypeFactory();
    }

    protected URI getOptimizedURIRoute(URI baseUrl, DiscoveryService ds) {
        URI resolvedURI = baseUrl;
        if (ds != null) {
            URI uri = ds.getOptimizedURI(baseUrl);
            if (uri != null) {
                resolvedURI = uri;
            }
        }

        return resolvedURI;
    }
}
