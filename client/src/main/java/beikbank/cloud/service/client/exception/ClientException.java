package beikbank.cloud.service.client.exception;

import lombok.Data;

/**
 * Created by 琉璃 on 2017/12/14.
 */
@Data
public class ClientException extends CoreClientException{

    private int statusCode;
    private String errorCode;

    public ClientException(int statusCode,String message){
        super(message);
        this.statusCode=statusCode;
    }

    public ClientException(int statusCode,String errorCode,String message){
        super(message);
        this.statusCode=statusCode;
        this.errorCode=errorCode;
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }


}
