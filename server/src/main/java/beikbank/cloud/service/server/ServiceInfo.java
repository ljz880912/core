package beikbank.cloud.service.server;

import java.beans.ConstructorProperties;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class ServiceInfo {

    private String title;
    private String description;
    private String serviceName;
    private String version;

    @ConstructorProperties({"title", "description", "serviceName", "version"})
    public ServiceInfo(String title, String description, String serviceName, String version) {
        this.title = title;
        this.description = description;
        this.serviceName = serviceName;
        this.version = version;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getVersion() {
        return this.version;
    }
}
