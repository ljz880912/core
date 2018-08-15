package beikbank.cloud.service.server.consul;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * consul动态读取配置不需要重启
 * @author : liujianzhao
 * @version :
 * @date: Create in 14:56 2018/4/10
 */
public class PolledConsulConfigurationSource implements PolledConfigurationSource{

    private static final Logger log = LoggerFactory.getLogger(PolledConsulConfigurationSource.class);
    private final String keyName;
    private ConsulClient consul;
    private String token;
    private long keyIndex = 0L;

    public PolledConsulConfigurationSource(String keyName,String token,ConsulClient consulClient){
        this.keyName=keyName;
        this.consul=consulClient;
        this.token=token;
    }

    @Override
    public PollResult poll(boolean initial, Object checkPoint) throws Exception {
        Response<GetValue> optValue = this.consul.getKVValue(this.keyName, Strings.isNullOrEmpty(this.token) ? null : this.token, new QueryParams(ConsistencyMode.CONSISTENT));
        GetValue value = optValue.getValue();
        if (value != null) {
            long crtIndex = value.getModifyIndex();
            if (this.keyIndex == crtIndex && !initial) {
                return PollResult.createIncremental(null, null, null, value.getFlags());
            } else {
                log.info("consul:[{}]内容加载", value.getKey());
                this.keyIndex = crtIndex;
                String propString = this.decodeBase64(value.getValue());
                Map<String, Object> map = Maps.newHashMap();
                if (propString == null) {
                    return PollResult.createFull(map);
                } else {
                    Properties properties = new Properties();
                    properties.load(new StringReader(propString));
                    properties.entrySet().forEach((e) -> {
                        map.put((String) e.getKey(), e.getValue());
                    });
                    return PollResult.createFull(map);
                }
            }
        } else {
            if (StringUtils.containsIgnoreCase(this.keyName, "config")) {
                log.error("无法从consul中获取服务配置[key:{}]", this.keyName);
            }
            return PollResult.createFull(Maps.newHashMap());
        }
    }


    private String decodeBase64(String value) {
        return value == null ? null : new String(Base64Utils.decodeFromString(value), Charsets.UTF_8);
    }
}
