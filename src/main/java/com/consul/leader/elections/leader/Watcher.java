package com.consul.leader.elections.leader;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import com.consul.leader.distributed.processing.DistributedOperation;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.event.IfGrantedLeaderEvent;
import com.consul.leader.elections.event.NewLeaderConfiguredEvent;
import com.consul.leader.elections.exception.LeaderNotPresented;

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

    public static boolean isLeader() {
        return LeaderObserver.getInstance().getServiceNode().isLeader();
    }

    public static Leader getCurrentLeader() {
        Leader leader = null;
        while (leader == null) {
            try {
                leader = LeaderObserver.getInstance().getCurrentLeader();
            } catch (LeaderNotPresented e) {
                // TODO Auto-generated catch block
                leader = null;
            }
        }

        return leader;

    }

    @EventListener(IfGrantedLeaderEvent.class)
    public default void receiveOnGrantedLeaderNotification() {
        logger.info("I'm Leader Right Now");
        LeaderObserver.getInstance().getServiceNode().setIsCurrentlyLeaderValue(true);
        onGrantedLeaderNotification();
    }

    @EventListener(NewLeaderConfiguredEvent.class)
    public default void receivenewLeaderNotification() {
        if (LeaderObserver.getInstance().getServiceNode().isLeader())
            logger.info("I'm not Leader Right Now ,New Leader is selected");
        LeaderObserver.getInstance().getServiceNode().setIsCurrentlyLeaderValue(false);
        newLeaderNotification();
    }

    public void onGrantedLeaderNotification();

    public void newLeaderNotification();

    public Optional<?> processDistributedOperations(List<DistributedOperation> operations);
}
