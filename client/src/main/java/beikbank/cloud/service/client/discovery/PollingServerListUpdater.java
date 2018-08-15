package beikbank.cloud.service.client.discovery;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 14:12 2018/4/12
 */
public class PollingServerListUpdater implements ServerListUpdater{

    private static final Logger log = LoggerFactory.getLogger(PollingServerListUpdater.class);
    private static long SERVER_LIST_CACHE_UPDATE_DELAY = 1000L;
    private static int SERVER_LIST_CACHE_REPEAT_INTERVAL = 30000;
    private final AtomicBoolean isActive;
    private volatile long lastUpdated;
    private final long initialDelayMs;
    private final long refreshIntervalMs;
    private volatile ScheduledFuture<?> scheduledFuture;

    private static ScheduledThreadPoolExecutor getRefreshExecutor() {
        return PollingServerListUpdater.LazyHolder.serverListRefreshExecutor;
    }

    public PollingServerListUpdater() {
        this(SERVER_LIST_CACHE_UPDATE_DELAY, (long)SERVER_LIST_CACHE_REPEAT_INTERVAL);
    }

    public PollingServerListUpdater(long initialDelayMs, long refreshIntervalMs) {
        this.isActive = new AtomicBoolean(false);
        this.lastUpdated = System.currentTimeMillis();
        this.initialDelayMs = initialDelayMs;
        this.refreshIntervalMs = refreshIntervalMs;
    }

    @Override
    public synchronized void start(UpdateAction updateAction) {

        if (this.isActive.compareAndSet(false, true)) {
            Runnable wrapperRunnable = () -> {
                if (!this.isActive.get()) {
                    if (this.scheduledFuture != null) {
                        this.scheduledFuture.cancel(true);
                    }

                } else {
                    try {
                        updateAction.doUpdate();
                        this.lastUpdated = System.currentTimeMillis();
                    } catch (Exception var3) {
                        log.warn("Failed one update cycle", var3);
                    }

                }
            };
            this.scheduledFuture = getRefreshExecutor().scheduleWithFixedDelay(wrapperRunnable, this.initialDelayMs, this.refreshIntervalMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public synchronized void stop() {
        if (this.isActive.compareAndSet(true, false) && this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
    }

    @Override
    public String getLastUpdate() {
        return (new Date(this.lastUpdated)).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return System.currentTimeMillis() - this.lastUpdated;
    }

    @Override
    public int getNumberMissedCycles() {
        return !this.isActive.get() ? 0 : (int)((long)((int)(System.currentTimeMillis() - this.lastUpdated)) / this.refreshIntervalMs);
    }

    @Override
    public int getCoreThreads() {
        return this.isActive.get() && getRefreshExecutor() != null ? getRefreshExecutor().getCorePoolSize() : 0;
    }

    private static class LazyHolder {
        private static Thread shutdownThread;
        private static final Integer poolSize = 2;
        static ScheduledThreadPoolExecutor serverListRefreshExecutor = null;

        private LazyHolder() {
        }

        private static void shutdownExecutorPool() {
            if (serverListRefreshExecutor != null) {
                serverListRefreshExecutor.shutdown();
                if (shutdownThread != null) {
                    try {
                        Runtime.getRuntime().removeShutdownHook(shutdownThread);
                    } catch (IllegalStateException var1) {
                        ;
                    }
                }
            }

        }

        static {
            int coreSize = poolSize;
            ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat("PollingServerListUpdater-%d").setDaemon(true).build();
            serverListRefreshExecutor = new ScheduledThreadPoolExecutor(coreSize, factory);
            shutdownThread = new Thread(() -> {
                PollingServerListUpdater.log.info("Shutting down the Executor Pool for PollingServerListUpdater");
                shutdownExecutorPool();
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
    }
}
