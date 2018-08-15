package beikbank.cloud.service.server.util.lock;

/**
 * 分布式锁
 * @author : liujianzhao
 * @version :
 * @date: Create in 13:11 2018/4/11
 */
public interface DistributedLock {

    boolean acquireLock(String var1);

    boolean releaseLock(String var1);

    boolean refreshLock(String var1);
}
