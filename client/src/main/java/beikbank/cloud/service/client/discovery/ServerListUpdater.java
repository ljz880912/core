package beikbank.cloud.service.client.discovery;

/**
 * @author : liujianzhao
 * @version :
 * @date: Create in 14:44 2018/4/12
 */
public interface ServerListUpdater {

    void start(ServerListUpdater.UpdateAction var1);

    void stop();

    String getLastUpdate();

    long getDurationSinceLastUpdateMs();

    int getNumberMissedCycles();

    int getCoreThreads();

    public interface UpdateAction {
        void doUpdate();
    }
}
