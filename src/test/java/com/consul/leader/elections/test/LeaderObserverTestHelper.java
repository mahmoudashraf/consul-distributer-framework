package com.consul.leader.elections.test;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.dto.ServiceNodeInfo;
import com.consul.leader.elections.leader.ConsulConnector;
import com.consul.leader.elections.leader.LeaderElectionUtil;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.SessionHolder;
import com.consul.leader.elections.resources.WebServerInitializedEventDummyPublisher;
import com.google.gson.Gson;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

@Component
public class LeaderObserverTestHelper {

    private static final String TTL_TEMPLATE = "%ss";
    LeaderElectionUtil leutil;
    @Autowired
    LeaderObserver leaderObserver;
    @Autowired
    ServiceNodeInfo serviceNode;

    public ServiceNodeInfo getServiceNode() {
        return serviceNode;
    }

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
        leutil = new LeaderElectionUtil(connector.getConsulClient(), 60);
    }

    protected String createSession() {
        SessionHolder holder =
                new SessionHolder(connector.getConsulClient(), serviceNode.getServiceName(), 120);
        return holder.getId();
    }

    protected void destroySession(String sessionId) {
        try {
            connector.getConsulClient().sessionClient().destroySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            leutil.electNewLeaderForService(serviceNode.getServiceName(), (Leader) leader);

        if (leader.getClass() == String.class)
            leutil.electNewLeaderForService(serviceNode.getServiceName(),
                    g.fromJson((String) leader, Leader.class));
    }

    protected void setُEmptyLeaderInConsul(String serviceName, long ttl) {
        final String key = getServiceKey(serviceName);
        final Session session = ImmutableSession.builder().name(serviceName)
                .ttl(String.format(TTL_TEMPLATE, ttl)).build();
        String sessionId =
                connector.getConsulClient().sessionClient().createSession(session).getId();
        connector.getConsulClient().keyValueClient().acquireLock(key, "", sessionId);
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

    private static String getServiceKey(String serviceName) {
        return "service/" + serviceName + "/leader";
    }
}
