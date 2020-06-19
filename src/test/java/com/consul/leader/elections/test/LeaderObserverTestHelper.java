package com.consul.leader.elections.test;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.dto.ServiceNodeInfo;
import com.consul.leader.elections.leader.ConsulConnector;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.resources.WebServerInitializedEventDummyPublisher;
import com.google.gson.Gson;
import com.orbitz.consul.util.LeaderElectionUtil;

@Component
public class LeaderObserverTestHelper {

    LeaderElectionUtil leutil;
    @Autowired
    LeaderObserver leaderObserver;
    @Autowired
    ServiceNodeInfo serviceNode;
    @Autowired
    private ConsulConnector connector;
    @Autowired
    private WebServerInitializedEventDummyPublisher publisher;
    private Gson g = new Gson();

    /*
     * @InjectMocks private Delete_EventPublisherService eventPublisherService;
     */

    @PostConstruct
    private void postConstruct() {
        leutil = new LeaderElectionUtil(connector.getConsulClient());
    }

    public LeaderObserver getLeaderObserver() {
        return leaderObserver;
    }

    protected void publishWebServerInitializedEventCustom() {
        publisher.publish();
    }

    protected String getLeaderFromConsul() {
        return leutil.getLeaderInfoForService(serviceNode.getServiceName()).get();
    }

    protected void setLeaderInConsul(Object leader) {
        leutil.releaseLockForService(serviceNode.getServiceName());

        if (leader.getClass() == Leader.class)
            leutil.electNewLeaderForService(serviceNode.getServiceName(), g.toJson(leader));

        if (leader.getClass() == String.class)
            leutil.electNewLeaderForService(serviceNode.getServiceName(), (String) leader);
    }

    protected void deleteLeaderFromConsul() {
        leutil.releaseLockForService(serviceNode.getServiceName());
    }

    protected boolean IsLeadeChecks() {

        return false;
    }

    protected boolean IsNotLeaderChecks() {

        return false;
    }

    protected void waitForMillisecond(long time) {
        try {

            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected String leaderToJson(Leader leader) {
        return g.toJson(leader);
    }
}
