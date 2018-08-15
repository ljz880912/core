package beikbank.cloud.service.server.service;

import com.beikbank.common.BaseJsonUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;

/**
 * Created by Administrator on 2018/4/7/007.
 */
@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    @Autowired
    private JedisPool jedisPool;

    public Boolean lock(String key) {

        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        try {
            Thread.sleep(100);
            long k = setNx(key, "tmpValue");
            if (k > 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            System.out.println("发生异常释放锁：" + key);
            jedis.del(key);
            e.printStackTrace();
            return false;
        }
    }

    public void unlock(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        del(key);
    }

    public Boolean exists(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        Boolean var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.exists(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public Boolean existsSave(String key) {
        try {
            return this.exists(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]:{}", key, ExceptionUtils.getStackTrace(var3));
            return null;
        }
    }

    public Long set(String key, Object value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        Long var4;
        try {
            jedis = this.jedisPool.getResource();
            if (!"ok".equalsIgnoreCase(jedis.set(key, BaseJsonUtils.writeValue(value)))) {
                var4 = Long.valueOf(0L);
                return var4;
            }

            var4 = Long.valueOf(1L);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var4;
    }

    public Long setSave(String key, Object value) {
        try {
            return this.set(key, value);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], value[{}]:{}", new Object[]{key, value, ExceptionUtils.getStackTrace(var4)});
            return Long.valueOf(0L);
        }
    }

    public Long setEx(String key, Object value, int expireInSecond) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        Long var5;
        try {
            jedis = this.jedisPool.getResource();
            if (!"ok".equalsIgnoreCase(jedis.setex(key, expireInSecond, BaseJsonUtils.writeValue(value)))) {
                var5 = Long.valueOf(0L);
                return var5;
            }

            var5 = Long.valueOf(1L);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var5;
    }

    public Long setExSave(String key, Object value, int expireInSecond) {
        try {
            return this.setEx(key, value, expireInSecond);
        } catch (Throwable var5) {
            log.error("redis操作异常. key[{}], value[{}]:{}", new Object[]{key, value, ExceptionUtils.getStackTrace(var5)});
            return Long.valueOf(0L);
        }
    }

    public Long setNx(String key, Object value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        Long var4;
        try {
            jedis = this.jedisPool.getResource();
            var4 = jedis.setnx(key, BaseJsonUtils.writeValue(value));
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var4;
    }

    public Long setNxSave(String key, Object value) {
        try {
            return this.setNx(key, value);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], value[{}]:{}", new Object[]{key, value, ExceptionUtils.getStackTrace(var4)});
            return Long.valueOf(0L);
        }
    }

    public Long setNxAndExpire(String key, Object value, Long expireInSecond) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Preconditions.checkArgument(expireInSecond != null, "expireInSecond不能为null");
        Jedis jedis = null;

        Long var5;
        try {
            jedis = this.jedisPool.getResource();
            if ("ok".equalsIgnoreCase(jedis.set(key, BaseJsonUtils.writeValue(value), "NX", "EX", expireInSecond.longValue()))) {
                var5 = Long.valueOf(1L);
                return var5;
            }

            var5 = Long.valueOf(0L);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var5;
    }

    public Long setNxAndExpireSave(String key, Object value, Long expireInSecond) {
        try {
            return this.setNxAndExpire(key, value, expireInSecond);
        } catch (Throwable var5) {
            log.error("redis操作异常. key[{}], value[{}], expire[{}]: {}", new Object[]{key, value, expireInSecond, ExceptionUtils.getStackTrace(var5)});
            return Long.valueOf(0L);
        }
    }

    public String get(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        String var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.get(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public String getSave(String key) {
        try {
            return this.get(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]: {}", key, ExceptionUtils.getStackTrace(var3));
            return null;
        }
    }

    public <T> T get(String key, Class<T> cls) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(cls != null, "cls不能为null");
        Jedis jedis = null;

        Object var4;
        try {
            jedis = this.jedisPool.getResource();
            var4 = BaseJsonUtils.readValue(jedis.get(key), cls);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return (T) var4;
    }

    public <T> T getSave(String key, Class<T> cls) {
        try {
            return this.get(key, cls);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}]: {}", key, ExceptionUtils.getStackTrace(var4));
            return null;
        }
    }

    public Long del(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        Long var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.del(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public Long delSave(String key) {
        try {
            return this.del(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]: {}", key, ExceptionUtils.getStackTrace(var3));
            return Long.valueOf(0L);
        }
    }

    public void lpush(String key, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            jedis.lpush(key, new String[]{value});
        } finally {
            IOUtils.closeQuietly(jedis);
        }

    }

    public void lpushSave(String key, String value) {
        try {
            this.lpush(key, value);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], value[{}]: {}", new Object[]{key, value, ExceptionUtils.getStackTrace(var4)});
        }

    }

    public String rpop(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        String var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.rpop(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public String rpopSave(String key) {
        try {
            return this.rpop(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]:{}", key, ExceptionUtils.getStackTrace(var3));
            return null;
        }
    }

    public Long llen(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        Long var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.llen(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public Long llenSave(String key) {
        try {
            return this.llen(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]:{}", key, ExceptionUtils.getStackTrace(var3));
            return null;
        }
    }

    public Long sAdd(String key, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        Long var4;
        try {
            jedis = this.jedisPool.getResource();
            var4 = jedis.sadd(key, new String[]{value});
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var4;
    }

    public Long sAddSave(String key, String value) {
        try {
            return this.sAdd(key, value);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], value[{}]: {}", new Object[]{key, value, ExceptionUtils.getStackTrace(var4)});
            return Long.valueOf(0L);
        }
    }

    public Set<String> sMembers(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        Set var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.smembers(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public Set<String> sMembersSave(String key) {
        try {
            return this.sMembers(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]: {}", key, ExceptionUtils.getStackTrace(var3));
            return new HashSet(0);
        }
    }

    public Long sRem(String key, Set<String> members) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        if (members.isEmpty()) {
            return Long.valueOf(0L);
        } else {
            Jedis jedis = null;

            Long var4;
            try {
                jedis = this.jedisPool.getResource();
                var4 = jedis.srem(key, (String[]) members.toArray(new String[0]));
            } finally {
                IOUtils.closeQuietly(jedis);
            }

            return var4;
        }
    }

    public Long sRemSave(String key, Set<String> members) {
        try {
            return this.sRem(key, members);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}]: {}", key, ExceptionUtils.getStackTrace(var4));
            return Long.valueOf(0L);
        }
    }

    public Boolean sIsMember(String key, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        Boolean var4;
        try {
            jedis = this.jedisPool.getResource();
            var4 = jedis.sismember(key, value);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var4;
    }

    public Boolean sIsMemberSave(String key, String value) {
        try {
            return this.sIsMember(key, value);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], value[{}]: {}", new Object[]{key, value, ExceptionUtils.getStackTrace(var4)});
            return null;
        }
    }

    public void hSet(String key, String field, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(field), "field不能为空");
        Preconditions.checkArgument(value != null, "value不能为null");
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            jedis.hset(key, field, value);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

    }

    public void hSetSave(String key, String field, String value) {
        try {
            this.hSet(key, field, value);
        } catch (Throwable var5) {
            log.error("redis操作异常. key[{}], field[{}], value[{}]:{}", new Object[]{key, field, value, ExceptionUtils.getStackTrace(var5)});
        }

    }

    public String hGet(String key, String field) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(field), "field不能为空");
        Jedis jedis = null;

        String var4;
        try {
            jedis = this.jedisPool.getResource();
            var4 = jedis.hget(key, field);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var4;
    }

    public String hGetSave(String key, String field) {
        try {
            return this.hGet(key, field);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], field[{}]:{}", new Object[]{key, field, ExceptionUtils.getStackTrace(var4)});
            return null;
        }
    }

    public Map<String, String> hGetAll(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        Map var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.hgetAll(key);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public Map<String, String> hGetAllSave(String key) {
        try {
            return this.hGetAll(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]:{}", key, ExceptionUtils.getStackTrace(var3));
            return new HashMap(0);
        }
    }

    public void hDel(String key, String... fields) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(fields != null && fields.length > 0, "fields不能为空");
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            jedis.hdel(key, fields);
        } finally {
            IOUtils.closeQuietly(jedis);
        }

    }

    public void hDelSave(String key, String... fields) {
        try {
            this.hDel(key, fields);
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], fields[{}]:{}", new Object[]{key, fields == null ? "null" : Arrays.asList(fields), ExceptionUtils.getStackTrace(var4)});
        }

    }

    public long incr(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Jedis jedis = null;

        long var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.incr(key).longValue();
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var3;
    }

    public long incrSave(String key) {
        try {
            return this.incr(key);
        } catch (Throwable var3) {
            log.error("redis操作异常. key[{}]:{}", key, ExceptionUtils.getStackTrace(var3));
            return 0L;
        }
    }

    public long incrEx(String key, Integer expireInSecond) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        Preconditions.checkArgument(expireInSecond != null, "expireInSecond不能为null");
        Jedis jedis = null;

        long var6;
        try {
            jedis = this.jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            Response<Long> result = pipeline.incr(key);
            pipeline.expire(key, expireInSecond.intValue());
            pipeline.sync();
            var6 = ((Long) result.get()).longValue();
        } finally {
            IOUtils.closeQuietly(jedis);
        }

        return var6;
    }

    public long incrExSave(String key, int expireInSecond) {
        try {
            return this.incrEx(key, Integer.valueOf(expireInSecond));
        } catch (Throwable var4) {
            log.error("redis操作异常. key[{}], expire[{}]: {}", new Object[]{key, Integer.valueOf(expireInSecond), ExceptionUtils.getStackTrace(var4)});
            return 0L;
        }
    }
}
