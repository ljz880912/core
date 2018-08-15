package beikbank.cloud.service.common.exception;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class CoreRuntimeException extends RuntimeException{

    public CoreRuntimeException() {
    }

    public CoreRuntimeException(Throwable e) {
        super(e);
    }

    public CoreRuntimeException(String message) {
        super(message);
    }

    public CoreRuntimeException(String message, Throwable e) {
        super(message, e);
    }
}
