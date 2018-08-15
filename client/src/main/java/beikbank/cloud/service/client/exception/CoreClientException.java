package beikbank.cloud.service.client.exception;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 16:24 2018/4/12
 */
public class CoreClientException extends RuntimeException{

    public CoreClientException() {
    }

    public CoreClientException(String message, Throwable e) {
        super(message, e);
    }

    public CoreClientException(Throwable e) {
        super(e);
    }

    public CoreClientException(String message) {
        super(message);
    }
}
