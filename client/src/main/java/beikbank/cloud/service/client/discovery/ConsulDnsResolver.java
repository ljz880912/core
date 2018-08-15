package beikbank.cloud.service.client.discovery;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/11/011.
 */
public class ConsulDnsResolver {
    private static final Logger log = LoggerFactory.getLogger(ConsulDnsResolver.class);
    private SimpleResolver resolver;

    public ConsulDnsResolver(String resolverHost, int resolverPort) {
        try {
            this.resolver = new SimpleResolver(resolverHost);
            this.resolver.setPort(resolverPort);
        } catch (UnknownHostException var4) {
            log.error(var4.getLocalizedMessage(), var4);
        }

    }

    public List<ServiceAddress> resolve(String serviceName, String tag) throws IOException {
        return this.resolve(serviceName, tag, "");
    }

    public List<ServiceAddress> resolve(String serviceName, String tag, String dc) throws IOException {
        Name target;
        try {
            String fqdn = this.getServiceFDQN(dc, serviceName, tag);
            target = Name.fromString(fqdn);
        } catch (TextParseException var18) {
            log.error(var18.getMessage(), var18);
            throw var18;
        }

        List<ServiceAddress> addressList = new ArrayList();
        Lookup lookup = new Lookup(target, 33);
        lookup.setResolver(this.resolver);
        Record question = Record.newRecord(target, 33, 1);
        Message query = Message.newQuery(question);
        Message response = this.resolver.send(query);
        Record[] answer = response.getSectionArray(1);
        Record[] sectionArray = response.getSectionArray(3);

        for(int i = 0; i < answer.length; ++i) {
            SRVRecord srvRecord = (SRVRecord)answer[i];
            String ip = sectionArray[i].rdataToString();
            String node = srvRecord.getAdditionalName().toString();
            log.debug("Resolved: {} {}:{}", new Object[]{node, ip, srvRecord.getPort()});
            String[] nodeParts = node.split("\\.");
            String nodeDc = nodeParts[nodeParts.length - 2];
            addressList.add(new ServiceAddress(nodeDc, ip, srvRecord.getPort()));
        }

        return addressList;
    }

    private String getServiceFDQN(String dc, String serviceName, String tag) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceName), "serviceName required");
        StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(tag)) {
            sb.append(tag).append('.');
        }

        sb.append(serviceName).append(".service");
        if (!Strings.isNullOrEmpty(dc)) {
            sb.append(".").append(dc);
        }

        sb.append(".consul.");
        return sb.toString();
    }
}
