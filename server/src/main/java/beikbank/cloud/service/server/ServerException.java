package beikbank.cloud.service.server;

import lombok.Data;

/**
 * Created by Administrator on 2018/4/11/011.
 */
@Data
public class ServerException extends RuntimeException{

    private String errorKey;
    private String[] args;

    public ServerException(String errorKey, String... args) {
        this.errorKey = errorKey;
        this.args = args;
    }

    public static ServerException fromKey(String key, String... args) {
        return new ServerException(key, args);
    }
}
