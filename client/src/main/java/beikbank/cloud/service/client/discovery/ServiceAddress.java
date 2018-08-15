package beikbank.cloud.service.client.discovery;

import lombok.Data;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 13:46 2018/4/12
 */
@Data
public class ServiceAddress {

    private static final String DEFAULT_DC = "dc1";
    private String host;
    private int port;
    private String dc;

    public ServiceAddress(String hostname, int port) {
        this(DEFAULT_DC, hostname, port);
    }

    public ServiceAddress(String dc, String hostname, int port) {
        this.host = hostname;
        this.port = port;
        this.dc = dc;
    }
}
