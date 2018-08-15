package beikbank.cloud.service.server.filter.ratelimit;

import beikbank.cloud.service.server.model.ConcurrentHashSet;
import beikbank.cloud.service.server.service.RedisService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/4/10/010.
 */
@Data
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);
    private String serviceName;
    private RateLimiter.Type type;
    private RateLimiter.Strategy strategy;
    private Integer duration;
    private Integer limit;
    private Map<String, Integer> countMap = new ConcurrentHashMap();
    private Set<String> distributedSet = new ConcurrentHashSet();
    private RedisService redis;

    /**
     * 策略
     */
    enum Strategy {
        LOCAL,
        //分布式
        DISTRIBUTED;

        private Strategy() {
        }
    }

    enum Type {
        IP,
        ORG,
        USER,
        TOKEN;

        private Type() {
        }
    }

    public RateLimiter(RedisService redis) {
        this.redis = redis;
    }

    public boolean incr(String typeValue) {
        Integer count = Integer.valueOf(1);
        String key = this.getKey(typeValue);
        if(this.countMap.containsKey(key)) {
            count = this.countMap.get(key);
            count = Integer.valueOf(count.intValue() + 1);
        }

        if(count.intValue() > this.limit.intValue()) {
            this.countMap.put(key, count);
            log.error("访问超限[key:{},count:{}]", key, count);
            return false;
        } else {
            if(this.strategy == RateLimiter.Strategy.DISTRIBUTED && this.redis != null) {
                int redisCount;
                if(this.distributedSet.contains(key)) {
                    redisCount = (int)this.redis.incrSave(key);
                } else {
                    redisCount = (int)this.redis.incrExSave(key, this.duration.intValue());
                    if(redisCount != 0) {
                        this.distributedSet.add(key);
                    }
                }

                count = Integer.valueOf(Math.max(redisCount, count.intValue()));
                if(redisCount < count.intValue()) {
                    this.redis.setExSave(key, count, this.duration.intValue());
                }
            }

            this.removeExpiredKey();
            this.countMap.put(key, count);
            if(count.intValue() > this.limit.intValue()) {
                log.error("访问超限[key:{},count:{}]", key, count);
                return false;
            } else {
                return true;
            }
        }
    }

    private String getKey(String typeValue) {
        return this.getServiceName() + ":" + this.getType().name().toLowerCase() + ":" + typeValue + ":" + this.duration + ":" + System.currentTimeMillis() / 1000L / (long)this.duration.intValue();
    }

    public void removeExpiredKey() {
        Iterator keys = this.countMap.keySet().iterator();

        while(keys.hasNext()) {
            String key = (String)keys.next();
            if(this.keyExpired(key)) {
                keys.remove();
                this.distributedSet.remove(key);
            }
        }

    }

    private boolean keyExpired(String key) {
        int bucket = Integer.valueOf(StringUtils.substringAfterLast(key, ":")).intValue();
        return System.currentTimeMillis() / 1000L / (long)this.duration.intValue() > (long)bucket;
    }
}
