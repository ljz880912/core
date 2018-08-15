package beikbank.cloud.service.server;

import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContextInitializer;

import java.util.Collection;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class CoreWebContextInitializer implements ApplicationContextInitializer<AnnotationConfigEmbeddedWebApplicationContext> {

    private Collection<String> packages;

    public CoreWebContextInitializer(Collection<String> packages) {
        if(packages != null) {
            this.packages = packages;
        }

    }

    @Override
    public void initialize(AnnotationConfigEmbeddedWebApplicationContext applicationContext) {
        if(this.packages != null) {
            String[] packageArray = new String[this.packages.size()];
            this.packages.toArray(packageArray);
            applicationContext.scan(packageArray);
        }

    }
}
