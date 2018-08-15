package beikbank.cloud.service.common.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Created by 琉璃 on 2017/12/15.
 */
public final class SslUtil {

    private SslUtil() {
    }

    public static SSLContext permissiveContext(){
        try {
            SSLContext context=SSLContext.getInstance("TLS");
            context.init(new KeyManager[0],new TrustManager[]{new SslUtil.PermissiveTrustManager()},new SecureRandom());
            return context;
        }catch (KeyManagementException | NoSuchAlgorithmException var1){
            throw new RuntimeException("error SSL",var1);
        }
    }

    public static class PermissiveTrustManager implements X509TrustManager{

        public PermissiveTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates,String s){
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates,String s){

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
