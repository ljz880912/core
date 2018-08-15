package beikbank.cloud.service.client.auth;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Administrator on 2017/11/26/026.
 */
public interface AuthorizationProvider extends Closeable{

    String getAuthorization();

    boolean refreshToken();

    @Override
    default void close() throws IOException{

    }
}
