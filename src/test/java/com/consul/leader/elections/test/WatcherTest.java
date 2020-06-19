package com.consul.leader.elections.test;


import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.annotation.OnLeader;
import com.consul.leader.elections.annotation.OnServent;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.event.IfGrantedLeaderEvent;
import com.consul.leader.elections.event.NewLeaderConfiguredEvent;
import com.consul.leader.elections.exception.LeaderNotPresented;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

@Component
public class WatcherTest implements Watcher {


    private boolean isLeader = false;

    public WatcherTest() {
        start();
    }

    @OnLeader
    public List<ServiceDefinition> getServents() {
        return getwatcher().getServentList();
    }

    @OnServent
    public Leader doServent() throws LeaderNotPresented {
        return getwatcher().getCurrentLeader();
    }

    @Override
    @EventListener(IfGrantedLeaderEvent.class)
    public void onGrantedLeaderNotification(IfGrantedLeaderEvent event) {
        setLeader(true);
    }

    @Override
    @EventListener(NewLeaderConfiguredEvent.class)
    public void newLeaderNotification(NewLeaderConfiguredEvent event) {
        setLeader(false);
    }

    @OnLeader
    public void stepDown() {
        getwatcher().stepDownAndWaitnewLeader();
    }

    @OnServent
    public void stepUp() {
        getwatcher().getServiceNode().enableLeadership();
    }

    public boolean isLeader() {
        return isLeader;
    }

    private void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }


}
