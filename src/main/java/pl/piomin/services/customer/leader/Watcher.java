package pl.piomin.services.customer.leader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.customer.event.IfGrantedLeaderEvent;
import pl.piomin.services.customer.event.NewLeaderConfiguredEvent;

public interface Watcher {

    static final Logger logger = LoggerFactory.getLogger(Watcher.class);

    default void start() {
        logger.info("Start attching new Watcher");
        if (LeaderObserver.getInstance() != null) {
            LeaderObserver.getInstance().attach(this);
        }
    }

    public default LeaderObserver getwatcher() {
        return LeaderObserver.getInstance();
    }

    public void onGrantedLeaderNotification(IfGrantedLeaderEvent event);

    public void newLeaderNotification(NewLeaderConfiguredEvent event);


}
