package beikbank.cloud.service.client.exception;

/**
 * 重试后异常
 * @author : liujianzhao
 * @version :
 * @date: Create in 14:49 2018/4/12
 */
public class RetryAfterException extends ClientException{

    private final Integer seconds;

    public RetryAfterException(int statusCode, String message, Integer seconds) {
        super(statusCode, message, (String)null);
        this.seconds = seconds;
    }

    public Integer getSeconds() {
        return this.seconds;
    }
}
