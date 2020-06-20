package com.consul.leader.elections.leader;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.dto.ServiceNodeInfo;
import com.consul.leader.elections.exception.LeaderNotPresented;
import com.consul.leader.elections.services.ServiceDefinition;
import com.google.gson.Gson;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.SessionInfo;

@Component
@Configuration
public class LeaderObserver {

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.consul.service-leader.time-waiting-before-volunteer-afterstepdown:120}")
    private long TIME_TO_WAIT_LEADER_AFTER_STEPDOWN_BEFORE_VOLUNTEERING_IN_SECONDS;
    @org.springframework.beans.factory.annotation.Value("${spring.cloud.consul.service-leader.time-waiting-before-volunteer-afterintialization:60}")
    private long TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS;
    @org.springframework.beans.factory.annotation.Value("${spring.cloud.consul.service-leader.timeout-stop-try-volunteering:60}")
    private long TIME_TO_WAIT_TRY_VOLUNTEERING_IN_SECONDS;
    @org.springframework.beans.factory.annotation.Value("${spring.cloud.consul.service-leader.session-ttl:60}")
    private int TIME_TTL_LEADER_SESSION_IN_SECONDS;

    private final Logger logger = LoggerFactory.getLogger(LeaderObserver.class);

    @Autowired
    private ConsulConnector connector;

    @Autowired
    private ServiceNodeInfo serviceNode;


    private volatile static LeaderObserver leaderObserver;

    @Autowired
    private LeaderUtil leaderUtil;

    private Leader observedLeader = new Leader();
    private Gson g = new Gson();
    private LeaderElectionUtil leutil;

    @PostConstruct
    private void postConstruct() {
        leutil = new LeaderElectionUtil(connector.getConsulClient(),
                TIME_TTL_LEADER_SESSION_IN_SECONDS);
        if (leaderObserver == null) {
            leaderObserver = this;
        }
        logger.info("LeaderObserver ref initialized");
    }

    @EventListener(WebServerInitializedEvent.class)
    private void bindToAddListner(WebServerInitializedEvent event) {
        logger.trace("WebServerInitializedEvent recieved to Add Listner");
        addListner();
        logger.trace("Listner added successfully");
    }

    public static LeaderObserver getInstance() {
        return LeaderObserver.leaderObserver;
    }

    public ServiceNodeInfo getServiceNode() {
        return serviceNode;
    }

    public void attach(Watcher observer) {
        leaderUtil.attach(observer);
    }

    synchronized private void startObservation(long timeout) throws ConsulException {
        logger.info("Start Observation");
        // this.observedLeader.reset();
        while (this.observedLeader.isEmpty()) {
            if (serviceNode.isCapableLeader())
                iVolunteer();
            else
                waitForAleader(timeout);

        }
    }

    private void waitForAleader(long timeTowaitInMillBeforeVolunteering) {
        logger.info("Waiting For A new leader For" + timeTowaitInMillBeforeVolunteering + " Mills");
        long start = System.currentTimeMillis();
        logger.trace("Start waiting adaptor" + start);

        while (this.observedLeader.isEmpty()) {
            logger.trace("Observed leader is Empty");

            updateLeaderFromConsul();

            if (this.observedLeader.isEmpty()
                    && System.currentTimeMillis() - start > timeTowaitInMillBeforeVolunteering) {
                logger.info("Threshold exceeded Will enable Leadership and Volunteer");
                serviceNode.enableLeadership();
                return;
            }

            logger.info("Waiting for 3 Seconds");
            waitForMillisecond(3000);
        }

    }

    private void updateLeaderFromConsul() {
        try {
            logger.trace("Get Leader Info from Consul");
            Optional<String> leaderOptional =
                    leutil.getLeaderInfoForService(serviceNode.getServiceName());
            if (leaderOptional.isPresent()) {
                String leaderInfo = leaderOptional.get();
                logger.trace("getLeaderInfoForService returned " + leaderInfo);
                if (leaderUtil.isValidLeader(leaderInfo)) {
                    this.observedLeader = g.fromJson(leaderOptional.get(), Leader.class);
                    leaderUtil.sendNewLeaderConfiguredNotification();
                    logger.trace(
                            "Leader is valid , Currently Observed Leader updated successfully");
                }
            }
        } catch (ConsulException e) {
            logger.info("ConsulException occured");
            logger.debug(e.getMessage());
        }
    }


    private void iVolunteer() {
        // bfore volounteer check if leader prsented i n consul and valid
        long start = System.currentTimeMillis();
        while (this.observedLeader.isEmpty()) {
            try {
                logger.info("Start Volunteering baecuase observer is Empty :"
                        + this.observedLeader.isEmpty());
                leutil.releaseLockForService(serviceNode.getServiceName());
                // Need to check if needed
                Leader leaderVoulantierInfo = new Leader(serviceNode.getIPAddress(),
                        serviceNode.getPort(), serviceNode.getNodeId());

                logger.info("Checking leaderVoulantierInfo");

                if (!leaderUtil.isValidLeader(leaderVoulantierInfo)) {
                    waitForMillisecond(5000);
                    continue;
                }

                logger.info("ServiceNode is Valid" + serviceNode.toString());
                executeElection(leaderVoulantierInfo);

            } catch (Exception e) {
                logger.error("Exception happened" + e.getMessage());
                logger.info("Election Failed Will wait and Continue");
                waitForMillisecond(5000);
            }

            if (this.observedLeader.isEmpty() && System.currentTimeMillis()
                    - start > TIME_TO_WAIT_TRY_VOLUNTEERING_IN_SECONDS * 1000) {
                logger.info("Threshold exceeded Will disable Leadership and Volunteer");
                serviceNode.disableLeadership();
                return;
            }

        } // End of while

    }

    private void executeElection(Leader leaderVoulantierInfo) {
        logger.info("Execute Election :");
        Optional<String> electNewLeaderForService =
                leutil.electNewLeaderForService(serviceNode.getServiceName(), leaderVoulantierInfo);

        if (electNewLeaderForService.isPresent()) {
            logger.trace("Election Data is presented" + electNewLeaderForService.get() + " -- "
                    + electNewLeaderForService.toString());

            String leaderInfo = electNewLeaderForService.get();
            this.observedLeader = g.fromJson(leaderInfo, Leader.class);
            logger.debug("Election Result is Current Leader" + leaderInfo);

            if (leaderUtil.isGrantedLeader(serviceNode, this.observedLeader)) {
                logger.info("I'm the leader Now");

                leaderUtil.sendNotifyLeader();
            } else {
                logger.info("I'm Servent Now");
                leaderUtil.sendNewLeaderConfiguredNotification();
            }

        } else {
            logger.trace(
                    "Election Data is not presented will wait for 3s and try to volunteer again");
            waitForMillisecond(3000);
        }

    }


    private void waitForMillisecond(long time) {
        try {

            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.trace(e.getMessage());
        }
    }

    public void stepDownAndWaitnewLeader() {
        if (leaderUtil.isGrantedLeader(serviceNode, this.observedLeader)) {
            try {
                logger.info("I'm Leader and will stepdown");
                leutil.releaseLockForService(serviceNode.getServiceName());
                this.observedLeader.reset();
                serviceNode.disableLeadership();
                leaderUtil.sendNewLeaderConfiguredNotification();
            } catch (ConsulException e) {
                logger.debug("Exception happened During Step Down" + e.getMessage());
                waitForMillisecond(3000);
                stepDownAndWaitnewLeader();
            }

        }

        waitForMillisecond(
                TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS * 1000);
    }

    private void addListner() {
        logger.info("Adding listner to consule leader K/V change");
        final KeyValueClient kvClient = connector.getConsulClient().keyValueClient();
        KVCache cache = KVCache.newCache(kvClient,
                "service/" + this.serviceNode.getServiceName() + "/leader");
        cache.addListener(newValues -> listen(newValues));
        cache.start();
    }


    private void listen(Map<String, Value> newValues) {
        logger.info("New leader Values Recieved From Consul");
        Optional<Value> newValue = newValues.values().stream()
                .filter(value -> value.getKey()
                        .equals("service/" + this.serviceNode.getServiceName() + "/leader"))
                .findAny();

        newValue.ifPresent(value -> {
            Optional<String> decodedValue = newValue.get().getValueAsString();
            boolean decodedValueIsPresent = decodedValue.isPresent();
            if (!decodedValueIsPresent) {
                logger.info("New Decoded Values is not present");
                this.observedLeader.reset();
                leaderUtil.sendNewLeaderConfiguredNotification();
                startObservation(
                        TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS
                                * 1000);
                return;
            } else if (decodedValueIsPresent) {
                String leaderInfo = decodedValue.get().toString();
                logger.info("Values Present for new leader :" + leaderInfo);

                boolean isValidLeader = leaderUtil.isValidLeader(leaderInfo);
                if (!isValidLeader) {
                    logger.info("New Leader is not Valid");
                    this.observedLeader.reset();
                    leaderUtil.sendNewLeaderConfiguredNotification();
                    startObservation(
                            TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS
                                    * 1000);
                    return;
                }

                boolean isNewLeader = leaderUtil.isNewLeader(leaderInfo, this.observedLeader);

                if (isNewLeader) {
                    logger.info(
                            "New Leader is Valid & I's a new leader Will update Observed Leader");
                    this.observedLeader = g.fromJson(decodedValue.get().toString(), Leader.class);
                    leaderUtil.sendNewLeaderConfiguredNotification();
                } else {
                    logger.info("This is not a new leader");
                    volunteerIfLeaderSessionInValid(leaderInfo);
                    return;
                }
            }

        });
        if (!newValue.isPresent()) {
            logger.info("New Values Not Present In listner");
            this.observedLeader.reset();
            leaderUtil.sendNewLeaderConfiguredNotification();
            startObservation(
                    TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS * 1000);
            return;
        }

    }

    protected void volunteerIfLeaderSessionInValid(String leaderInfo) {
        logger.trace("Checking if Leader sionId is Valid");
        Optional<SessionInfo> sessionInfo = connector.getConsulClient().sessionClient()
                .getSessionInfo(g.fromJson(leaderInfo, Leader.class).getSessionId());
        if (!sessionInfo.isPresent()) {
            logger.info("Leader SessionId is not valid will volunteer");
            this.observedLeader.reset();
            startObservation(
                    TIME_TO_WAIT_LEADER_AFTER_INTIALIZATION_BEFORE_VOLUNTEERING_IN_SECONDS * 1000);
        }
    }

    public Leader getCurrentLeader() throws LeaderNotPresented {
        logger.info("Requesting Current Leader");
        if (this.observedLeader == null || this.observedLeader.isEmpty()) {
            logger.info("Current observed leader is empty Requesting New Leader");
            throw new LeaderNotPresented();
        }
        logger.info("Current Leader:" + this.observedLeader);
        return this.observedLeader;
    }

    public boolean isGrantedLeader() {
        return leaderUtil.isGrantedLeader(serviceNode, this.observedLeader);
    }

    public List<ServiceDefinition> getServentList() {
        List<ServiceDefinition> serviceDefList = leaderUtil.getServents();
        logger.debug(serviceDefList.toString());
        return serviceDefList;
    }
}

