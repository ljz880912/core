package beikbank.cloud.service.server.util.lock;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 13:42 2018/4/11
 */
public class DistributedLockImpl implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockImpl.class);
    private ConsulClient consul;
    private NewSession newSession;
    private String sessionId;

    public DistributedLockImpl(ConsulClient consul) {
        this(consul, 0L, 120L);
    }

    public DistributedLockImpl(ConsulClient consul, long lockDelayInSeconds, long ttlInSeconds) {
        Preconditions.checkNotNull(consul, "consul");
        this.consul = consul;
        this.newSession = new NewSession();
        this.newSession.setLockDelay(lockDelayInSeconds);
        this.newSession.setBehavior(Session.Behavior.DELETE);
        this.newSession.setTtl(ttlInSeconds + "s");
    }

    @Override
    public boolean acquireLock(String theKey) {
        String key = this.getKeyInConsul(theKey);

        try {
            Response sessionResponse;
            if (this.sessionId == null) {
                sessionResponse = this.consul.sessionCreate(this.newSession, QueryParams.DEFAULT);
                if (sessionResponse.getValue() != null) {
                    this.sessionId = (String)sessionResponse.getValue();
                } else {
                    log.debug("Create session for lock failed!");
                }
            }

            if (this.sessionId != null) {
                sessionResponse = this.consul.getSessionInfo(this.sessionId, QueryParams.DEFAULT);
                Session session = (Session)sessionResponse.getValue();
                if (session == null) {
                    this.sessionId = null;
                    return false;
                }

                PutParams params = new PutParams();
                params.setAcquireSession(session.getId());
                Response<Boolean> kvValue = this.consul.setKVValue(key, "", params);
                Boolean acquired = kvValue.getValue();
                if (acquired != null) {
                    if (!acquired) {
                        this.consul.sessionDestroy(this.sessionId, QueryParams.DEFAULT);
                        this.sessionId = null;
                        return false;
                    }

                    return true;
                }
            }

            return false;
        } catch (Exception var8) {
            log.debug("Get lock met exception {}", var8.getMessage());
            return false;
        }
    }

    @Override
    public boolean releaseLock(String theKey) {
        String key = this.getKeyInConsul(theKey);
        if (this.sessionId != null) {
            boolean var4;
            try {
                PutParams params = new PutParams();
                params.setReleaseSession(this.sessionId);
                Response<Boolean> kvValue = this.consul.setKVValue(key, "", params);
                Boolean released =kvValue.getValue();
                this.consul.sessionDestroy(this.sessionId, QueryParams.DEFAULT);
                boolean var6 = released;
                return var6;
            } catch (Exception var10) {
                var4 = false;
            } finally {
                this.sessionId = null;
            }

            return var4;
        } else {
            return false;
        }
    }

    @Override
    public boolean refreshLock(String theKey) {
        String key = this.getKeyInConsul(theKey);
        if (this.sessionId != null) {
            try {
                Response<Session> response = this.consul.renewSession(this.sessionId, QueryParams.DEFAULT);
                return response.getValue() != null;
            } catch (Exception var4) {
                log.error("failed to refresh locker " + key + " on session: " + this.sessionId, var4);
                return false;
            }
        } else {
            return false;
        }
    }

    private String getKeyInConsul(String theKey) {
        return "locks/" + theKey;
    }
}
