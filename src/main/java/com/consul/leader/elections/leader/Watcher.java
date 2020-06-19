package com.consul.leader.elections.leader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consul.leader.elections.event.IfGrantedLeaderEvent;
import com.consul.leader.elections.event.NewLeaderConfiguredEvent;

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
