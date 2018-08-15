package beikbank.cloud.service.client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by 琉璃 on 2017/11/28.
 */
public class Request <T extends HttpRequestBase> {

    private final T request;
    private final Client client;
    private final URIBuilder uriBuilder;

    public Request(T request,Client client,URI baseUrl){
        this.request=request;
        this.client=client;
        this.uriBuilder=new URIBuilder(baseUrl);
    }

    public Request<T> jsonEntity(Object value){
        return this.entity(this.client.jsonEntity(value));
    }

    public Request<T> urlEncodedEntity(List<? extends NameValuePair> valuePairs){
        return this.entity(this.client.urlEncodedFormEntity(valuePairs));
    }

    public Request<T> entity(HttpEntity entity){
        if(!(this.request instanceof HttpEntityEnclosingRequest)){
            throw new IllegalArgumentException("HTTP"+this.request.getMethod()+" doesn't accept an entity");
        }else {
            ((HttpEntityEnclosingRequest)this.request).setEntity(entity);
            return this;
        }
    }

    public Request<T> param(String name,Object value){
        if(value!=null){
            this.uriBuilder.setParameter(name,value.toString());
        }

        return this;
    }

    public Request<T> addParam(String name, Object value) {
        if(value != null) {
            this.uriBuilder.addParameter(name, value.toString());
        }

        return this;
    }
    public Request<T> range(Long fromBytes, Long toBytes) {
        Preconditions.checkArgument(fromBytes != null || toBytes != null, "Either fromBytes or toBytes must be specified");
        Preconditions.checkArgument(fromBytes == null || fromBytes >= 0L, "Invalid fromBytes: " + fromBytes);
        Preconditions.checkArgument(toBytes == null || toBytes >= 0L, "Invalid toBytes: " + fromBytes);
        StringBuilder sb = new StringBuilder();
        if (fromBytes != null) {
            sb.append(fromBytes);
        }

        sb.append('-');
        if (toBytes != null) {
            sb.append(toBytes);
        }

        this.header("Range", sb.toString());
        return this;
    }

    public Request<T> path(Object... paths) {
        StringBuilder sb = append(new StringBuilder(), this.uriBuilder.getPath());
        Object[] var3 = paths;
        int var4 = paths.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Object path = var3[var5];
            sb = append(sb, path.toString());
        }

        this.uriBuilder.setPath(sb.toString());
        return this;
    }

    private static StringBuilder append(StringBuilder sb, String path) {
        while(path.startsWith("/")) {
            path = path.substring(1);
        }

        if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }

        return sb.append(path);
    }

    public Request<T> header(String name, Object value) {
        if (value != null) {
            this.request.setHeader(name, value.toString());
        }

        return this;
    }

    public long execute(OutputStream out) {
        return this.client.execute(this.httpRequest(), out);
    }

    public long execute(ByteSink out) {
        return this.client.execute(this.httpRequest(), out);
    }

    public HttpResponse execute() {
        return this.client.execute(this.httpRequest());
    }

    public <T> T execute(TypeReference<T> typeReference) {
        return this.client.execute(this.httpRequest(), typeReference);
    }

    public <T> T execute(Class<T> type) {
        return this.client.execute(this.httpRequest(), type);
    }

    public <T> T execute(JavaType type) {
        return this.client.execute(this.httpRequest(), type);
    }

    public <T> T execute(ResponseHandler<T> handler) {
        return this.client.execute(this.httpRequest(), handler);
    }

    public T httpRequest() {
        URI uri = this.getRequestUri();
        this.request.setURI(uri);
        return this.request;
    }

    public URI getRequestUri() {
        try {
            return this.uriBuilder.build();
        } catch (URISyntaxException var2) {
            throw new IllegalStateException("Malformed URI '" + var2.getInput() + "': " + var2.getMessage());
        }
    }
}
