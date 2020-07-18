package com.consul.leader.elections.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.Watcher;

@Component
@Configuration
public class OnLeaderCheckImpl {

    private static final Logger logger = LoggerFactory.getLogger(OnLeaderCheckImpl.class);

    @Autowired
    private LeaderObserver observer;

    public boolean onLeaderModeCheck() {
        if (Watcher.isLeader()) {
            // logger.info("I'm leader Leader");
            return true;
        }
        logger.info("It's Not Leader");
        return false;
    }
}
