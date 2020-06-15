package pl.piomin.services.customer.leader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import pl.piomin.services.customer.dto.Leader;
import pl.piomin.services.customer.dto.ServiceNodeInfo;
import pl.piomin.services.customer.event.IfGrantedLeaderEventPublisher;
import pl.piomin.services.customer.event.NewLeaderConfiguredEventPublisher;
import pl.piomin.services.customer.services.ConsulServiceDiscovery;
import pl.piomin.services.customer.services.ServiceDefinition;

@Component
public class LeaderUtil {

    @Autowired
    private NewLeaderConfiguredEventPublisher newLeaderConfiguredEvent;
    @Autowired
    private IfGrantedLeaderEventPublisher ifGrantedLeaderEvent;

    @Autowired
    ConsulServiceDiscovery discovery;

    private Gson g = new Gson();
    private CopyOnWriteArrayList<Watcher> watchers = new CopyOnWriteArrayList<Watcher>();
    private static final Logger logger = LoggerFactory.getLogger(LeaderUtil.class);


    protected void attach(Watcher watcher) {
        logger.debug("Attaching new Observer");
        watchers.add(watcher);
        if (logger.isInfoEnabled() || logger.isDebugEnabled())
            logger.info("New Watcher Attached");
    }

    protected void sendNewLeaderConfiguredNotification() {
        logger.info("Publishing New Leader Configured Event");

        (new Thread(new Runnable() {
            @Override
            public void run() {
                newLeaderConfiguredEvent.publish();
            }
        })).start();

    }

    protected void sendNotifyLeader() {
        logger.info("Publishing Granite Leader Event");

        (new Thread(new Runnable() {
            @Override
            public void run() {
                ifGrantedLeaderEvent.publish();
            }
        })).start();
    }

    protected boolean isValidLeader(Object leaderInfo) {
        Leader leader = new Leader();;
        try {
            if (leaderInfo.getClass() == String.class) {
                logger.trace("Checking if leader valid" + leaderInfo.toString());
                leader = g.fromJson((String) leaderInfo, Leader.class);
            } else if (leaderInfo.getClass() == Leader.class) {
                logger.trace("Checking if leader valid" + leader.toString());
                leader = (Leader) leaderInfo;
            }

            if (leader == null || StringUtils.isBlank(leader.getIPAddress())
                    || StringUtils.isBlank(leader.getNodeId()) || leader.getPort() < 0) {
                return false;
            }
        } catch (Exception e) {
            logger.debug("Validation Failed with exception" + e.getMessage());
            return false;
        }
        return true;
    }

    protected boolean isNewLeader(String leaderInfo, Leader leader) {
        logger.trace("Checking if leader is new (" + leaderInfo.toString() + "),("
                + leader.toString() + ")");
        return (isValidLeader(leaderInfo) && !leaderInfo.equals(g.toJson(leader).toString())) ? true
                : false;
    }

    protected boolean isGrantedLeader(ServiceNodeInfo serviceNode, Leader leader) {
        logger.trace("Checking if current serviceNode is granted Leader (" + serviceNode.toString()
                + "),(" + leader.toString() + ")");
        return g.toJson(new Leader(serviceNode.getIPAddress(), serviceNode.getPort(),
                serviceNode.getNodeId())).toString().equals(g.toJson(leader).toString()) ? true
                        : false;
    }


    public List<ServiceDefinition> getServents() {
        return discovery.getServices();
    }

}
